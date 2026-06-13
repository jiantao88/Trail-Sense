import { C, header, footer, h1, p, proof, layers } from "./theme.mjs";

export default async function slide09(presentation) {
  const slide = presentation.slides.add();
  header(slide, 9, "差异化：不是云端聊天机器人，而是端侧工具型 AI");
  slide.compose(layers({ width: 1280, height: 720 }, [
    h1("差异来自场景、架构和交互三层。", 76, 150, 620, 58, 38),
    p("Trail Sense AI Assistant 的核心价值不是“能回答户外问题”，而是接入真实 App 工具、真实本地上下文和真实应急动作。", 76, 232, 690, 76, 21),
    proof("普通云端 Chatbot", "依赖网络；不了解 App 工具；容易泛泛建议；不能生成本机动作。", 96, 360, 310, 160, C.rust),
    proof("单点 AI 功能", "只展示模型能力；产品上下文弱；难证明用户会持续使用。", 480, 360, 310, 160, C.amber),
    proof("Trail Sense AI Assistant", "原工具箱 + 端侧 LLM；Agent Skill + Tool Calling；本地数据 + 行动卡片。", 864, 340, 340, 200, C.leaf),
    p("把 AI 做进一个有明确场景、有离线价值、有真实工具资产的 App。", 96, 584, 980, 42, 24, C.ink),
  ]));
  footer(slide);
  return slide;
}
