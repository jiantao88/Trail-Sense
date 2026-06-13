import { addBase, addFooter, C, body, featureCard, label, metric, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide02(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 2, "先从户外用户的问题开始：不是缺工具，而是关键时刻不知道怎么判断");
  slide.compose(layers({ width: 1280, height: 720 }, [
    strong("户外场景的难点，是“信息碎片 + 高风险 + 弱网络”。", 76, 150, 620, 92, 38),
    body("Trail Sense 原本已经有大量离线工具；半决赛路演要讲清楚的是：AI 如何把这些工具组织成用户能立即执行的判断和动作。", 76, 254, 650, 92, 22, C.slate),
    metric("工具多", "天气、导航、路径、信标、云识别、生存指南、SOS、哨子等", 760, 150, 250, C.leaf),
    metric("读数难", "气压、方向、云层、日落、路径数据需要解释", 1028, 150, 250, C.amber),
    metric("场景急", "迷路、雷暴、低温、天黑返程、求救都需要低操作成本", 760, 310, 250, C.rust),
    metric("无网络", "野外弱网下，云端聊天机器人无法稳定承担核心体验", 1028, 310, 250, C.sky),
    featureCard("路演主张", "Trail Sense AI Assistant 把原本分散的离线工具变成一个能理解问题、能解释读数、能准备动作的端侧 AI 户外助手。", 76, 460, 1160, 120, C.amber),
  ]));
  addFooter(slide);
  return slide;
}
