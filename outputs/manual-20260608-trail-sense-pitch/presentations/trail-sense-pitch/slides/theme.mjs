import {
  layers as rawLayers,
  grid as rawGrid,
  card as rawCard,
  shape as rawShape,
  text as rawText,
  connector,
  stroke,
} from "@oai/artifact-tool";

export const C = {
  ink: "#10231C",
  moss: "#234D36",
  pine: "#0B3B2E",
  leaf: "#77A35D",
  amber: "#F0B35A",
  sand: "#F4E8CF",
  bone: "#FFF9EC",
  fog: "#E8EFE4",
  rust: "#C45C3B",
  sky: "#8EB6A7",
  slate: "#31463F",
  white: "#FFFFFF",
};

export const W = 1280;
export const H = 720;
export const noLine = stroke("none");
export const line = (color, width = 1) => stroke(`${width}px solid ${color}`);

function normalizePosition(props = {}) {
  const position = props.position;
  if (position && typeof position === "object" && "x" in position && "y" in position) {
    return {
      ...props,
      position: {
        left: position.x,
        top: position.y,
      },
    };
  }
  return props;
}

export const layers = (props = {}, children = []) => rawLayers(normalizePosition(props), children);
export const grid = (props = {}, children = []) => rawGrid(normalizePosition(props), children);
export const card = (props = {}, child) => rawCard(normalizePosition(props), child);
export const shape = (props = {}) => rawShape(normalizePosition(props));
export const text = (value, props = {}) => rawText(value, normalizePosition(props));

export function addBase(slide, idx, title, kicker = "Trail Sense AI Assistant") {
  slide.background.fill = C.bone;
  slide.compose(layers({ width: W, height: H }, [
    shape({
      geometry: "rect",
      position: { x: 0, y: 0 },
      width: W,
      height: H,
      fill: "linear(135deg, #FFF9EC 0%, #E8EFE4 58%, #D8E0C8 100%)",
      line: noLine,
    }),
    shape({
      geometry: "rect",
      position: { x: 0, y: 0 },
      width: 34,
      height: H,
      fill: C.pine,
      line: noLine,
    }),
    shape({
      geometry: "rect",
      position: { x: 34, y: 0 },
      width: 8,
      height: H,
      fill: C.amber,
      line: noLine,
    }),
    shape({
      geometry: "rect",
      position: { x: 72, y: 50 },
      width: 108,
      height: 6,
      fill: C.amber,
      line: noLine,
    }),
    text(kicker.toUpperCase(), {
      position: { x: 72, y: 34 },
      width: 520,
      height: 28,
      style: {
        typeface: "Avenir Next Condensed",
        fontSize: 15,
        bold: true,
        color: C.moss,
      },
    }),
    text(title, {
      position: { x: 72, y: 62 },
      width: 820,
      height: 60,
      style: {
        typeface: "Avenir Next",
        fontSize: 31,
        bold: true,
        color: C.ink,
        wrap: "square",
      },
    }),
    text(String(idx).padStart(2, "0"), {
      position: { x: 1160, y: 646 },
      width: 80,
      height: 32,
      style: {
        typeface: "Avenir Next Condensed",
        fontSize: 22,
        bold: true,
        color: C.moss,
        alignment: "right",
      },
    }),
  ]));
}

export function label(value, x, y, color = C.moss) {
  return text(value.toUpperCase(), {
    position: { x, y },
    width: 380,
    height: 24,
    style: {
      typeface: "Avenir Next Condensed",
      fontSize: 14,
      bold: true,
      color,
    },
  });
}

export function body(value, x, y, w, h, size = 23, color = C.ink) {
  return text(value, {
    position: { x, y },
    width: w,
    height: h,
    style: {
      typeface: "Avenir Next",
      fontSize: size,
      color,
      wrap: "square",
    },
  });
}

export function strong(value, x, y, w, h, size = 34, color = C.ink) {
  return text(value, {
    position: { x, y },
    width: w,
    height: h,
    style: {
      typeface: "Avenir Next",
      fontSize: size,
      bold: true,
      color,
      wrap: "square",
    },
  });
}

export function pill(value, x, y, w, fill = C.pine, color = C.white) {
  return card({
    position: { x, y },
    width: w,
    height: 38,
    padding: 0,
    fill,
    line: noLine,
    borderRadius: 19,
    align: "center",
    justify: "center",
  }, text(value, {
    width: w,
    height: 28,
    style: {
      typeface: "Avenir Next Condensed",
      fontSize: 18,
      bold: true,
      color,
      alignment: "center",
    },
  }));
}

export function metric(value, labelText, x, y, w = 225, accent = C.amber) {
  return card({
    position: { x, y },
    width: w,
    height: 132,
    padding: 18,
    fill: "rgba(255,255,255,0.72)",
    line: line(C.fog),
    borderRadius: 22,
  }, layers({ width: w - 36, height: 96 }, [
    shape({ position: { x: 0, y: 0 }, width: 46, height: 5, fill: accent, line: noLine }),
    text(value, {
      position: { x: 0, y: 18 },
      width: w - 36,
      height: 44,
      style: {
        typeface: "Avenir Next",
        fontSize: 31,
        bold: true,
        color: C.ink,
      },
    }),
    text(labelText, {
      position: { x: 0, y: 62 },
      width: w - 36,
      height: 44,
      style: {
        typeface: "Avenir Next",
        fontSize: 16,
        color: C.slate,
        wrap: "square",
      },
    }),
  ]));
}

export function featureCard(title, desc, x, y, w, h, accent = C.leaf) {
  return card({
    position: { x, y },
    width: w,
    height: h,
    padding: 18,
    fill: "rgba(255,255,255,0.78)",
    line: line(C.fog),
    borderRadius: 22,
  }, layers({ width: w - 36, height: h - 36 }, [
    shape({ position: { x: 0, y: 0 }, width: 38, height: 6, fill: accent, line: noLine }),
    text(title, {
      position: { x: 0, y: 18 },
      width: w - 36,
      height: 34,
      style: { typeface: "Avenir Next", fontSize: 22, bold: true, color: C.ink, wrap: "square" },
    }),
    text(desc, {
      position: { x: 0, y: 58 },
      width: w - 36,
      height: h - 88,
      style: { typeface: "Avenir Next", fontSize: 16, color: C.slate, wrap: "square" },
    }),
  ]));
}

export function arrow(x1, y1, x2, y2, color = C.moss) {
  const horizontal = Math.abs(x2 - x1) >= Math.abs(y2 - y1);
  if (horizontal) {
    return layers({ width: W, height: H }, [
      shape({
        position: { x: Math.min(x1, x2), y: y1 - 1.5 },
        width: Math.abs(x2 - x1),
        height: 3,
        fill: color,
        line: noLine,
      }),
      shape({
        position: { x: x2 - 5, y: y2 - 5 },
        width: 10,
        height: 10,
        geometry: "ellipse",
        fill: color,
        line: noLine,
      }),
    ]);
  }
  return layers({ width: W, height: H }, [
    shape({
      position: { x: x1 - 1.5, y: Math.min(y1, y2) },
      width: 3,
      height: Math.abs(y2 - y1),
      fill: color,
      line: noLine,
    }),
    shape({
      position: { x: x2 - 5, y: y2 - 5 },
      width: 10,
      height: 10,
      geometry: "ellipse",
      fill: color,
      line: noLine,
    }),
  ]);
}

export function nodeBox(title, subtitle, x, y, w, h, fill = C.white, accent = C.amber) {
  return card({
    position: { x, y },
    width: w,
    height: h,
    padding: 16,
    fill,
    line: line(C.fog),
    borderRadius: 18,
  }, layers({ width: w - 32, height: h - 32 }, [
    shape({ position: { x: 0, y: 0 }, width: 34, height: 5, fill: accent, line: noLine }),
    text(title, {
      position: { x: 0, y: 15 },
      width: w - 32,
      height: 24,
      style: { typeface: "Avenir Next", fontSize: 18, bold: true, color: C.ink, wrap: "square" },
    }),
    text(subtitle, {
      position: { x: 0, y: 54 },
      width: w - 32,
      height: h - 78,
      style: { typeface: "Avenir Next", fontSize: 12.5, color: C.slate, wrap: "square" },
    }),
  ]));
}

export function addFooter(slide, textValue = "Gemma 4 Hackathon Shanghai · Trail Sense AI Assistant") {
  slide.compose(layers({ width: W, height: H }, [
    text(textValue, {
      position: { x: 72, y: 652 },
      width: 700,
      height: 24,
      style: { typeface: "Avenir Next", fontSize: 13, color: C.slate },
    }),
  ]));
}

export { connector };
