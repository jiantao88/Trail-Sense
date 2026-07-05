# 【硬件交互赛道】Trail Sense AI Assistant Demo —— 离线户外工具箱的端侧 AI 向导

## 1. Demo 简介

Trail Sense AI Assistant 是一个 Android App 内的端侧 AI 助手，面向徒步者、露营者、地理寻宝玩家、户外新人和无网络环境下需要导航/天气/应急辅助的人。

它基于开源户外工具 App Trail Sense 扩展开发，在原有指南针、天气、气压计、路径、信标、云识别、生存指南、手电筒、哨子等离线工具之上，接入 Gemma 4 + LiteRT-LM 本地推理能力，并新增端侧重排方案，让 AI 在离线环境下也能更准确地匹配工具、技能和知识。模型下载后，用户可以直接用自然语言询问“我迷路了该先做什么”“天气读数怎么看”“快天黑了还能不能继续走”“怎么打开 SOS 手电筒”。

核心功能：

- 自然语言找工具：根据用户目标推荐 Trail Sense 内置工具和使用顺序。
- 端侧重排：通过关键词召回、语义精排和可选 LLM 兜底，在本机完成工具/知识匹配。
- 解释本地读数：结合天气、气压、导航、路径、信标、云层等上下文解释当前风险。
- Native Function Calling：调用本地工具读取上下文，或准备 SOS 手电筒、哨子、打开工具等行动卡片。
- 端侧隐私：位置、路径、气压、图片和聊天内容默认只在本机使用，不上传云端 AI 服务。

## 2. Demo 创作思路

灵感来自户外场景里的一个真实问题：App 里有很多工具，但人在迷路、天黑、天气变化或紧急求助时，往往没时间慢慢找菜单，也不一定能理解气压、方位、路径等传感器读数。

我没有做一个通用聊天机器人，而是把 AI 放进 Trail Sense 的真实工具体系里，让它成为“离线户外工具向导”。它的价值不是替代真实地图、指南针或专业救援，而是在弱网/无网环境下帮助用户更快找到正确工具、理解本机传感器上下文，并按更清晰的步骤行动。

技术取舍：

- 选择 Gemma 4 本地模型，而不是云端 LLM，优先保证户外无网络可用和隐私。
- 增加 on-device reranking，不依赖云端向量库或远程检索服务，保证工具匹配也能离线运行。
- 复用 Trail Sense 已有工具和传感器能力，不重新造导航、天气或求救系统。
- 通过 Native Function Calling 连接 AI 与本地工具，让回答尽量基于本机上下文。
- 在高风险场景保留安全边界：AI 只能辅助判断，必须提醒用户优先使用可靠装备、官方信息和专业救援。

## 3. Demo 体验地址

硬件交互赛道用演示视频替代在线体验：

- Demo 视频：https://www.youtube.com/watch?v=EkK9DF7OfXg
- Demo 视频备用下载：https://raw.githubusercontent.com/jiantao88/Trail-Sense/feature/ai-assistant/submissions/2026/C/Trail-Sense-AI-Assistant/demo/trail-sense-ai-assistant-demo.mp4
- 项目仓库：https://github.com/jiantao88/Trail-Sense/tree/feature/ai-assistant
- APK 下载：https://raw.githubusercontent.com/jiantao88/Trail-Sense/feature/ai-assistant/submissions/2026/C/Trail-Sense-AI-Assistant/demo/trail-sense-ai-assistant-release-unsigned.apk

文件校验：

- Demo 视频 SHA256：`016255515b17f18a6e9a8f9a6c7b13c0b8dc66a1641e8b8062c935fdc05a29a6`
- APK SHA256：`c9495f5df919f878fa6a4acd9002d1519245be3dd6a30ec1b170452938db1069`

## 4. TRAE 实践过程

本 Demo 的开发过程围绕“让 Trail Sense 的本地工具能被端侧 AI 理解和调用”展开。

关键步骤 1：梳理 Trail Sense 工具体系  
先把天气、导航、路径、信标、云识别、SOS 手电筒、哨子、生存指南等工具整理成 AI 可理解的工具知识库和技能库，明确每个工具解决什么问题、入口在哪里、关键读数如何解释。

关键步骤 2：接入 Gemma 4 端侧推理  
使用 LiteRT-LM Android Runtime 封装本地推理层，支持模型下载、选择、本地路径管理、GPU 优先和 CPU 回退。模型下载完成后，核心推理在 Android 设备本地运行。

关键步骤 3：设计 on-device reranking  
新增端侧重排设计：先用 KeywordReranker 做关键词召回，再用 CrossEncoderReranker 做 TF-IDF 加权的字符 n-gram 哈希嵌入精排，低置信度时可用 LlmReranker 兜底。默认策略是关键词召回 + 语义精排，典型 20 个候选完整重排约 3-5ms，缓存命中约 1ms，总内存占用小于 300KB。

关键步骤 4：实现 Native Function Calling  
注册 `loadSkill` 和 `runTrailSenseTool` 两类工具调用，让 AI 可以先加载户外任务工作流，再调用 Trail Sense 本地工具读取上下文或准备行动卡片。

关键步骤 5：完成 AI Assistant 交互界面  
在 App 内新增 AI Assistant 工具入口，支持聊天、图片附件、历史记录、建议问题、模型状态和应急行动卡片展示。

关键代码位置：

- AI 助手界面：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/ui/AiAssistantFragment.kt`
- 模型管理：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/ModelManager.kt`
- LiteRT-LM 推理封装：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiInferenceSubsystem.kt`
- Function Calling 注册：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiAssistantTools.kt`
- Trail Sense 工具执行：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/TrailSenseAiToolRunner.kt`
- Prompt 构建：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiPromptBuilder.kt`
- 工具知识库：`app/src/main/res/raw/ai_tool_knowledge.md`
- 工具技能库：`app/src/main/res/raw/ai_tool_skills.md`
- 端侧重排方案：`docs/ai-assistant/on-device-reranking.md`
- 混合重排器：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/HybridReranker.kt`
- 语义重排器：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/CrossEncoderReranker.kt`
- 重排子系统：`app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/RerankerSubsystem.kt`

开发关键步骤截图：

1. TRAE 中梳理工具知识库 / Skill 的任务过程
2. TRAE 中实现 Gemma 4 / LiteRT-LM 推理封装的任务过程
3. TRAE 中补充端侧重排方案和 Native Function Calling 的任务过程

关键任务 Session ID：

1. `1942216060962768:e5c597ca727d786c90e91fd44eb7b905_6a462b34dcf077e47603625a.6a462b34dcf077e47603625d.6a462b34dcf077e47603625b:TRAE Work CN.0.1.29.no_sid.no_ppe.T(2026/7/2 17:11:16)`
2. `1942216060962768:99073c698800511652c437d465eb54c4_6a462b34dcf077e47603625a.6a462cc7dcf077e4760362ad.6a462cc7dcf077e4760362ab:TRAE Work CN.0.1.29.no_sid.no_ppe.T(2026/7/2 17:17:59)`
3. `1942216060962768:1750d58b722d92e76b2ac941325a0f83_6a462b34dcf077e47603625a.6a48d06492986ac69f2d3f82.6a48d06492986ac69f2d3f80:TRAE Work CN.0.1.29.no_sid.no_ppe.T(2026/7/4 17:20:36)`

## 报名帖链接

https://forum.trae.cn/t/topic/42937

## 补充说明

Trail Sense AI Assistant 是辅助工具，不替代真实地图、指南针、官方天气信息、专业救援和必要户外装备。涉及生命安全、天气预警、救援或导航决策时，应优先使用可靠装备、官方信息和专业救援渠道。
