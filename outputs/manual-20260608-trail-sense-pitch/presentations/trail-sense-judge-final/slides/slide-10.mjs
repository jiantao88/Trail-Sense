import { C, W, H, bg, h1, p, tag, metric, shape, text, layers, noLine } from "./theme.mjs";

export default async function slide10(presentation) {
  const slide = presentation.slides.add();
  bg(slide, true);
  slide.compose(layers({ width: W, height: H }, [
    shape({ position: { x: 92, y: 92 }, width: 140, height: 8, fill: C.amber, line: noLine }),
    text("FINAL CLAIM", {
      position: { x: 92, y: 120 },
      width: 240,
      height: 32,
      style: { typeface: "Avenir Next Condensed", fontSize: 24, bold: true, color: C.amber },
    }),
    h1("让户外工具\n在无网时也能听懂人话", 90, 184, 760, 150, 56, C.bone),
    p("Trail Sense AI Assistant = 成熟离线户外工具箱 + Gemma 4 本地 LLM + Agent Skill + Native Function Calling + 多模态输入。", 94, 378, 760, 82, 25, "#E8EFE4"),
    tag("Demo: youtube.com/watch?v=EkK9DF7OfXg", 94, 530, 430, C.amber, C.ink),
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
