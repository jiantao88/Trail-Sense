import { addBase, addFooter, C, body, featureCard, metric, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide09(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 9, "工程验证：不是概念稿，已经有代码、APK、视频和官方提交材料");
  slide.compose(layers({ width: 1280, height: 720 }, [
    strong("半决赛要强调：这是可运行的 Android 项目。", 76, 148, 620, 60, 36),
    body("代码路径、构建命令、测试命令、Demo 视频、APK 下载和技术报告都已经整理进提交材料。", 76, 224, 590, 62, 21, C.slate),
    metric("Gemma 4", "E2B 默认模型 / E4B 可选", 730, 150, 260, C.amber),
    metric("LiteRT-LM", "本地 Engine + Conversation", 1010, 150, 260, C.leaf),
    metric("Tests", "Prompt、Skill、模型、工具服务", 730, 310, 260, C.sky),
    metric("APK + Demo", "YouTube 视频 + APK 下载", 1010, 310, 260, C.rust),
    featureCard("核心代码路径", "AiInferenceSubsystem / ModelManager / AiAssistantTools / TrailSenseAiToolRunner\nAiPromptBuilder / ai_tool_knowledge.md / ai_tool_skills.md", 76, 450, 1160, 120, C.leaf),
  ]));
  addFooter(slide);
  return slide;
}
