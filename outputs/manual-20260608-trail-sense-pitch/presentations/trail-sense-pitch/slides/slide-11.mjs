import { addBase, addFooter, C, body, featureCard, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide11(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 11, "半决赛答辩重点：把项目从“功能”讲成“系统”");
  slide.compose(layers({ width: 1280, height: 720 }, [
    strong("建议口径：这是端侧 AI 在垂直工具 App 里的完整落地。", 76, 146, 760, 64, 36),
    featureCard("1. 完整产品", "先讲 Trail Sense 原有工具箱，再讲 AI 如何让工具箱更易用。", 76, 250, 360, 120, C.leaf),
    featureCard("2. Edge AI", "Gemma 4 + LiteRT-LM 在 Android 本地推理，弱网/无网仍能工作。", 470, 250, 360, 120, C.amber),
    featureCard("3. Agent Skill", "AI 能加载户外任务技能，并通过工具调用读取 Trail Sense 本地能力。", 864, 250, 360, 120, C.rust),
    featureCard("4. 多模态", "图片、云层、户外环境和 App 截图进入本地视觉问答链路。", 76, 420, 360, 120, C.sky),
    featureCard("5. 安全边界", "不伪造传感器读数；高风险问题强调真实装备、官方信息和救援。", 470, 420, 360, 120, C.leaf),
    featureCard("6. 可运行证明", "Demo 视频、APK、测试、技术报告、官方补充 PR 已准备。", 864, 420, 360, 120, C.amber),
  ]));
  addFooter(slide);
  return slide;
}
