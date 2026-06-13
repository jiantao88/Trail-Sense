import { C, header, footer, h1, p, metric, proof, layers } from "./theme.mjs";

export default async function slide02(presentation) {
  const slide = presentation.slides.add();
  header(slide, 2, "户外场景不是缺 App，而是缺少低成本判断");
  slide.compose(layers({ width: 1280, height: 720 }, [
    h1("弱网、读数和紧急情境叠加时，工具箱需要变成决策入口。", 76, 160, 700, 95, 38),
    p("Trail Sense 原本已经有大量离线工具；AI 的价值是把分散工具、传感器读数和应急动作组织为用户能执行的下一步。", 76, 275, 660, 72, 21),
    metric("工具多", "天气、导航、路径、信标、云识别、SOS", 790, 160, 235, C.leaf),
    metric("读数难", "气压、方向、云层、日落、路径数据", 1045, 160, 235, C.amber),
    metric("场景急", "迷路、雷暴、低温、天黑、求救", 790, 320, 235, C.rust),
    metric("无网络", "云端 AI 无法承担核心体验", 1045, 320, 235, C.sky),
    proof("核心机会", "把 Trail Sense 从“用户自己查工具”升级为“用户说目标，系统组织工具、解释读数、准备动作”。", 76, 480, 1110, 110, C.amber),
  ]));
  footer(slide);
  return slide;
}
