import {
  layers as rawLayers,
  card as rawCard,
  shape as rawShape,
  text as rawText,
  stroke,
} from "@oai/artifact-tool";

export const W = 1280;
export const H = 720;

export const C = {
  pine: "#0B3328",
  moss: "#1F5A3E",
  leaf: "#78A85B",
  amber: "#F0B35A",
  rust: "#C85F3D",
  sand: "#F7EDD8",
  bone: "#FFF9EC",
  fog: "#E8EFE4",
  sky: "#83B2A3",
  ink: "#10231C",
  slate: "#31463F",
  white: "#FFFFFF",
};

export const noLine = stroke("none");
export const line = (color, width = 1) => stroke(`${width}px solid ${color}`);

function pos(props = {}) {
  const p = props.position;
  if (p && typeof p === "object" && "x" in p && "y" in p) {
    return { ...props, position: { left: p.x, top: p.y } };
  }
  return props;
}

export const layers = (props = {}, children = []) => rawLayers(pos(props), children);
export const card = (props = {}, child) => rawCard(pos(props), child);
export const shape = (props = {}) => rawShape(pos(props));
export const text = (value, props = {}) => rawText(value, pos(props));

export function bg(slide, dark = false) {
  slide.background.fill = dark ? C.pine : C.bone;
  slide.compose(layers({ width: W, height: H }, [
    shape({
      position: { x: 0, y: 0 },
      width: W,
      height: H,
      fill: dark
        ? "linear(135deg, #0B3328 0%, #10231C 70%, #C85F3D 160%)"
        : "linear(135deg, #FFF9EC 0%, #E8EFE4 62%, #D9E3CE 100%)",
      line: noLine,
    }),
    shape({ position: { x: 0, y: 0 }, width: 34, height: H, fill: dark ? C.amber : C.pine, line: noLine }),
    shape({ position: { x: 34, y: 0 }, width: 8, height: H, fill: dark ? C.fog : C.amber, line: noLine }),
  ]));
}

export function header(slide, n, title, dark = false) {
  bg(slide, dark);
  slide.compose(layers({ width: W, height: H }, [
    text("TRAIL SENSE AI ASSISTANT", {
      position: { x: 72, y: 38 },
      width: 420,
      height: 24,
      style: {
        typeface: "Avenir Next Condensed",
        fontSize: 15,
        bold: true,
        color: dark ? C.amber : C.moss,
      },
    }),
    shape({ position: { x: 72, y: 60 }, width: 118, height: 6, fill: C.amber, line: noLine }),
    text(title, {
      position: { x: 72, y: 74 },
      width: 860,
      height: 76,
      style: {
        typeface: "Avenir Next",
        fontSize: 32,
        bold: true,
        color: dark ? C.bone : C.ink,
        wrap: "square",
      },
    }),
    text(String(n).padStart(2, "0"), {
      position: { x: 1160, y: 648 },
      width: 72,
      height: 30,
      style: {
        typeface: "Avenir Next Condensed",
        fontSize: 22,
        bold: true,
        color: dark ? C.bone : C.moss,
        alignment: "right",
      },
    }),
  ]));
}

export function h1(value, x, y, w, h, size = 42, color = C.ink) {
  return text(value, {
    position: { x, y },
    width: w,
    height: h,
    style: { typeface: "Avenir Next", fontSize: size, bold: true, color, wrap: "square" },
  });
}

export function p(value, x, y, w, h, size = 20, color = C.slate) {
  return text(value, {
    position: { x, y },
    width: w,
    height: h,
    style: { typeface: "Avenir Next", fontSize: size, color, wrap: "square" },
  });
}

export function tag(value, x, y, w, fill = C.pine, color = C.white) {
  return card({
    position: { x, y },
    width: w,
    height: 36,
    padding: 0,
    fill,
    line: noLine,
    borderRadius: 18,
    align: "center",
    justify: "center",
  }, text(value, {
    width: w,
    height: 24,
    style: {
      typeface: "Avenir Next Condensed",
      fontSize: 17,
      bold: true,
      color,
      alignment: "center",
    },
  }));
}

export function proof(title, desc, x, y, w, h, accent = C.leaf) {
  return card({
    position: { x, y },
    width: w,
    height: h,
    padding: 18,
    fill: "rgba(255,255,255,0.80)",
    line: line("#DDE6D8"),
    borderRadius: 22,
  }, layers({ width: w - 36, height: h - 36 }, [
    shape({ position: { x: 0, y: 0 }, width: 44, height: 5, fill: accent, line: noLine }),
    text(title, {
      position: { x: 0, y: 18 },
      width: w - 36,
      height: 34,
      style: { typeface: "Avenir Next", fontSize: 22, bold: true, color: C.ink, wrap: "square" },
    }),
    text(desc, {
      position: { x: 0, y: 58 },
      width: w - 36,
      height: Math.max(28, h - 86),
      style: { typeface: "Avenir Next", fontSize: 15.5, color: C.slate, wrap: "square" },
    }),
  ]));
}

export function metric(value, label, x, y, w = 240, accent = C.amber) {
  return card({
    position: { x, y },
    width: w,
    height: 124,
    padding: 18,
    fill: "rgba(255,255,255,0.82)",
    line: line("#DDE6D8"),
    borderRadius: 22,
  }, layers({ width: w - 36, height: 88 }, [
    shape({ position: { x: 0, y: 0 }, width: 45, height: 5, fill: accent, line: noLine }),
    text(value, {
      position: { x: 0, y: 18 },
      width: w - 36,
      height: 38,
      style: { typeface: "Avenir Next", fontSize: 30, bold: true, color: C.ink, wrap: "square" },
    }),
    text(label, {
      position: { x: 0, y: 62 },
      width: w - 36,
      height: 34,
      style: { typeface: "Avenir Next", fontSize: 14.5, color: C.slate, wrap: "square" },
    }),
  ]));
}

export function node(title, desc, x, y, w, h, accent = C.leaf) {
  return card({
    position: { x, y },
    width: w,
    height: h,
    padding: 14,
    fill: C.white,
    line: line("#DDE6D8"),
    borderRadius: 18,
  }, layers({ width: w - 28, height: h - 28 }, [
    shape({ position: { x: 0, y: 0 }, width: 34, height: 5, fill: accent, line: noLine }),
    text(title, {
      position: { x: 0, y: 15 },
      width: w - 28,
      height: 26,
      style: { typeface: "Avenir Next", fontSize: 17, bold: true, color: C.ink, wrap: "square" },
    }),
    text(desc, {
      position: { x: 0, y: 48 },
      width: w - 28,
      height: h - 72,
      style: { typeface: "Avenir Next", fontSize: 12.2, color: C.slate, wrap: "square" },
    }),
  ]));
}

export function flow(x1, y1, x2, y2, color = C.moss) {
  const horizontal = Math.abs(x2 - x1) >= Math.abs(y2 - y1);
  return layers({ width: W, height: H }, [
    shape({
      position: { x: horizontal ? Math.min(x1, x2) : x1 - 1.5, y: horizontal ? y1 - 1.5 : Math.min(y1, y2) },
      width: horizontal ? Math.abs(x2 - x1) : 3,
      height: horizontal ? 3 : Math.abs(y2 - y1),
      fill: color,
      line: noLine,
    }),
    shape({ position: { x: x2 - 5, y: y2 - 5 }, width: 10, height: 10, geometry: "ellipse", fill: color, line: noLine }),
  ]);
}

export function footer(slide, dark = false) {
  slide.compose(layers({ width: W, height: H }, [
    text("Gemma 4 Hackathon Shanghai · Track C Edge AI", {
      position: { x: 72, y: 654 },
      width: 520,
      height: 22,
      style: { typeface: "Avenir Next", fontSize: 12.5, color: dark ? "#DDE6D8" : C.slate },
    }),
  ]));
}
