import { addBase, addFooter, C, arrow, body, nodeBox, pill, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide04(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 4, "AI 的角色：把“工具列表”变成“可执行户外工作流”");
  slide.compose(layers({ width: 1280, height: 720 }, [
    strong("用户不需要先知道工具名。", 76, 150, 480, 52, 38),
    body("例如“我迷路了，手机没信号怎么办？”不是一个聊天问题，而是一个多工具协同问题：定位、路径、信标、离线地图、求救信号和安全提醒需要按顺序组织。", 76, 218, 560, 92, 21, C.slate),
    pill("自然语言问题", 90, 440, 180, C.pine),
    arrow(280, 459, 390, 459, C.moss),
    pill("意图识别", 400, 440, 150, C.leaf, C.ink),
    arrow(560, 459, 670, 459, C.moss),
    pill("工具组合", 680, 440, 150, C.amber, C.ink),
    arrow(840, 459, 950, 459, C.moss),
    pill("行动卡片", 960, 440, 150, C.rust),
    nodeBox("找工具", "根据目标推荐正确工具，而不是让用户翻工具列表。", 720, 150, 260, 112, C.white, C.leaf),
    nodeBox("解释读数", "把气压、天气、导航、云层等上下文解释成实际判断。", 1010, 150, 260, 112, C.white, C.amber),
    nodeBox("执行动作", "SOS 手电筒、哨子、打开工具等通过本机动作呈现。", 720, 285, 260, 112, C.white, C.rust),
    nodeBox("安全边界", "不伪造传感器读数；高风险场景提醒真实装备和救援渠道。", 1010, 285, 260, 112, C.white, C.sky),
    body("路演表达：AI 是 Trail Sense 的“操作层”和“解释层”，不是替代原 App。", 720, 525, 520, 70, 24, C.ink),
  ]));
  addFooter(slide);
  return slide;
}
