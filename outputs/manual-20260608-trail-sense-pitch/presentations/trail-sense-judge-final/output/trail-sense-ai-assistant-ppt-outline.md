# Trail Sense AI Assistant 半决赛路演 PPT 大纲

版本定位：给评委看到的最终展示版大纲，不包含演讲提示、讲稿备注或待准备事项。

建议时长：8-10 分钟，10 页正文，加 Q&A。

## 总体叙事主线

Trail Sense 原本是一个成熟的离线户外工具箱，覆盖导航、天气、路径、信标、云层识别、生存指南和应急工具。参赛项目不是从零做一个 AI Chatbot，而是在这个完整 App 基础上增加 AI 操作层：让用户用自然语言和图片表达户外问题，由 Gemma 4 在端侧完成理解、推理和工具调度，再通过 Agent Skill 和 Native Function Calling 把已有工具组织成可执行的户外任务流程。

核心价值：在弱网、无网、紧急和读数复杂的户外场景里，把“用户自己找工具、看读数、判断下一步”升级为“用户说目标，Trail Sense 帮用户组织工具、解释读数、准备行动”。

## 幻灯片大纲

### 01. 封面：让离线户外工具箱具备端侧 AI 判断力

页面目的：建立项目定位和技术关键词，让评委一眼知道这是完整 Android App 上的端侧 AI 改造。

主标题：让离线户外工具箱具备端侧 AI 判断力

核心信息：Trail Sense AI Assistant 基于成熟离线户外 App 构建，使用 Gemma 4、LiteRT-LM、Agent Skill、Native Function Calling 和多模态输入，让户外工具箱具备自然语言理解和本地任务执行能力。

页面内容：

- 项目名称：Trail Sense AI Assistant
- 技术标签：Gemma 4、LiteRT-LM、Agent Skill、Edge AI
- 产品标签：0 云端 LLM、12+ 本地工具、完整 Android App
- Demo 链接：[youtube.com/watch?v=EkK9DF7OfXg](https://www.youtube.com/watch?v=EkK9DF7OfXg)

评委应获得的结论：这是一个可运行的端侧 AI 户外工具产品，不是单独的聊天 Demo。

### 02. 问题：户外场景不是缺 App，而是缺少低成本判断

页面目的：说明为什么需要在户外工具 App 中加入 AI 操作层。

主标题：户外场景不是缺 App，而是缺少低成本判断

核心信息：户外场景下，真正的困难不是没有工具，而是工具多、读数复杂、场景紧急、网络不可依赖，用户需要更低成本地获得下一步行动建议。

页面内容：

- 工具多：指南针、路径、天气、信标、SOS、云层、离线地图分散在不同入口。
- 读数难：气压、方向、坡度、云层变化需要经验解释。
- 场景急：迷路、天气突变、夜间返程、受伤求助时操作成本必须降低。
- 无网络：云端 AI 和在线搜索在关键时刻不可依赖。

评委应获得的结论：端侧 AI 的价值来自户外场景本身的高不确定性和低网络可靠性。

### 03. 完整产品：成熟离线户外工具箱 + AI 操作层

页面目的：从整个项目介绍，而不是只介绍 AI 助手。

主标题：完整产品：成熟离线户外工具箱 + AI 操作层

核心信息：Trail Sense 已经具备成熟的离线户外能力，AI Assistant 是建立在原有工具资产上的新交互层。

页面内容：

- 原有能力：导航与定位、天气与环境、生存与应急、天文与时间。
- 新增能力：自然语言入口、Gemma 4 本地推理、Agent Skill、Native Function Calling、图片输入和行动卡片。
- 产品关系：AI 不替代原工具，而是把原工具组织成更低成本的户外任务流程。

评委应获得的结论：项目的壁垒不只是模型接入，而是把 AI 深度接入已有户外工具体系。

### 04. 用户流程：用户说目标，Trail Sense 组织工具、解释读数、准备动作

页面目的：用一个清晰场景说明最终用户体验。

主标题：用户说目标，Trail Sense 组织工具、解释读数、准备动作

核心信息：自然语言成为户外工具箱的新入口，用户不需要先知道该打开哪个工具。

页面内容：

- 示例问题：“我迷路了，手机没信号怎么办？”
- AI 识别意图：迷路、无信号、需要定位和求救。
- 工具组合：导航、路径、信标、离线地图、SOS、哨子。
- 输出形态：行动卡片、工具入口、解释性建议、安全提示。

评委应获得的结论：AI Assistant 解决的是工具发现、读数解释和行动组织，而不是泛泛聊天。

### 05. 端侧架构：Gemma 4 本地推理接入 Trail Sense 工具调用

页面目的：展示核心技术架构，突出端侧 AI 和本地执行链路。

主标题：端侧架构：Gemma 4 本地推理接入 Trail Sense 工具调用

核心信息：用户输入在本地经过 Prompt、知识库、Gemma 4 推理和函数调用，最终触发 Trail Sense 本地工具能力。

页面内容：

- 输入层：文字、图片、App 上下文。
- AI 层：AI Assistant UI、Prompt Builder、工具知识库、Skill 知识库。
- 推理层：Gemma 4 本地 LLM、LiteRT-LM Runtime、模型管理。
- 调用层：Native Function Calling、Tool Runner、本地工具执行。
- 数据边界：模型、推理、上下文、历史记录和图片输入主要留在端侧；网络只用于模型下载等必要环节。

评委应获得的结论：这是端侧可运行的 AI 架构，具备隐私、离线和低延迟优势。

### 06. Agent Skill：把户外任务封装成可执行工作流

页面目的：突出项目不只是工具匹配，而是把复杂任务抽象为 Skill。

主标题：Agent Skill：把户外任务封装成可执行工作流

核心信息：Agent Skill 表达的是“完成一个户外任务的步骤”，不是单个功能卡片。

页面内容：

- Skill 知识库：`ai_tool_skills.md`
- 工具知识库：`ai_tool_knowledge.md`
- 调用流程：`loadSkill(skillName)` -> `runTrailSenseTool(toolId)` -> 本地工具结果 -> 回答与行动卡片。
- 示例 Skill：迷路无信号、天气风险、天黑返程。

评委应获得的结论：项目使用 Agent Skill 将 LLM 能力连接到真实户外任务流程。

### 07. 多模态输入：户外照片和 App 截图进入本地推理链路

页面目的：突出图片识别和多模态能力。

主标题：多模态输入：户外照片和 App 截图进入本地推理链路

核心信息：图片是户外场景的重要观察上下文，AI 可以结合图片、文字和工具知识做判断。

页面内容：

- 输入流程：图片选择、Bitmap 处理、Prompt 图片提示、LiteRT-LM 输入。
- 典型场景：云层识别、App 截图解释、环境观察。
- 用户价值：用户可以直接提交“看到的东西”，而不是先把环境转换成专业术语。

评委应获得的结论：项目覆盖 LLM 文本理解，也覆盖图片输入和场景识别。

### 08. 工程交付：代码、测试、APK、Demo 和官方提交材料已完成

页面目的：证明项目不是概念稿，而是可运行、可提交、可验证的工程成果。

主标题：工程交付：代码、测试、APK、Demo 和官方提交材料已完成

核心信息：项目已经形成完整 Android 工程交付，包括模型管理、推理子系统、AI 工具调用、知识库、测试、APK 和 Demo。

页面内容：

- 技术栈：Gemma 4、LiteRT-LM、Android/Kotlin、Trail Sense 本地工具体系。
- 核心模块：`AiInferenceSubsystem`、`ModelManager`、`AiAssistantTools`、`TrailSenseAiToolRunner`。
- 知识与 Prompt：`AiPromptBuilder`、`ai_tool_knowledge.md`、`ai_tool_skills.md`。
- 交付物：APK、Demo 视频、技术报告、官方提交材料。

评委应获得的结论：这是已经落地到代码和 APK 的参赛项目。

### 09. 差异化：不是云端聊天机器人，而是端侧工具型 AI

页面目的：帮助评委理解项目和普通 AI Chatbot、单点 AI 功能的区别。

主标题：差异化：不是云端聊天机器人，而是端侧工具型 AI

核心信息：Trail Sense AI Assistant 的差异来自场景、架构和交互三层。

页面内容：

- 对比普通云端 Chatbot：本项目强调端侧推理、离线使用、隐私保护和户外场景约束。
- 对比单点 AI 功能：本项目不是只做一个问答入口，而是接入完整户外工具体系。
- 对比传统工具箱：本项目用自然语言、图片输入和 Agent Skill 降低工具使用门槛。

评委应获得的结论：项目把 AI 做进了一个有明确场景、有离线价值、有真实工具资产的 App。

### 10. 结尾：让户外工具在无网时也能听懂人话

页面目的：收束价值主张，强化最终记忆点。

主标题：让户外工具在无网时也能听懂人话

核心信息：Trail Sense AI Assistant = 成熟离线户外工具箱 + Gemma 4 本地 LLM + Agent Skill + Native Function Calling + 多模态输入。

页面内容：

- 一句话总结：把户外工具箱从“功能集合”升级为“端侧 AI 户外助理”。
- 技术总结：LLM、图片识别、Agent Skill、Native Function Calling、本地工具执行。
- 产品总结：弱网可用、隐私友好、操作成本低、与真实户外工具深度结合。
- 结束页元素：Track C、Gemma 4、Trail Sense、Q&A。

评委应获得的结论：项目具备清晰场景、完整产品、端侧 AI 技术实现和可验证交付。

## Demo 可嵌入点

推荐位置：第 04 页之后或第 08 页之后。

推荐 Demo 顺序：

1. 展示 Trail Sense 原有工具入口，说明这是完整离线户外 App。
2. 打开 AI Assistant，输入迷路、天气、读数或工具查找类问题。
3. 展示 AI 返回的工具建议、行动卡片和本地工具跳转。
4. 展示图片输入或截图解释能力，突出多模态和图片识别。
5. 说明 Demo 视频地址：[youtube.com/watch?v=EkK9DF7OfXg](https://www.youtube.com/watch?v=EkK9DF7OfXg)

## 版本说明

本大纲对应当前最终版 PPT：

`outputs/manual-20260608-trail-sense-pitch/presentations/trail-sense-judge-final/output/trail-sense-ai-assistant-judge-final.pptx`

