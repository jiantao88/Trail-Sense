import { C, W, H, bg, h1, p, tag, metric, shape, text, layers, noLine } from "./theme.mjs";

export default async function slide01(presentation) {
  const slide = presentation.slides.add();
  bg(slide, true);
  slide.compose(layers({ width: W, height: H }, [
    shape({ position: { x: 790, y: 0 }, width: 360, height: 360, geometry: "ellipse", fill: "rgba(240,179,90,0.17)", line: noLine }),
    shape({ position: { x: 890, y: 244 }, width: 340, height: 340, geometry: "ellipse", fill: "rgba(255,249,236,0.10)", line: noLine }),
    text("TRAIL SENSE AI ASSISTANT", {
      position: { x: 74, y: 62 },
      width: 460,
      height: 28,
      style: { typeface: "Avenir Next Condensed", fontSize: 21, bold: true, color: C.amber },
    }),
    h1("让离线户外工具箱\n具备端侧 AI 判断力", 74, 138, 760, 150, 54, C.bone),
    p("基于 Trail Sense 的天气、导航、路径、信标、云识别、生存指南和应急工具，接入 Gemma 4 本地推理、Agent Skill、Native Function Calling 与多模态输入。", 78, 330, 710, 92, 24, "#E8EFE4"),
    tag("Gemma 4 · LiteRT-LM · Agent Skill · Edge AI", 78, 470, 440, C.amber, C.ink),
    metric("0 云端 LLM", "推理和工具上下文在本机完成", 78, 538, 250, C.amber),
    metric("12+ 工具", "接入原 Trail Sense 户外能力", 354, 538, 250, C.leaf),
    metric("完整 App", "不是单点 AI demo", 630, 538, 250, C.sky),
    text("半决赛路演最终版", {
      position: { x: 950, y: 620 },
      width: 220,
      height: 30,
      style: { typeface: "Avenir Next Condensed", fontSize: 18, bold: true, color: C.bone, alignment: "right" },
    }),
  ]));
  return slide;
}
