import { C, header, footer, h1, p, proof, metric, layers } from "./theme.mjs";

export default async function slide08(presentation) {
  const slide = presentation.slides.add();
  header(slide, 8, "工程交付：代码、测试、APK、Demo 和官方提交材料已完成");
  slide.compose(layers({ width: 1280, height: 720 }, [
    h1("这是可运行的 Android 项目，而不是概念稿。", 76, 150, 640, 72, 38),
    p("提交材料包含技术报告、Demo 视频、APK 下载、构建说明和核心代码路径。", 76, 246, 620, 56, 21),
    metric("Gemma 4", "E2B 默认模型 / E4B 可选", 740, 150, 250, C.amber),
    metric("LiteRT-LM", "本地 Engine + Conversation", 1015, 150, 250, C.leaf),
    metric("Tests", "Prompt、Skill、模型、工具服务", 740, 310, 250, C.sky),
    metric("APK + Demo", "YouTube 视频 + APK 下载", 1015, 310, 250, C.rust),
    proof("核心代码路径", "AiInferenceSubsystem / ModelManager / AiAssistantTools / TrailSenseAiToolRunner\nAiPromptBuilder / ai_tool_knowledge.md / ai_tool_skills.md", 76, 470, 1110, 100, C.leaf),
  ]));
  footer(slide);
  return slide;
}
