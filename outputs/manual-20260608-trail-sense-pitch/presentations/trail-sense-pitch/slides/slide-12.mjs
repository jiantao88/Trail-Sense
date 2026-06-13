import { C, H, W, metric, noLine, pill, shape, text, layers } from "./theme.mjs";

export default async function slide12(presentation) {
  const slide = presentation.slides.add();
  slide.background.fill = C.pine;
  slide.compose(layers({ width: W, height: H }, [
    shape({ position: { x: 0, y: 0 }, width: W, height: H, fill: "linear(145deg, #0B3B2E 0%, #10231C 68%, #C45C3B 150%)", line: noLine }),
    shape({ position: { x: 92, y: 92 }, width: 156, height: 8, fill: C.amber, line: noLine }),
    text("最后一句话", {
      position: { x: 92, y: 120 },
      width: 260,
      height: 38,
      style: { typeface: "Avenir Next Condensed", fontSize: 24, bold: true, color: C.amber },
    }),
    text("让户外工具\n在无网时也能听懂人话", {
      position: { x: 90, y: 184 },
      width: 760,
      height: 160,
      style: { typeface: "Avenir Next", fontSize: 56, bold: true, color: C.bone, wrap: "square" },
    }),
    text("Trail Sense AI Assistant = 成熟离线户外工具箱 + Gemma 4 本地 LLM + Agent Skill + Native Function Calling + 多模态输入。", {
      position: { x: 94, y: 380 },
      width: 760,
      height: 90,
      style: { typeface: "Avenir Next", fontSize: 25, color: "#E8EFE4", wrap: "square" },
    }),
    pill("Demo: youtube.com/watch?v=EkK9DF7OfXg", 94, 530, 430, C.amber, C.ink),
    metric("Track C", "Edge AI", 890, 142, 210, C.amber),
    metric("Gemma 4", "端侧 LLM + 多模态", 1000, 302, 210, C.leaf),
    metric("Trail Sense", "完整户外工具产品", 850, 462, 250, C.sky),
    text("Q&A", {
      position: { x: 1040, y: 622 },
      width: 160,
      height: 62,
      style: { typeface: "Avenir Next", fontSize: 44, bold: true, color: C.bone, alignment: "right" },
    }),
  ]));
  return slide;
}
