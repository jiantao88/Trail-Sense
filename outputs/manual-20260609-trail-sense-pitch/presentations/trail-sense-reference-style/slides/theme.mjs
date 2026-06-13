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
  paper: "#F7F1E4",
  cream: "#FFFDF4",
  ink: "#122419",
  pine: "#173F22",
  deep: "#0E2718",
  moss: "#46683B",
  sage: "#71865C",
  line: "#8FA181",
  faint: "#D9E0CF",
  sky: "#DDE8E5",
  amber: "#C8953C",
  leaf: "#6A8A4B",
  white: "#FFFFFF",
};

export const noLine = stroke("none");
export const line = (color = C.line, width = 1) => stroke(`${width}px solid ${color}`);

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

export function t(value, x, y, width, height, style = {}) {
  return text(value, {
    position: { x, y },
    width,
    height,
    style: {
      typeface: style.typeface ?? "Avenir Next",
      fontSize: style.fontSize ?? 20,
      bold: style.bold ?? false,
      color: style.color ?? C.ink,
      alignment: style.alignment ?? "left",
      wrap: style.wrap ?? "square",
      anchor: style.anchor,
      lineSpacing: style.lineSpacing,
    },
  });
}

export function paper(slide, opts = {}) {
  slide.background.fill = C.paper;
  const mountain = opts.mountain ?? true;
  slide.compose(layers({ width: W, height: H }, [
    shape({ position: { x: 0, y: 0 }, width: W, height: H, fill: "linear(150deg, #FFFDF4 0%, #F4EDDE 62%, #E6E2D0 100%)", line: noLine }),
    shape({ position: { x: 870, y: 0 }, width: 390, height: 130, geometry: "ellipse", fill: "rgba(70,104,59,0.06)", line: noLine }),
    contour(900, 45, 300, 190, 0.26),
    contour(945, 140, 290, 190, 0.16),
    contour(45, 548, 300, 150, 0.14),
    mountain ? mountainBand(0, 520, W, 172, opts.darkMountain ?? false) : layers({ width: 1, height: 1 }, []),
  ]));
}

export function contour(x, y, w, h, opacity = 0.2) {
  return layers({ width: W, height: H }, [
    shape({ position: { x, y }, width: w, height: h, geometry: "ellipse", fill: "rgba(255,255,255,0)", line: line(`rgba(70,104,59,${opacity})`, 1) }),
    shape({ position: { x: x + 38, y: y + 24 }, width: w - 76, height: h - 48, geometry: "ellipse", fill: "rgba(255,255,255,0)", line: line(`rgba(70,104,59,${opacity * 0.72})`, 1) }),
    shape({ position: { x: x + 82, y: y + 52 }, width: w - 164, height: h - 104, geometry: "ellipse", fill: "rgba(255,255,255,0)", line: line(`rgba(70,104,59,${opacity * 0.55})`, 1) }),
  ]);
}

export function mountainBand(x, y, w, h, dark = false) {
  const base = dark ? "rgba(14,39,24,0.82)" : "rgba(88,118,77,0.24)";
  const back = dark ? "rgba(70,104,59,0.44)" : "rgba(113,134,92,0.18)";
  return layers({ width: W, height: H }, [
    shape({ position: { x, y: y + 35 }, width: w * 0.38, height: h * 0.82, geometry: "triangle", fill: back, line: noLine }),
    shape({ position: { x: x + 210, y: y + 10 }, width: w * 0.42, height: h, geometry: "triangle", fill: back, line: noLine }),
    shape({ position: { x: x + 565, y: y + 28 }, width: w * 0.44, height: h * 0.9, geometry: "triangle", fill: back, line: noLine }),
    shape({ position: { x: x + 800, y: y }, width: w * 0.35, height: h, geometry: "triangle", fill: base, line: noLine }),
    shape({ position: { x: x + 80, y: y + 60 }, width: w * 0.48, height: h, geometry: "triangle", fill: base, line: noLine }),
    shape({ position: { x, y: y + h - 18 }, width: w, height: 34, fill: dark ? "rgba(14,39,24,0.86)" : "rgba(247,241,228,0.75)", line: noLine }),
  ]);
}

export function page(slide, n, title, opts = {}) {
  paper(slide, opts);
  slide.compose(layers({ width: W, height: H }, [
    marker(n, 24, 18),
    t(title, 94, 22, opts.titleWidth ?? 900, 64, { fontSize: opts.titleSize ?? 31, bold: true, color: C.ink, lineSpacing: 0.92 }),
  ]));
}

export function marker(n, x, y) {
  return card({
    position: { x, y },
    width: 56,
    height: 56,
    padding: 0,
    fill: C.pine,
    line: noLine,
    borderRadius: 8,
    align: "center",
    justify: "center",
  }, t(String(n).padStart(2, "0"), 0, 0, 56, 54, {
    typeface: "Avenir Next Condensed",
    fontSize: 30,
    bold: true,
    color: C.white,
    alignment: "center",
    anchor: 2,
  }));
}

export function pill(label, x, y, w, fill = C.pine, color = C.white) {
  return card({
    position: { x, y },
    width: w,
    height: 32,
    padding: 0,
    fill,
    line: noLine,
    borderRadius: 16,
    align: "center",
    justify: "center",
  }, t(label, 0, 0, w, 30, { fontSize: 14, bold: true, color, alignment: "center", anchor: 2 }));
}

export function iconCircle(label, x, y, size = 44, fill = C.moss) {
  return layers({ width: W, height: H }, [
    shape({ position: { x, y }, width: size, height: size, geometry: "ellipse", fill, line: noLine }),
    t(label, x, y + 1, size, size - 2, { typeface: "Avenir Next Condensed", fontSize: size * 0.34, bold: true, color: C.white, alignment: "center", anchor: 2 }),
  ]);
}

export function panel(x, y, w, h, children = [], opts = {}) {
  return card({
    position: { x, y },
    width: w,
    height: h,
    padding: 0,
    fill: opts.fill ?? "rgba(255,253,244,0.88)",
    line: line(opts.line ?? "rgba(70,104,59,0.45)", opts.lineWidth ?? 1),
    borderRadius: opts.radius ?? 12,
  }, layers({ width: w, height: h }, children));
}

export function infoCard(title, body, x, y, w, h, icon = "", accent = C.moss) {
  return panel(x, y, w, h, [
    icon ? iconCircle(icon, 14, 16, 38, accent) : shape({ position: { x: 14, y: 18 }, width: 28, height: 4, fill: accent, line: noLine }),
    t(title, icon ? 62 : 18, 17, w - (icon ? 78 : 36), 28, { fontSize: 18, bold: true, color: C.ink }),
    t(body, icon ? 62 : 18, 50, w - (icon ? 78 : 36), h - 58, { fontSize: 13.5, color: C.ink, lineSpacing: 1.08 }),
  ]);
}

export function conclusion(value, x = 78, y = 626, w = 1088) {
  return card({
    position: { x, y },
    width: w,
    height: 48,
    padding: 0,
    fill: C.moss,
    line: noLine,
    borderRadius: 8,
    align: "center",
    justify: "center",
  }, t(value, 24, 0, w - 48, 46, { fontSize: 18, bold: true, color: C.white, alignment: "center", anchor: 2 }));
}

export function arrow(x, y, w = 42, color = C.moss) {
  return shape({ position: { x, y }, width: w, height: 24, geometry: "rightArrow", fill: color, line: noLine });
}

export function phone(x, y, w = 190, h = 380, opts = {}) {
  const innerX = 12;
  const innerY = 26;
  const innerW = w - 24;
  const innerH = h - 46;
  const title = opts.title ?? "Trail Sense";
  const dark = opts.dark ?? true;
  const lines = opts.lines ?? ["AI Assistant", "本地推理", "工具建议"];
  const chips = opts.chips ?? ["导航", "天气", "SOS", "路径"];
  return layers({ width: W, height: H }, [
    shape({ position: { x, y }, width: w, height: h, fill: "#101912", line: line("rgba(18,36,25,0.65)", 2), borderRadius: 28 }),
    shape({ position: { x: x + innerX, y: y + innerY }, width: innerW, height: innerH, fill: dark ? "#1A2B1D" : C.cream, line: noLine, borderRadius: 20 }),
    shape({ position: { x: x + w / 2 - 32, y: y + 10 }, width: 64, height: 8, fill: "#222C24", line: noLine, borderRadius: 4 }),
    t(title, x + 22, y + 38, w - 44, 24, { typeface: "Avenir Next Condensed", fontSize: 12, bold: true, color: dark ? C.white : C.ink, alignment: "center" }),
    shape({ position: { x: x + 30, y: y + 70 }, width: w - 60, height: 95, fill: dark ? "rgba(255,253,244,0.08)" : "#EDF2E8", line: line(dark ? "rgba(255,255,255,0.22)" : "rgba(70,104,59,0.35)", 1), borderRadius: 14 }),
    t(lines[0] ?? "", x + 42, y + 82, w - 84, 30, { fontSize: 13, bold: true, color: dark ? C.white : C.ink }),
    t(lines.slice(1).join("\n"), x + 42, y + 115, w - 84, 52, { fontSize: 10.5, color: dark ? "#DDE8D6" : C.ink, lineSpacing: 1.1 }),
    ...chips.slice(0, 4).map((c, i) => {
      const cx = x + 30 + (i % 2) * ((w - 70) / 2 + 8);
      const cy = y + 190 + Math.floor(i / 2) * 52;
      return panel(cx, cy, (w - 78) / 2, 40, [
        t(c, 0, 0, (w - 78) / 2, 38, { fontSize: 11, bold: true, color: dark ? C.white : C.ink, alignment: "center", anchor: 2 }),
      ], { fill: dark ? "rgba(255,253,244,0.08)" : "#FFFFFF", line: dark ? "rgba(255,255,255,0.18)" : "rgba(70,104,59,0.35)", radius: 10 });
    }),
    shape({ position: { x: x + w / 2 - 14, y: y + h - 24 }, width: 28, height: 6, fill: "#303A33", line: noLine, borderRadius: 3 }),
  ]);
}

export function miniTable(x, y, w, h, headers, rows, highlightCol = -1) {
  const rowH = h / (rows.length + 1);
  const colW = w / headers.length;
  const cells = [];
  headers.forEach((head, i) => {
    cells.push(shape({ position: { x: i * colW, y: 0 }, width: colW, height: rowH, fill: i === highlightCol ? C.moss : C.pine, line: line("rgba(255,255,255,0.22)", 1) }));
    cells.push(t(head, i * colW + 8, 2, colW - 16, rowH - 4, { fontSize: 13.5, bold: true, color: C.white, alignment: "center", anchor: 2 }));
  });
  rows.forEach((row, r) => {
    row.forEach((cell, i) => {
      cells.push(shape({ position: { x: i * colW, y: (r + 1) * rowH }, width: colW, height: rowH, fill: i === highlightCol ? "rgba(70,104,59,0.18)" : "rgba(255,253,244,0.92)", line: line("rgba(70,104,59,0.32)", 1) }));
      cells.push(t(cell, i * colW + 8, (r + 1) * rowH + 4, colW - 16, rowH - 8, { fontSize: 12.3, color: C.ink, alignment: "center", anchor: 2 }));
    });
  });
  return panel(x, y, w, h, cells, { fill: "rgba(255,253,244,0.4)", line: "rgba(70,104,59,0.5)", radius: 8 });
}
