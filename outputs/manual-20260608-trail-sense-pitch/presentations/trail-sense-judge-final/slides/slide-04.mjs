import { C, header, footer, h1, p, tag, proof, flow, layers } from "./theme.mjs";

export default async function slide04(presentation) {
  const slide = presentation.slides.add();
  header(slide, 4, "用户说目标，Trail Sense 组织工具、解释读数、准备动作");
  slide.compose(layers({ width: 1280, height: 720 }, [
    h1("自然语言成为户外工具箱的新入口。", 76, 155, 560, 60, 38),
    p("例如“我迷路了，手机没信号怎么办？”会触发导航、路径、信标、离线地图、SOS 和哨子等多工具工作流。", 76, 236, 590, 70, 21),
    tag("自然语言问题", 92, 445, 180, C.pine),
    flow(282, 463, 390, 463),
    tag("意图识别", 400, 445, 150, C.leaf, C.ink),
    flow(560, 463, 670, 463),
    tag("工具组合", 680, 445, 150, C.amber, C.ink),
    flow(840, 463, 950, 463),
    tag("行动卡片", 960, 445, 150, C.rust),
    proof("找工具", "根据用户目标推荐正确工具，而不是让用户翻工具列表。", 720, 154, 250, 106, C.leaf),
    proof("解释读数", "把天气、气压、导航、云层等上下文解释成实际判断。", 1010, 154, 250, 106, C.amber),
    proof("执行动作", "SOS 手电筒、哨子、打开工具等通过本机动作呈现。", 720, 290, 250, 106, C.rust),
    proof("安全边界", "不伪造传感器读数；高风险场景强调真实装备和救援渠道。", 1010, 290, 250, 106, C.sky),
    p("AI 在这里承担 Trail Sense 的“操作层”和“解释层”。", 720, 545, 560, 42, 24, C.ink),
  ]));
  footer(slide);
  return slide;
}
