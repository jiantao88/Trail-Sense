# Trail Sense AI Assistant

> 基于开源户外工具 App [Trail Sense](https://github.com/kylecorry31/Trail-Sense) 扩展的端侧 AI 助手项目。  
> 本项目不是从零重写 App，而是在原有 Trail Sense 的离线户外工具、传感器能力和生存指南基础上，新增了一个使用 Gemma 4 的本地 AI Assistant。

[![License](https://img.shields.io/:license-mit-blue.svg?style=flat-square)](https://badges.mit-license.org)

## 项目定位

Trail Sense 原本是一个面向徒步、露营、地理定位和生存场景的 Android 工具箱，核心能力包括天气预测、导航、路径记录、信标、指南针、云识别、生存指南、手电筒、哨子、天文信息等。

本项目在这个原有 App 之上增加了 AI Assistant，让用户可以用自然语言询问：

- 我现在应该使用哪个 Trail Sense 工具？
- 这个天气、气压、导航或传感器读数是什么意思？
- 遇到迷路、失温、雷暴、天黑前返程等情况应该怎么处理？
- 如何快速打开 SOS 手电筒、哨子等应急功能？
- 如何根据当前 App 内上下文得到更具体的户外建议？

项目目标不是做一个通用聊天机器人，而是做一个理解 Trail Sense 工具体系、能调用本地工具、能解释户外场景数据的端侧 AI 助手。

## 与原 Trail Sense 的关系

本仓库是 [kylecorry31/Trail-Sense](https://github.com/kylecorry31/Trail-Sense) 的 fork。

保留的原 App 能力：

- 离线优先的户外工具箱体验
- 天气、气压、导航、路径、信标、天文、云识别、生存指南等核心功能
- 手机传感器驱动的实用工具
- 本地 SQLite / SharedPreferences 数据存储
- 原有 Android 架构、Compose/XML 混合界面和工具注册体系

新增的 AI 能力：

- 新增 `AI Assistant` 工具入口
- 新增 Gemma 4 模型下载、选择和本地加载能力
- 新增 LiteRT-LM 端侧推理封装
- 新增 Native Function Calling / Tool Calling，让模型可以调用 Trail Sense 本地工具
- 新增 AI 工具知识库和技能库，让模型知道每个工具能做什么、在哪里打开、读数如何解释
- 新增聊天记录、图片附件和应急行动卡片 UI

## 使用的 AI 技术

本项目参考 Gemma 4 Gallery App 能展示的几类核心能力，把它们落到 Trail Sense 的户外工具场景中：

- **LLM 本地问答**：使用 Gemma 4 在手机端进行自然语言理解、工具推荐、读数解释和户外安全问答。
- **图片识别 / 多模态输入**：支持在聊天中附加图片，为云层识别、户外环境观察和视觉问答提供入口。
- **Agent Skill**：把 Trail Sense 的户外工具封装成可被 AI 理解和调度的技能，例如天气、导航、路径、信标、SOS 手电筒和哨子。
- **Native Function Calling / Tool Calling**：让 Gemma 4 不只是生成文本，而是可以调用 App 内本地工具读取上下文或生成行动卡片。
- **Edge AI**：模型下载后在 Android 设备本地运行，不依赖云端 LLM API。

### LLM：Gemma 4 本地大语言模型

默认模型：

- `Gemma-4-E2B-it`

可选模型：

- `Gemma-4-E4B-it`

模型通过 Hugging Face 下载为 LiteRT-LM 可加载的本地模型文件。首次下载需要网络，下载完成后推理在设备本地运行。

Gemma 4 在本项目中承担 LLM 能力：

- 理解用户自然语言问题
- 判断用户意图是找工具、解释读数、执行应急动作还是询问户外安全知识
- 基于系统 Prompt、工具知识库和 App 上下文生成回答
- 在需要时触发工具调用，而不是只返回泛泛建议

### 推理运行时

项目使用 Google AI Edge 的 LiteRT-LM Android Runtime：

```kotlin
implementation(libs.litertlm)
```

核心封装位于：

- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiInferenceSubsystem.kt`

该模块负责：

- 初始化 LiteRT-LM `Engine`
- 优先使用 GPU backend，失败后回退 CPU backend
- 创建带 system instruction 和 tools 的 Conversation
- 发送文本和图片输入
- 控制 token、采样参数和图片压缩尺寸

### 图片识别 / 多模态输入

Gemma 4 Gallery App 中的一个重要能力是多模态输入。本项目把这个能力迁移到 Trail Sense 的户外场景中：AI Assistant 聊天界面支持附加图片，并在推理层把图片压缩后传入 LiteRT-LM Conversation。

相关代码：

- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/ui/AiAssistantFragment.kt`
- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiInferenceSubsystem.kt`

可支持的场景包括：

- 上传天空或云层图片，让 AI 辅助解释云层和天气风险
- 上传户外环境照片，让 AI 结合 Trail Sense 工具给出观察建议
- 为后续物种、地形、天气迹象等视觉识别工作流预留入口

当前实现重点是完成图片输入链路和端侧多模态推理接口，具体识别准确度仍取决于所选 Gemma 4 模型和设备性能。

### 模型管理

模型下载和本地文件管理位于：

- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/ModelManager.kt`

它负责：

- Gemma 4 模型列表
- 默认模型选择
- 断点续传下载
- 本地模型路径管理
- 模型删除和磁盘大小统计

### Agent Skill：把 Trail Sense 工具变成 AI 技能

Gemma 4 Gallery App 展示的 Agent Skill 能力，本项目用 Trail Sense 的真实工具来实现：AI 不只是回答“你可以试试某某工具”，而是知道每个工具能解决什么问题、入口在哪里、需要什么参数、是否能读取当前上下文。

Agent Skill 相关资源：

- `app/src/main/res/raw/ai_tool_skills.md`
- `app/src/main/res/raw/ai_tool_knowledge.md`
- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolSkillMatcher.kt`
- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolKnowledgeMatcher.kt`

目前封装的 Skill 类型包括：

- 工具发现：根据用户目标推荐正确的 Trail Sense 工具
- 工具使用说明：解释某个工具怎么打开、怎么操作
- 读数解释：解释天气、导航、气压、云层等上下文
- 应急操作：准备 SOS 手电筒、哨子等行动卡片
- 安全约束：在高风险场景提醒用户使用真实装备、官方信息或救援渠道

### Native Function Calling / Tool Calling

本项目的重点不是简单 Prompt 工程，而是让 Gemma 4 可以通过工具调用访问 Trail Sense 的本地能力。

相关代码：

- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiAssistantTools.kt`
- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/TrailSenseAiToolRunner.kt`
- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiToolExecutionService.kt`

AI 可以调用的能力包括：

- 查询天气和气压上下文
- 查询导航、路径、信标等户外定位上下文
- 查询云识别和云层观察信息
- 准备 SOS 手电筒行动卡片
- 准备哨子求救行动卡片
- 返回打开某个 Trail Sense 工具的导航动作
- 根据工具知识库推荐正确工具

工具调用只读取本机 App 数据或准备本机动作，不会把位置、路径、图片或聊天内容上传到云端模型服务。

### 知识库与 Prompt

AI Assistant 会结合 App 内置知识库生成系统上下文：

- `app/src/main/res/raw/ai_tool_knowledge.md`
- `app/src/main/res/raw/ai_tool_skills.md`
- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiPromptBuilder.kt`
- `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolKnowledgeService.kt`

这些内容用于告诉模型：

- Trail Sense 有哪些工具
- 每个工具适合解决什么问题
- 工具在 App 中的入口
- 关键读数的含义
- 户外安全场景下应该如何表达不确定性
- 什么时候必须建议用户使用真实装备、官方信息或专业救援

## AI Assistant 可以实现的功能

### 1. 自然语言找工具

用户可以直接问：

```text
我想知道快天黑了还能走多久，应该用哪个工具？
```

AI 会根据工具知识库推荐日落提醒、导航、路径或天气相关工具，并解释如何打开。

### 2. 解释传感器和户外读数

AI 可以结合 Trail Sense 的本地上下文解释：

- 气压变化可能代表什么
- 当前天气趋势是否需要关注
- 导航方向和路径信息如何理解
- 云层观察和天气风险之间的关系

AI 不会伪造传感器读数；如果没有本地数据，会明确说明限制。

### 3. 应急行动卡片

当用户输入：

```text
打开 SOS 手电筒
```

或：

```text
用哨子求救
```

AI 可以通过工具调用生成行动卡片，让用户直接打开对应 Trail Sense 工具并执行本机动作。

### 4. 户外安全问答

AI 可以回答与徒步、生存、天气、导航相关的问题，同时结合 Trail Sense 的工具体系给出实际操作路径。

例如：

- 迷路时优先做什么？
- 雷暴天气如何判断风险？
- 没有网络时如何使用离线工具？
- 如何使用路径和信标回到安全位置？

### 5. 图片输入入口

聊天界面支持附加图片，为云识别、户外视觉问答和多模态工作流预留入口。

## 隐私与端侧部署

本项目采用端侧 AI 方案：

- Gemma 4 模型下载后在 Android 设备本地运行
- 聊天记录保存在本地数据库
- 位置、路径、天气、气压等数据默认只在本机使用
- 图片附件进入本机推理流程
- 不调用云端 LLM API

网络只用于首次下载模型。核心 Trail Sense 工具仍保持离线优先。

## 关键代码位置

| 模块 | 路径 |
|---|---|
| AI 助手界面 | `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/ui/AiAssistantFragment.kt` |
| AI 设置页 | `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/ui/AiSettingsFragment.kt` |
| LiteRT-LM 推理封装 | `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiInferenceSubsystem.kt` |
| Gemma 4 模型管理 | `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/ModelManager.kt` |
| Tool Calling 注册 | `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiAssistantTools.kt` |
| Trail Sense 工具执行 | `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/TrailSenseAiToolRunner.kt` |
| Prompt 构建 | `app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiPromptBuilder.kt` |
| 工具知识库 | `app/src/main/res/raw/ai_tool_knowledge.md` |
| 工具技能库 | `app/src/main/res/raw/ai_tool_skills.md` |

## 构建与运行

### 环境

- Android Studio Narwhal 或更新版本
- JDK 21
- Android SDK
- Android 设备或模拟器

### 构建 Debug APK

```bash
./gradlew :app:assembleDebug
```

输出路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 构建 Release APK

```bash
./gradlew :app:assembleRelease
```

输出路径：

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

注意：当前 `release` build type 没有配置签名 key，因此生成的是 unsigned APK。若要直接安装录制演示视频，请使用已签名的 debug/staging 构建，或先对 release APK 签名。

### 运行测试

```bash
./gradlew :app:testDebugUnitTest
```

已覆盖的 AI 相关测试包括：

- Prompt 构建
- 工具知识解析和匹配
- 工具技能解析和匹配
- 模型管理
- 工具执行服务
- 天气上下文 provider

## Demo 与提交材料

黑客松提交材料位于：

```text
submissions/2026/C/Trail-Sense-AI-Assistant/
```

Demo 目录：

```text
submissions/2026/C/Trail-Sense-AI-Assistant/demo/
```

当前 APK：

```text
submissions/2026/C/Trail-Sense-AI-Assistant/demo/trail-sense-ai-assistant-release-unsigned.apk
```

Demo 视频：

```text
submissions/2026/C/Trail-Sense-AI-Assistant/demo/trail-sense-ai-assistant-demo.mp4
```

在线视频链接：

- Demo 视频（YouTube）：https://www.youtube.com/watch?v=EkK9DF7OfXg
- Demo 视频备用下载：https://raw.githubusercontent.com/jiantao88/Trail-Sense/feature/ai-assistant/submissions/2026/C/Trail-Sense-AI-Assistant/demo/trail-sense-ai-assistant-demo.mp4
- Release APK：https://raw.githubusercontent.com/jiantao88/Trail-Sense/feature/ai-assistant/submissions/2026/C/Trail-Sense-AI-Assistant/demo/trail-sense-ai-assistant-release-unsigned.apk

APK SHA256：

```text
c9495f5df919f878fa6a4acd9002d1519245be3dd6a30ec1b170452938db1069
```

Demo 视频 SHA256：

```text
016255515b17f18a6e9a8f9a6c7b13c0b8dc66a1641e8b8062c935fdc05a29a6
```

## 安全说明

Trail Sense 和本项目的 AI Assistant 都只能作为辅助工具使用。户外活动中仍应携带真实地图、指南针、照明、通信、保暖和急救装备。

AI 输出可能不完整或不适用于所有环境。涉及生命安全、天气预警、救援、医疗或导航决策时，应优先使用官方信息、专业救援和可靠装备。

## 开源与许可

本项目基于 Trail Sense 扩展开发。原项目由 Kyle Corry 维护，采用 MIT License。

- 上游项目：https://github.com/kylecorry31/Trail-Sense
- 原项目贡献者：https://github.com/kylecorry31/Trail-Sense/graphs/contributors
- 许可协议：[MIT license](LICENSE)
