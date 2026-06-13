# AI 助手性能优化建议

## 当前性能瓶颈分析

### 问题：简单问题（如"上山会不会冷"）响应慢

#### 根本原因
1. **过度的预处理**：每次都执行完整的 skill 匹配和工具执行流程
2. **大 prompt 开销**：包含过多上下文信息
3. **设备端模型推理速度**：Gemini Nano 生成速度有限

---

## 优化方案

### 🔥 高优先级优化

#### 1. 添加快速路径（Fast Path）
对于不需要工具数据的简单问题，跳过 skill 执行。

**位置**：`AiAssistantFragment.kt` 的 `sendMessage` 函数

**实现**：
```kotlin
// 在 sendMessage 函数开始处添加
fun sendMessage(text: String) {
    if (text.isBlank() || isGenerating) return
    // ... 现有的模型检查代码 ...
    
    // 新增：快速路径判断
    val shouldSkipTools = shouldUseF astPath(text)
    
    val selectedSkill = if (!shouldSkipTools) {
        toolExecutionService.selectSkill(text, selectedSkillIds)
    } else {
        null  // 跳过 skill 选择
    }
    
    // 后续只在 selectedSkill != null 时执行工具
}

// 新增辅助函数
private fun shouldUseFastPath(question: String): Boolean {
    val q = question.lowercase()
    // 纯知识问答，不需要实时数据
    return q.length < 50 && (
        q.contains("什么") ||
        q.contains("为什么") ||
        q.contains("如何") ||
        q.contains("怎么") ||
        (q.contains("会") && q.contains("吗")) ||
        q.startsWith("能")
    ) && !(
        // 但这些需要工具数据
        q.contains("现在") ||
        q.contains("当前") ||
        q.contains("我的")
    )
}
```

**预期收益**：节省 200-500ms（skill 匹配 + 工具执行时间）

---

#### 2. 延迟加载 Tool Knowledge
不在发送前加载所有工具知识，而是让模型先尝试回答，仅在需要时才检索。

**位置**：`AiAssistantFragment.kt:469-473`

**当前代码**：
```kotlin
val toolKnowledge = toolKnowledgeService.getPromptContext(
    text,
    currentAiContext?.toolId,
    enabledSkillIds = selectedSkillIds
)
```

**优化后**：
```kotlin
val toolKnowledge = if (shouldSkipTools) {
    null  // 快速路径不加载
} else {
    toolKnowledgeService.getPromptContext(
        text,
        currentAiContext?.toolId,
        enabledSkillIds = selectedSkillIds
    )
}
```

**预期收益**：
- 减少 prompt 大小（最多 5000 字符）
- 节省文本处理和匹配时间（约 50-100ms）

---

#### 3. 优化 Skill 匹配算法
当前 `AiToolSkillMatcher` 对每个 skill 都计算分数，可以提前终止。

**位置**：`AiToolSkillMatcher.kt:18-34`

**优化**：
```kotlin
fun rankWithScores(
    question: String,
    entries: Collection<AiToolSkillEntry>,
    limit: Int = 2,
    minScore: Float = 0.35f  // 新增参数
): List<ScoredSkill> {
    val results = mutableListOf<ScoredSkill>()
    
    for (entry in entries) {
        val score = score(question, entry)
        if (score > 0f) {
            results.add(ScoredSkill(entry, score))
            // 如果找到高分匹配，可以提前返回
            if (score > 0.8f && results.size >= limit) {
                break
            }
        }
    }
    
    return results
        .sortedByDescending { it.score }
        .take(limit)
}
```

**预期收益**：在高置信匹配时节省 30-50% 匹配时间

---

### ⚡ 中优先级优化

#### 4. 缓存聊天历史构建结果
`buildChatHistory` 每次都重新遍历消息，可以缓存。

**位置**：`AiAssistantFragment.kt:736`

**实现**：
```kotlin
private var cachedChatHistory: Pair<Int, String>? = null  // (messageCount, history)

fun buildChatHistory(messages: List<ChatMessage>): String {
    val cached = cachedChatHistory
    if (cached != null && cached.first == messages.size) {
        return cached.second
    }
    
    val history = messages.takeLast(6)  // 现有逻辑
        .filter { !it.isLoading && it.text.isNotBlank() }
        .joinToString("\n") { msg ->
            if (msg.isUser) "User: ${msg.text}" 
            else "Assistant: ${msg.text}"
        }
    
    cachedChatHistory = messages.size to history
    return history
}
```

**预期收益**：节省 10-30ms（取决于历史长度）

---

#### 5. 异步初始化 AI 引擎
当前在首次使用时才初始化，可以提前在后台初始化。

**位置**：`AiAssistantFragment.kt:323-344`

**优化**：将初始化移到 `LaunchedEffect(Unit)` 的开始，与其他加载并行：

```kotlin
LaunchedEffect(Unit) {
    // 并行启动初始化
    val initJob = async {
        if (!aiSubsystem.isEngineReady()) {
            aiSubsystem.initialize()
            aiSubsystem.createConversation(...)
        }
    }
    
    // 同时进行其他准备工作
    val toolId = arguments?.getString("tool_id")
    // ... 现有的 context 加载逻辑 ...
    
    // 等待初始化完成
    try {
        initJob.await()
    } catch (e: Exception) {
        setError(getString(R.string.ai_initialization_failed))
    }
    setIsInitializing(false)
}
```

**预期收益**：首次消息响应快 200-500ms

---

#### 6. 减小 Prompt 模板大小
`AiPromptBuilder.buildSystemPrompt` 包含大量指令（约 2000 字符），可以精简。

**位置**：`AiPromptBuilder.kt:7-35`

**优化方向**：
- 移除重复的安全警告
- 合并类似的指令
- 将长指令拆分为核心和扩展部分，简单问题只用核心

**预期收益**：减少 token 处理开销，提升 5-10% 生成速度

---

### 💡 低优先级优化

#### 7. 实现响应缓存
对于相同或相似的问题，缓存之前的回答。

**实现**：
- 使用问题的哈希作为 key
- 缓存最近 20 条问答
- 相似度匹配阈值 0.9

**预期收益**：重复问题瞬时响应

---

#### 8. Prompt 压缩
使用更紧凑的格式，比如：
- 将 "User question: xxx" 缩短为 "Q: xxx"
- 移除不必要的换行和空格

**预期收益**：减少 10-15% token 数量

---

## 实施优先级

### 第一阶段（立即实施）
1. ✅ **快速路径** - 最大收益，低风险
2. ✅ **延迟加载 Tool Knowledge** - 高收益，低风险

预计总收益：**减少 30-50% 响应时间**（对简单问题）

### 第二阶段（1-2 周内）
3. ⚡ 优化 Skill 匹配算法
4. ⚡ 缓存聊天历史
5. ⚡ 异步初始化

预计额外收益：**再减少 15-25% 响应时间**

### 第三阶段（长期）
6. 💡 精简 Prompt 模板
7. 💡 响应缓存
8. 💡 Prompt 压缩

---

## 性能基准

### 当前性能（估算）
- 简单问题（如"上山会不会冷"）：**2-4 秒**
  - Skill 匹配：200ms
  - 工具执行：300ms
  - Prompt 构建：100ms
  - 模型推理：1.5-3s

### 优化后目标
- 简单问题：**1-2 秒**（快速路径）
- 需要工具的问题：**2-3 秒**（保持现状）

---

## 测试建议

### 性能测试用例
1. **纯知识问答**
   - "上山会不会冷"
   - "雷暴时应该怎么办"
   - "如何使用指南针"

2. **需要实时数据**
   - "现在的气压是多少"
   - "我离目的地还有多远"
   - "今天日出时间"

3. **复杂工作流**
   - "如何判断是否会下雨"
   - "我应该折返吗"

### 性能指标
- 记录每个阶段的耗时
- 对比优化前后的 P50、P90、P99 延迟
- 监控 prompt token 数量变化

---

## 注意事项

1. **保持准确性**：快速路径不能降低回答质量
2. **渐进式部署**：先在测试版启用，收集反馈
3. **降级机制**：如果快速路径失败，回退到完整流程
4. **日志记录**：记录使用快速路径 vs 完整路径的统计

---

## 代码修改清单

### 需要修改的文件
1. `AiAssistantFragment.kt` - 添加快速路径逻辑
2. `AiToolSkillMatcher.kt` - 优化匹配算法
3. `AiPromptBuilder.kt` - 精简 prompt 模板（可选）

### 需要添加的文件
- `AiFastPathDetector.kt` - 快速路径判断逻辑

### 需要添加的测试
- `AiFastPathDetectorTest.kt`
- 性能基准测试

---

## 总结

通过实施**快速路径**和**延迟加载 Tool Knowledge**两项优化，可以显著提升简单问题的响应速度，同时保持复杂问题的准确性。这些优化都是低风险的，不会影响现有功能的正确性。
