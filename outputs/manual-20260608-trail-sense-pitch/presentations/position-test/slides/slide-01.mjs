import { layers, shape, text, stroke } from "@oai/artifact-tool";

export default async function testPosition(presentation) {
  const slide = presentation.slides.add();
  const noLine = stroke("none");
  slide.compose(layers({ width: 1280, height: 720 }, [
    shape({ position: "100 100", width: 80, height: 40, fill: "#ff0000", line: noLine }),
    shape({ position: "x: 220; y: 100", width: 80, height: 40, fill: "#00ff00", line: noLine }),
    shape({ position: { left: 340, top: 100 }, width: 80, height: 40, fill: "#0000ff", line: noLine }),
    shape({ position: { x: 460, y: 100 }, width: 80, height: 40, fill: "#ffaa00", line: noLine }),
    text("hello", { position: "100 200", width: 200, height: 40, style: { fontSize: 24, color: "#000000" } }),
  ]));
  return slide;
}
