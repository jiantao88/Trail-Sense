# Trail-Sense AI 端侧重排设计文档

## 概述

端侧重排（On-Device Reranking）是 AI 助手的核心组件，负责在用户提问和工具/知识库之间做精准匹配。它决定了 AI 能否选对工具、找到对的知识、给出有用的回答。

Trail-Sense 的核心约束是**完全离线运行**，因此重排系统不能依赖云端 API，必须全部在设备端完成。

---

## 架构

### 整体架构

```
用户提问
    │
    ▼
┌─────────────────────────────────────────┐
│           RerankerSubsystem             │
│         (单例，生命周期管理)              │
│                                         │
│  ┌─────────────────────────────────┐    │
│  │        HybridReranker           │    │
│  │    (三级级联策略控制器)           │    │
│  │                                 │    │
│  │  Stage 1: KeywordReranker       │    │
│  │           (关键词召回)            │    │
│  │            │                    │    │
│  │  Stage 2: CrossEncoderReranker  │    │
│  │           (语义精排)              │    │
│  │            │                    │    │
│  │  Stage 3: LlmReranker (可选)    │    │
│  │           (LLM 兜底决策)          │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
    │                        │
    ▼                        ▼
 AiToolSkillService    AiToolKnowledgeService
 (技能匹配)             (知识匹配)
```

### 三级级联策略

| 策略 | 条件 | 说明 |
|------|------|------|
| `KEYWORD_ONLY` | 语义重排关闭 | 仅关键词匹配，最快但精度低 |
| `KEYWORD_THEN_CROSS_ENCODER` | 语义重排开启（默认） | 关键词召回 + 语义精排，平衡速度与精度 |
| `KEYWORD_THEN_CROSS_ENCODER_THEN_LLM` | LLM 引擎就绪 | 前两级 + LLM 兜底，最高精度但最慢 |

策略选择在 `HybridReranker.getActiveStrategy()` 中自动决策，无需手动配置。

### 数据流

```
用户提问 "我冷怎么办"
    │
    ├─→ AiToolSkillService.getMatchingSkills()
    │       │
    │       ├─→ 构建候选列表（从 ai_tool_skills 资源文件）
    │       ├─→ RerankerSubsystem.rerankSkills()
    │       │       │
    │       │       ├─→ Stage 1: KeywordReranker
    │       │       │    召回 8 个候选（recallCount）
    │       │       │
    │       │       ├─→ Stage 2: CrossEncoderReranker
    │       │       │    精排至 3 个（finalTopK）
    │       │       │
    │       │       └─→ Stage 3: LlmReranker（可选）
    │       │            仅当 topScore < 0.45 时触发
    │       │
    │       └─→ 返回匹配的技能列表
    │
    └─→ AiToolKnowledgeService.getPromptContext()
            │
            ├─→ 构建候选列表（从 ai_tool_knowledge 资源文件）
            ├─→ RerankerSubsystem.rerankKnowledge()
            │       （同样的三级级联流程）
            └─→ 返回工具知识 + 指南摘录 → 注入 LLM Prompt
```

---

## 核心组件

### 1. KeywordReranker（关键词重排器）

**角色**：第一阶段召回，快速过滤

**算法**：
- **短语匹配**：检查查询是否包含候选文本中的短语（支持逗号/分号/换行分隔的多语言短语）
- **Token 匹配**：拉丁词元（>2 字符）+ CJK 双字组，计算 Jaccard 重叠率
- **评分**：`phraseScore + tokenScore`

**快路径**：高置信度阈值 `0.8`，命中时跳过后续阶段

**文件**：[KeywordReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/KeywordReranker.kt)

### 2. CrossEncoderReranker（语义重排器）

**角色**：第二阶段精排，理解语义相关性

**核心算法**：TF-IDF 加权的字符 n-gram 哈希嵌入 + 余弦相似度

#### 嵌入计算流程

```
文本 → lowercase
     → 字符 2-gram + 3-gram（支持 CJK 混合）
     → 词元提取（拉丁 + CJK）
     → TF-IDF 加权
         ├─ TF: 1 + log(count)  (sublinear，避免高频词主导)
         ├─ IDF: log((N+1)/(df+1)) + 1  (罕见词权重更高)
         └─ weight = TF × IDF
     → MurmurHash3 → 512 维向量
     → L2 归一化
```

#### 评分公式

```
score = 0.55 × cosineSim(query, candidate)
      + 0.30 × keywordOverlap(query, candidate)
      + 0.15 × positionBias(candidate.metadata)
```

| 信号 | 权重 | 说明 |
|------|------|------|
| 余弦相似度 | 0.55 | 语义相关性的核心信号 |
| 关键词重叠 | 0.30 | 精确匹配的补充信号 |
| 位置偏置 | 0.15 | 来自 metadata["priority"] 的先验权重 |

#### 优化点（v2）

| 优化项 | 旧版 | 新版 | 效果 |
|--------|------|------|------|
| 嵌入维度 | 256 | 512 | 哈希冲突降低 ~50% |
| 哈希函数 | `31*h + c` | MurmurHash3 | 分布更均匀，冲突更少 |
| TF 加权 | 无（词频计数） | Sublinear TF (1+log) | 避免高频词主导 |
| IDF 加权 | 无 | `log((N+1)/(df+1))` | 罕见词获得更高权重 |
| 嵌入缓存 | 无 | LRU 缓存（128 条） | 重复查询零开销 |
| 耗时日志 | 无 | 每次调用记录 | 性能可观测 |

**文件**：[CrossEncoderReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/CrossEncoderReranker.kt)

### 3. LlmReranker（LLM 重排器）

**角色**：第三阶段兜底，处理低置信度场景

**触发条件**：精排 Top 分数 < `0.45`（`LLM_TRIGGER_THRESHOLD`）

**算法**：
1. 构建 rerank prompt（列出候选，要求 LLM 返回 TopK 编号）
2. 通过 `AiInferenceSubsystem.sendMessage()` 发送请求
3. 超时 5 秒（`RERANK_TIMEOUT_MS`）
4. 解析 LLM 返回的编号序列（如 "3,1,5"）

**降级策略**：
- LLM 不可用 → 退回两阶段结果
- LLM 超时/出错 → 退回两阶段结果
- LLM 返回无法解析 → 退回两阶段结果

**文件**：[LlmReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/LlmReranker.kt)

### 4. HybridReranker（混合重排器）

**角色**：策略控制器，协调三级级联

#### 快路径优化

| 条件 | 行为 | 节省 |
|------|------|------|
| 候选数 ≤ topK | 直接返回，跳过所有阶段 | 全部精排开销 |
| 关键词高置信度 (≥0.8) | 跳过语义精排和 LLM | 精排 + LLM 开销 |
| 精排高置信度 (≥0.45) | 跳过 LLM | LLM 推理开销（~2-5 秒） |

#### 降级链

```
三级策略失败 → 退回两阶段结果
两阶段失败   → 退回关键词结果
关键词失败   → 返回空列表（调用方走 fallback）
```

**文件**：[HybridReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/HybridReranker.kt)

---

## 调用方

### AiToolSkillService（技能匹配）

将用户问题与 `ai_tool_skills` 资源文件中的技能工作流匹配。

```kotlin
// 调用示例
val results = rerankerSubsystem.rerankSkills(
    query = question,
    candidates = candidates,      // 技能列表
    recallCount = limit * 3,      // 召回数量
    finalTopK = limit             // 最终返回数量
)
```

**Fallback**：`AiToolSkillMatcher.rankWithScores()`（纯关键词匹配，阈值 0.35）

**文件**：[AiToolSkillService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolSkillService.kt)

### AiToolKnowledgeService（知识匹配）

将用户问题与 `ai_tool_knowledge` 资源文件中的工具知识匹配，并提取生存指南摘录。

```kotlin
// 调用示例
val results = rerankerSubsystem.rerankKnowledge(
    query = question,
    candidates = candidates,      // 工具知识列表
    recallCount = limit * 2,      // 召回数量
    finalTopK = limit             // 最终返回数量
)
```

**Fallback**：`AiToolKnowledgeMatcher.rank()`（纯关键词匹配）

**文件**：[AiToolKnowledgeService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolKnowledgeService.kt)

---

## 配置

### 用户可配置

| 设置项 | 位置 | 默认值 | 说明 |
|--------|------|--------|------|
| 语义重排开关 | AI 设置页 | 开启 | 控制 CrossEncoderReranker 是否可用 |

### 代码常量

| 常量 | 位置 | 值 | 说明 |
|------|------|-----|------|
| `EMBEDDING_DIM` | CrossEncoderReranker | 512 | 哈希嵌入维度 |
| `MAX_CACHE_SIZE` | CrossEncoderReranker | 128 | 嵌入缓存大小 |
| `SEMANTIC_WEIGHT` | CrossEncoderReranker | 0.55 | 语义相似度权重 |
| `KEYWORD_WEIGHT` | CrossEncoderReranker | 0.30 | 关键词重叠权重 |
| `POSITION_WEIGHT` | CrossEncoderReranker | 0.15 | 位置偏置权重 |
| `HIGH_CONFIDENCE_THRESHOLD` | KeywordReranker | 0.8 | 关键词高置信度阈值 |
| `LLM_TRIGGER_THRESHOLD` | HybridReranker | 0.45 | LLM 触发阈值 |
| `RERANK_TIMEOUT_MS` | LlmReranker | 5000 | LLM 超时时间 |

---

## 性能特征

### 典型耗时

| 场景 | 候选数 | 耗时 | 说明 |
|------|--------|------|------|
| KEYWORD_ONLY | 20 | <1ms | 纯字符串匹配 |
| 两阶段（快路径） | 20 | ~1ms | 关键词高置信度命中 |
| 两阶段（完整） | 20 | ~3-5ms | 含嵌入计算 |
| 两阶段（缓存命中） | 20 | ~1ms | 嵌入已缓存 |
| 三阶段（触发 LLM） | 20 | ~2-5s | 含 LLM 推理 |

### 内存占用

| 组件 | 内存 | 说明 |
|------|------|------|
| 嵌入缓存 | ~256KB | 128 × 512 × 4B |
| 文档频率表 | ~10KB | 每次调用重建 |
| 总计 | <300KB | 远小于 LLM 模型 |

---

## 弱网/离线优化

### 设计原则

1. **零网络依赖**：重排系统完全不依赖网络，纯本地计算
2. **渐进降级**：任一阶段失败自动降级到下一级 fallback
3. **快路径优先**：高置信度场景跳过昂贵计算
4. **缓存复用**：嵌入缓存避免重复计算

### 降级链路

```
网络状态      │ 引擎状态     │ 策略              │ 行为
─────────────┼─────────────┼──────────────────┼──────────────────────
离线          │ 引擎未就绪   │ KEYWORD_THEN_    │ 关键词 + 语义精排
              │             │  CROSS_ENCODER   │（不触发 LLM）
─────────────┼─────────────┼──────────────────┼──────────────────────
离线          │ 引擎就绪     │ KEYWORD_THEN_    │ 关键词 + 语义 + LLM
              │             │  CROSS_ENCODER_  │（LLM 可用做兜底）
              │             │  THEN_LLM        │
─────────────┼─────────────┼──────────────────┼──────────────────────
有网络        │ 引擎就绪     │ 同上             │ 同上（网络不影响重排）
─────────────┼─────────────┼──────────────────┼──────────────────────
任意          │ 语义重排关闭 │ KEYWORD_ONLY     │ 仅关键词匹配
```

---

## 文件索引

| 文件 | 角色 |
|------|------|
| [IReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/IReranker.kt) | 重排器接口 + 数据模型 |
| [KeywordReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/KeywordReranker.kt) | 关键词重排器（Stage 1） |
| [CrossEncoderReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/CrossEncoderReranker.kt) | 语义重排器（Stage 2） |
| [LlmReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/LlmReranker.kt) | LLM 重排器（Stage 3） |
| [HybridReranker.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/HybridReranker.kt) | 混合重排器（策略控制器） |
| [RerankerSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/RerankerSubsystem.kt) | 重排子系统（单例入口） |
| [AiToolSkillService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolSkillService.kt) | 技能匹配调用方 |
| [AiToolKnowledgeService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolKnowledgeService.kt) | 知识匹配调用方 |
| [ModelManager.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/ModelManager.kt) | 模型管理（含语义重排开关） |
| [AiSettingsFragment.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/ui/AiSettingsFragment.kt) | AI 设置页 UI |
