import { C, H, W, metric, noLine, pill, shape, text, layers } from "./theme.mjs";

export default async function slide01(presentation) {
  const slide = presentation.slides.add();
  slide.background.fill = C.bone;
  slide.compose(layers({ width: W, height: H }, [
    shape({ position: { x: 0, y: 0 }, width: W, height: H, fill: "linear(135deg, #10231C 0%, #234D36 48%, #F0B35A 145%)", line: noLine }),
    shape({ position: { x: 770, y: 0 }, width: 360, height: 360, geometry: "ellipse", fill: "rgba(240,179,90,0.18)", line: noLine }),
    shape({ position: { x: 890, y: 220 }, width: 360, height: 360, geometry: "ellipse", fill: "rgba(255,249,236,0.10)", line: noLine }),
    text("TRAIL SENSE", {
      position: { x: 74, y: 58 },
      width: 420,
      height: 34,
      style: { typeface: "Avenir Next Condensed", fontSize: 22, bold: true, color: C.amber },
    }),
    text("把离线户外工具箱升级为\n端侧 AI 生存助手", {
      position: { x: 72, y: 138 },
      width: 780,
      height: 170,
      style: { typeface: "Avenir Next", fontSize: 54, bold: true, color: C.bone, wrap: "square" },
    }),
    text("不是只加一个聊天窗口，而是让 Gemma 4 理解整个 Trail Sense 工具体系：天气、导航、路径、信标、云识别、SOS、哨子和生存指南。", {
      position: { x: 76, y: 330 },
      width: 660,
      height: 86,
      style: { typeface: "Avenir Next", fontSize: 24, color: "#E8EFE4", wrap: "square" },
    }),
    pill("Gemma 4 · LiteRT-LM · Agent Skill · Edge AI", 76, 456, 430, C.amber, C.ink),
    metric("0 云端 LLM", "位置、路径、图片和聊天内容默认留在本机", 76, 520, 240, C.amber),
    metric("12+ 工具", "天气、导航、路径、信标、云识别、SOS 等原 App 能力", 340, 520, 270, C.leaf),
    metric("半决赛路演", "项目叙事从完整 Trail Sense 产品出发", 634, 520, 270, C.sky),
    text("Gemma 4 Hackathon Shanghai · Track C Edge AI", {
      position: { x: 920, y: 665 },
      width: 360,
      height: 30,
      style: { typeface: "Avenir Next Condensed", fontSize: 18, bold: true, color: "#FFF9EC", alignment: "right" },
    }),
  ]));
  return slide;
}
