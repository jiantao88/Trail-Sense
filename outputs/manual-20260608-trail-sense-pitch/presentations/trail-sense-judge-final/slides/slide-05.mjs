import { C, header, footer, p, node, flow, proof, layers } from "./theme.mjs";

export default async function slide05(presentation) {
  const slide = presentation.slides.add();
  header(slide, 5, "端侧架构：Gemma 4 本地推理接入 Trail Sense 工具调用");
  slide.compose(layers({ width: 1280, height: 720 }, [
    node("用户输入", "文本问题、图片、Trail Sense 截图", 78, 170, 190, 106, C.amber),
    flow(278, 222, 365, 222),
    node("AI Assistant UI", "AiAssistantFragment\n聊天、图片、历史、行动卡片", 376, 158, 226, 130, C.leaf),
    flow(612, 222, 700, 222),
    node("Prompt + Knowledge", "AiPromptBuilder\nai_tool_knowledge.md\nai_tool_skills.md", 712, 146, 240, 154, C.sky),
    flow(962, 222, 1040, 222),
    node("Gemma 4 本地 LLM", "LiteRT-LM Engine\nGemma-4-E2B-it / E4B-it\nGPU 优先，CPU 回退", 1050, 138, 230, 170, C.amber),
    node("Native Function Calling", "AiAssistantTools\nloadSkill()\nrunTrailSenseTool()", 318, 398, 246, 150, C.rust),
    node("Tool Execution", "AiToolExecutionService\nTrailSenseAiToolRunner", 612, 398, 246, 150, C.leaf),
    node("Trail Sense 本地工具", "天气、导航、路径、信标、云识别、SOS、哨子", 906, 398, 260, 150, C.sky),
    flow(1030, 310, 1030, 388),
    flow(906, 475, 868, 475),
    flow(612, 475, 574, 475),
    proof("Edge AI 价值", "模型、推理、工具上下文、聊天历史和图片输入都在设备侧完成；网络只用于首次下载模型。", 78, 535, 1088, 82, C.amber),
  ]));
  footer(slide);
  return slide;
}
