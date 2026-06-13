import { addBase, addFooter, C, arrow, body, nodeBox, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide05(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 5, "系统架构：Gemma 4 运行在手机本地，工具调用进入 Trail Sense 原能力");
  slide.compose(layers({ width: 1280, height: 720 }, [
    nodeBox("用户输入", "文本问题 / 图片 / Trail Sense 截图", 78, 174, 190, 104, C.white, C.amber),
    arrow(278, 226, 365, 226, C.moss),
    nodeBox("AI Assistant UI", "AiAssistantFragment\n聊天、图片、历史、行动卡片", 376, 162, 226, 128, C.white, C.leaf),
    arrow(610, 226, 700, 226, C.moss),
    nodeBox("Prompt + Knowledge", "AiPromptBuilder\nai_tool_knowledge.md\nai_tool_skills.md", 712, 150, 240, 152, C.white, C.sky),
    arrow(960, 226, 1042, 226, C.moss),
    nodeBox("Gemma 4 本地 LLM", "LiteRT-LM Engine\nGemma-4-E2B-it / E4B-it\nGPU 优先，CPU 回退", 1050, 142, 230, 168, C.white, C.amber),
    nodeBox("Native Function Calling", "AiAssistantTools\nloadSkill()\nrunTrailSenseTool()", 318, 398, 246, 150, C.white, C.rust),
    nodeBox("Tool Execution", "AiToolExecutionService\nTrailSenseAiToolRunner", 612, 398, 246, 150, C.white, C.leaf),
    nodeBox("Trail Sense 本地工具", "天气 / 导航 / 路径 / 信标 / 云识别 / SOS / 哨子", 906, 398, 260, 150, C.white, C.sky),
    arrow(1030, 312, 1030, 388, C.moss),
    arrow(905, 475, 868, 475, C.moss),
    arrow(600, 475, 574, 475, C.moss),
    body("关键点：模型、推理、工具上下文、聊天历史和图片输入都在设备侧完成；网络只用于首次下载模型。", 80, 580, 1120, 46, 22, C.ink),
  ]));
  addFooter(slide);
  return slide;
}
