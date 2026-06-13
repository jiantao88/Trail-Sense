import { addBase, addFooter, C, body, nodeBox, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide10(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 10, "差异化：不是云端聊天机器人，而是端侧工具型 AI");
  slide.compose(layers({ width: 1280, height: 720 }, [
    strong("评委应该看到三层差异：场景、架构、交互。", 76, 148, 660, 58, 36),
    body("Trail Sense AI Assistant 的核心壁垒不是“能回答户外问题”，而是它接入了真实 App 工具、真实本地上下文和真实应急动作。", 76, 224, 670, 74, 21, C.slate),
    nodeBox("普通云端 Chatbot", "依赖网络\n不了解 App 工具\n容易泛泛建议\n不能生成本机动作", 110, 360, 300, 170, "#FFF2E8", C.rust),
    nodeBox("单独 AI 功能", "只介绍模型能力\n产品上下文弱\n难证明用户会持续使用", 490, 360, 300, 170, "#F6F3E8", C.amber),
    nodeBox("Trail Sense AI Assistant", "原工具箱 + 端侧 LLM\nAgent Skill + Tool Calling\n本地数据 + 行动卡片", 870, 340, 330, 210, "#ECF4E8", C.leaf),
    body("路演落点：把 AI 做进一个有明确场景、有离线价值、有真实工具资产的 App。", 110, 580, 1040, 46, 24, C.ink),
  ]));
  addFooter(slide);
  return slide;
}
