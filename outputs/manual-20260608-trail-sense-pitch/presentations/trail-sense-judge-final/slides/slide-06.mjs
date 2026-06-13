import { C, header, footer, h1, p, tag, proof, flow, layers } from "./theme.mjs";

export default async function slide06(presentation) {
  const slide = presentation.slides.add();
  header(slide, 6, "Agent Skill：把户外任务封装成可执行工作流");
  slide.compose(layers({ width: 1280, height: 720 }, [
    h1("Skill 表达的是“完成任务的步骤”，不是单个功能卡片。", 76, 150, 630, 76, 36),
    p("Gemma 4 先加载匹配的户外 Skill，再通过工具调用读取本机上下文或生成行动卡片。", 76, 250, 610, 56, 21),
    proof("Skill 知识库", "ai_tool_skills.md：迷路、天气风险、天黑返程、求救信号等工作流。", 720, 148, 250, 106, C.amber),
    proof("工具知识库", "ai_tool_knowledge.md：工具入口、读数解释、使用步骤和注意事项。", 1010, 148, 250, 106, C.leaf),
    tag("loadSkill(skillName)", 120, 388, 230, C.pine),
    flow(360, 406, 486, 406),
    tag("runTrailSenseTool(toolId)", 496, 388, 280, C.amber, C.ink),
    flow(786, 406, 906, 406),
    tag("本地工具结果", 916, 388, 180, C.leaf, C.ink),
    flow(1006, 434, 1006, 486),
    tag("回答 + 行动卡片", 912, 496, 220, C.rust),
    proof("迷路无信号", "导航、路径、信标、离线地图、SOS 手电筒、哨子。", 76, 540, 330, 90, C.rust),
    proof("天气风险", "天气、气压、云识别、雷电距离等组合判断。", 450, 540, 330, 90, C.amber),
    proof("天黑返程", "日落、路径、导航和返程提醒组织为行动顺序。", 824, 540, 330, 90, C.leaf),
  ]));
  footer(slide);
  return slide;
}
