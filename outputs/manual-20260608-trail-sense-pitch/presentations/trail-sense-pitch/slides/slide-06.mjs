import { addBase, addFooter, C, arrow, body, featureCard, nodeBox, pill, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide06(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 6, "Agent Skill：把 Trail Sense 的工具能力封装成户外任务技能");
  slide.compose(layers({ width: 1280, height: 720 }, [
    strong("Skill 不是功能卡片，而是完成任务的步骤。", 76, 148, 580, 78, 36),
    body("Gemma 4 先理解用户目标，再加载相关 Skill，最后通过工具调用读取本机上下文或生成行动卡片。", 76, 240, 580, 70, 21, C.slate),
    nodeBox("Skill 知识库", "ai_tool_skills.md\n迷路、天气风险、天黑返程、求救信号", 710, 146, 260, 136, C.white, C.amber),
    nodeBox("工具知识库", "ai_tool_knowledge.md\n入口、读数、使用步骤、注意事项", 1000, 146, 260, 136, C.white, C.leaf),
    pill("loadSkill(skillName)", 128, 400, 230, C.pine),
    arrow(368, 419, 490, 419, C.moss),
    pill("runTrailSenseTool(toolId)", 500, 400, 270, C.amber, C.ink),
    arrow(780, 419, 900, 419, C.moss),
    pill("本地工具结果", 910, 400, 180, C.leaf, C.ink),
    arrow(1100, 419, 1195, 419, C.moss),
    pill("回答 + 卡片", 1020, 482, 180, C.rust),
    featureCard("示例：迷路无信号", "推荐导航、路径、信标、离线地图；必要时准备 SOS 手电筒和哨子。", 76, 520, 360, 110, C.rust),
    featureCard("示例：天气风险", "读取天气/气压上下文，结合云层观察解释风险，而不是只给泛泛户外建议。", 472, 520, 360, 110, C.amber),
    featureCard("示例：天黑返程", "把日落、路径、导航和返程提醒组织成一个行动顺序。", 868, 520, 360, 110, C.leaf),
  ]));
  addFooter(slide);
  return slide;
}
