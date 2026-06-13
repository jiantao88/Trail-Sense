import {
  W,
  H,
  C,
  noLine,
  line,
  layers,
  card,
  shape,
  t,
  paper,
  page,
  marker,
  pill,
  iconCircle,
  panel,
  infoCard,
  conclusion,
  arrow,
  phone,
  miniTable,
  mountainBand,
} from "./theme.mjs";

function logoMark(x, y, scale = 1) {
  return layers({ width: W, height: H }, [
    shape({ position: { x, y }, width: 68 * scale, height: 52 * scale, geometry: "triangle", fill: "rgba(23,63,34,0.10)", line: line(C.pine, 3 * scale) }),
    shape({ position: { x: x + 36 * scale, y: y + 8 * scale }, width: 52 * scale, height: 44 * scale, geometry: "triangle", fill: "rgba(23,63,34,0.05)", line: line(C.pine, 3 * scale) }),
  ]);
}

function toolList(items, x, y, w, gap = 76) {
  return layers({ width: W, height: H }, items.map((it, i) => infoCard(it.title, it.body, x, y + i * gap, w, 70, it.icon, it.accent)));
}

function smallModule(title, body, x, y, w, h, icon, accent = C.moss) {
  return panel(x, y, w, h, [
    iconCircle(icon, 14, 16, 34, accent),
    t(title, 58, 16, w - 72, 25, { fontSize: 16, bold: true }),
    t(body, 58, 43, w - 72, h - 48, { fontSize: 12.3, color: C.ink, lineSpacing: 1.05 }),
  ]);
}

export async function slide01(presentation) {
  const slide = presentation.slides.add();
  paper(slide, { mountain: true });
  slide.compose(layers({ width: W, height: H }, [
    marker(1, 24, 18),
    t("让离线户外工具箱\n具备端侧 AI 判断力", 70, 84, 520, 150, { fontSize: 40, bold: true, lineSpacing: 0.9 }),
    logoMark(76, 248, 0.92),
    t("TRAIL SENSE\nAI ASSISTANT", 174, 252, 270, 58, { typeface: "Avenir Next Condensed", fontSize: 28, bold: true, lineSpacing: 0.82 }),
    t("Trail Sense AI Assistant 基于成熟离线户外 App 构建，使用 Gemma 4、LiteRT-LM、Agent Skill、Native Function Calling 和多模态输入，让户外工具箱具备自然语言理解和本地任务执行能力。", 74, 342, 510, 88, { fontSize: 16, lineSpacing: 1.12 }),
    pill("Gemma 4", 78, 462, 120, C.cream, C.pine),
    pill("LiteRT-LM", 212, 462, 120, C.cream, C.pine),
    pill("Agent Skill", 346, 462, 134, C.cream, C.pine),
    pill("Edge AI", 494, 462, 108, C.cream, C.pine),
    pill("0 云端 LLM", 78, 520, 158, C.pine, C.white),
    pill("12+ 本地工具", 252, 520, 168, C.pine, C.white),
    pill("完整 Android App", 436, 520, 198, C.pine, C.white),
    pill("Demo 链接：youtube.com/watch?v=EkK9DF7OfXg", 78, 582, 420, "#E9EFE4", C.ink),
    phone(812, 112, 210, 404, {
      title: "TRAIL SENSE",
      lines: ["AI Assistant", "本地 LLM\n工具调用\n离线任务"],
      chips: ["指南针", "路径", "天气", "SOS"],
    }),
    shape({ position: { x: 945, y: 470 }, width: 180, height: 170, geometry: "triangle", fill: "rgba(23,63,34,0.34)", line: noLine }),
    t("Track C · Gemma 4 · Edge AI", 815, 610, 320, 24, { fontSize: 14, bold: true, color: C.pine, alignment: "center" }),
  ]));
  return slide;
}

export async function slide02(presentation) {
  const slide = presentation.slides.add();
  page(slide, 2, "户外场景不是缺 App，\n而是缺少低成本判断", { titleWidth: 550, titleSize: 30 });
  slide.compose(layers({ width: W, height: H }, [
    toolList([
      { icon: "多", title: "工具多", body: "指南针、路径、天气、信标、SOS、云层、高线地图分散在不同入口。", accent: C.moss },
      { icon: "读", title: "读数难", body: "气压、方向、坡度、云层变化需要经验解释。", accent: C.sage },
      { icon: "急", title: "场景急", body: "迷路、天气突变、夜间返程、受伤求助时操作成本必须降低。", accent: C.leaf },
      { icon: "网", title: "无网络", body: "云端 AI 和在线搜索在关键时刻不可依赖。", accent: C.moss },
    ], 72, 144, 470, 82),
    phone(760, 118, 210, 392, {
      title: "Outdoor Tools",
      lines: ["工具入口很多", "导航 / 天气 / 路径\n信标 / SOS / 指南"],
      chips: ["地图", "气压", "信标", "指南"],
    }),
    panel(690, 548, 390, 76, [
      t("户外真正的困难，\n是工具多、读数复杂、场景紧急、网络不可依赖。", 22, 14, 346, 48, { fontSize: 18, bold: true, color: C.white, alignment: "center", anchor: 2 }),
    ], { fill: C.moss, line: C.moss, radius: 10 }),
  ]));
  return slide;
}

export async function slide03(presentation) {
  const slide = presentation.slides.add();
  page(slide, 3, "完整产品：成熟离线户外工具箱\n+ AI 操作层", { titleWidth: 880, titleSize: 30 });
  const left = [
    ["导航与定位", "指南针、路线、位置、航向"],
    ["天气与环境", "天气预报、气压、风向、云层"],
    ["生存与应急", "信标、SOS、哨子、急救指南"],
    ["天文与时间", "日出日落、月相、时间工具"],
  ];
  const right = [
    ["自然语言入口", "用户用问题表达目标和问题"],
    ["Gemma 4 本地推理", "端侧理解、推理与决策"],
    ["Agent Skill", "任务到工具的流程编排"],
    ["Native Function Calling", "调用本地工具能力"],
    ["图片输入与行动卡片", "多模态理解与建议输出"],
  ];
  slide.compose(layers({ width: W, height: H }, [
    panel(74, 134, 330, 424, [
      t("原有能力（成熟离线）", 0, 16, 330, 26, { fontSize: 16, bold: true, alignment: "center" }),
      ...left.flatMap((item, i) => [
        iconCircle(String(i + 1), 22, 62 + i * 82, 40, C.moss),
        t(item[0], 78, 58 + i * 82, 210, 26, { fontSize: 18, bold: true }),
        t(item[1], 78, 86 + i * 82, 220, 28, { fontSize: 12.5 }),
      ]),
    ]),
    phone(510, 146, 204, 388, {
      title: "Trail Sense",
      lines: ["完整户外 App", "地图 / 天气 / 工具\n全部本地可用"],
      chips: ["导航", "路径", "天气", "信标"],
    }),
    panel(820, 126, 360, 450, [
      t("新增能力（AI 操作层）", 0, 16, 360, 26, { fontSize: 16, bold: true, alignment: "center" }),
      ...right.flatMap((item, i) => [
        iconCircle(String(i + 1), 22, 58 + i * 70, 38, i === 1 ? C.pine : C.moss),
        t(item[0], 72, 54 + i * 70, 230, 24, { fontSize: 17, bold: true }),
        t(item[1], 72, 80 + i * 70, 238, 26, { fontSize: 12.3 }),
      ]),
    ]),
    conclusion("AI 不替代原工具，而是把原工具组织成更低成本的户外任务流程。", 148, 620, 980),
  ]));
  return slide;
}

export async function slide04(presentation) {
  const slide = presentation.slides.add();
  page(slide, 4, "用户说目标，Trail Sense\n组织工具、解释读数、准备动作", { titleWidth: 720, titleSize: 28 });
  slide.compose(layers({ width: W, height: H }, [
    panel(82, 132, 500, 86, [
      t("示例问题：", 22, 16, 112, 24, { fontSize: 15, bold: true }),
      t("\"我迷路了，手机没信号怎么办？\"", 22, 42, 430, 28, { fontSize: 20, bold: true }),
    ]),
    infoCard("AI 识别意图", "迷路、无信号，需要定位和求救。", 82, 246, 500, 78, "1", C.moss),
    infoCard("工具组合", "导航、路径、信标、离线地图、SOS、哨子。", 82, 342, 500, 78, "2", C.sage),
    infoCard("输出形态", "行动卡片、工具入口、解释性建议、安全提示。", 82, 438, 500, 78, "3", C.leaf),
    phone(836, 104, 230, 440, {
      title: "Trail Sense AI Assistant",
      lines: ["我迷路了，手机没信号怎么办？", "建议先保存当前位置\n再使用信标和 SOS"],
      chips: ["指南针", "路径", "SOS", "哨子"],
    }),
    conclusion("AI 解决的是工具发现、读数解释和行动组织，而不是泛泛聊天。", 242, 622, 820),
  ]));
  return slide;
}

export async function slide05(presentation) {
  const slide = presentation.slides.add();
  page(slide, 5, "端侧架构：Gemma 4 本地推理\n接入 Trail Sense 工具调用", { titleWidth: 810, titleSize: 29, mountain: false });
  const rows = [
    ["输入层", "文字、图片、App 上下文"],
    ["AI 层", "AI Assistant UI、Prompt Builder、工具知识库、Skill 知识库"],
    ["推理层", "Gemma 4 本地 LLM、LiteRT-LM Runtime、模型管理"],
    ["调用层", "Native Function Calling、Tool Runner、本地工具执行"],
    ["执行层", "Trail Sense 本地工具：导航、天气、路径、SOS"],
  ];
  slide.compose(layers({ width: W, height: H }, [
    panel(72, 132, 1016, 402, [
      ...rows.flatMap((row, i) => [
        iconCircle(String(i + 1), 24, 26 + i * 72, 36, i === 2 ? C.pine : C.moss),
        t(row[0], 76, 24 + i * 72, 110, 34, { fontSize: 17, bold: true }),
        shape({ position: { x: 205, y: 16 + i * 72 }, width: 1, height: 54, fill: "rgba(70,104,59,0.25)", line: noLine }),
        t(row[1], 232, 24 + i * 72, 520, 34, { fontSize: 15.5 }),
        i < rows.length - 1 ? shape({ position: { x: 28, y: 82 + i * 72 }, width: 940, height: 1, fill: "rgba(70,104,59,0.18)", line: noLine }) : shape({ position: { x: 0, y: 0 }, width: 1, height: 1, fill: "rgba(0,0,0,0)", line: noLine }),
      ]),
      t("LiteRT", 820, 186, 150, 48, { typeface: "Avenir Next Condensed", fontSize: 34, bold: true, color: C.moss, alignment: "center" }),
      t("端侧运行", 822, 240, 150, 22, { fontSize: 14, bold: true, color: C.ink, alignment: "center" }),
    ]),
    conclusion("数据边界：模型、推理、上下文、历史记录和图片输入主要留在端侧；网络只用于模型下载等必要环节。", 72, 586, 1016),
  ]));
  return slide;
}

export async function slide06(presentation) {
  const slide = presentation.slides.add();
  page(slide, 6, "Agent Skill：把户外任务\n封装成可执行工作流", { titleWidth: 700, titleSize: 29, mountain: false });
  slide.compose(layers({ width: W, height: H }, [
    smallModule("Skill 知识库", "ai_tool_skills.md", 76, 150, 230, 74, "S", C.moss),
    smallModule("工具知识库", "ai_tool_knowledge.md", 76, 250, 230, 74, "T", C.sage),
    smallModule("调用流程", "loadSkill(skillName)\n-> runTrailSenseTool(toolId)\n-> 本地工具结果\n-> 回答与行动卡片", 76, 350, 280, 156, "F", C.leaf),
    panel(496, 128, 360, 422, [
      t("Skill 执行流程示例（迷路无信号）", 0, 18, 360, 24, { fontSize: 15, bold: true, alignment: "center" }),
      ...["识别意图\n迷路 + 无信号 + 需要求救", "加载对应 Skill\nloadSkill('lost_no_signal')", "执行步骤\n调用多个本地工具", "整合结果\n生成解释与行动建议"].flatMap((label, i) => [
        panel(48, 62 + i * 80, 264, 54, [t(label, 12, 7, 240, 40, { fontSize: 12.5, alignment: "center", anchor: 2 })], { fill: C.cream, line: "rgba(70,104,59,0.45)", radius: 8 }),
        i < 3 ? arrow(154, 121 + i * 80, 40, C.sage) : shape({ position: { x: 0, y: 0 }, width: 1, height: 1, fill: "rgba(0,0,0,0)", line: noLine }),
      ]),
    ]),
    panel(918, 156, 250, 258, [
      t("工具执行", 0, 18, 250, 26, { fontSize: 17, bold: true, alignment: "center" }),
      ...["指南针", "地图", "信标", "SOS", "路径", "哨子"].map((label, i) => panel(30 + (i % 2) * 96, 62 + Math.floor(i / 2) * 58, 82, 42, [
        t(label, 0, 0, 82, 40, { fontSize: 12.5, bold: true, alignment: "center", anchor: 2 }),
      ], { fill: "#F2F6EC", line: "rgba(70,104,59,0.35)", radius: 8 })),
      t("Trail Sense\n本地工具", 0, 220, 250, 30, { fontSize: 13, bold: true, alignment: "center" }),
    ]),
    conclusion("Skill 表达的是“完成一个户外任务的步骤”，不是单个功能卡片。", 214, 620, 852),
  ]));
  return slide;
}

export async function slide07(presentation) {
  const slide = presentation.slides.add();
  page(slide, 7, "多模态输入：户外照片和\nApp 截图进入本地推理链路", { titleWidth: 760, titleSize: 29 });
  slide.compose(layers({ width: W, height: H }, [
    panel(72, 138, 324, 188, [
      t("输入流程", 22, 18, 160, 24, { fontSize: 17, bold: true }),
      t("图片选择\n-> Bitmap 处理\n-> Prompt 图片提示\n-> LiteRT-LM 输入", 28, 62, 240, 92, { fontSize: 16, lineSpacing: 1.18 }),
    ]),
    panel(72, 370, 520, 150, [
      t("典型场景", 22, 18, 120, 24, { fontSize: 17, bold: true }),
      ...["云层识别", "App 截图解释", "环境观察"].map((label, i) => panel(26 + i * 160, 60, 134, 62, [
        mountainBand(-40 + i * 20, 18, 180, 52, false),
        t(label, 0, 20, 134, 32, { fontSize: 13, bold: true, alignment: "center", anchor: 2 }),
      ], { fill: "rgba(255,253,244,0.88)", line: "rgba(70,104,59,0.42)", radius: 10 })),
    ]),
    panel(448, 158, 250, 132, [
      t("AI 识别与理解", 0, 18, 250, 24, { fontSize: 17, bold: true, alignment: "center" }),
      t("图片 + 文字 + 工具知识\n合并进入本地推理链路", 28, 58, 194, 50, { fontSize: 14, alignment: "center", anchor: 2 }),
    ]),
    arrow(410, 208, 48, C.moss),
    arrow(710, 208, 48, C.moss),
    phone(810, 108, 230, 440, {
      title: "AI Assistant",
      lines: ["这片云层有什么风险？", "结合图片判断：可能出现\n积云发展，关注天气变化"],
      chips: ["看天气", "看气压", "安全建议", "工具入口"],
    }),
    t("用户可以直接提交“看到的东西”，AI 结合图片、文字和工具知识做判断。", 76, 604, 650, 38, { fontSize: 19, bold: true }),
  ]));
  return slide;
}

export async function slide08(presentation) {
  const slide = presentation.slides.add();
  page(slide, 8, "工程交付：代码、测试、APK、\nDemo 和官方提交材料已完成", { titleWidth: 810, titleSize: 28 });
  slide.compose(layers({ width: W, height: H }, [
    smallModule("技术栈", "Gemma 4、LiteRT-LM、Android/Kotlin、Trail Sense 本地工具体系", 76, 142, 420, 88, "1", C.moss),
    smallModule("核心模块", "AiInferenceSubsystem、ModelManager、AiAssistantTools、TrailSenseAiToolRunner", 76, 252, 420, 96, "2", C.sage),
    smallModule("知识与 Prompt", "AiPromptBuilder、ai_tool_knowledge.md、ai_tool_skills.md", 76, 372, 420, 88, "3", C.leaf),
    smallModule("交付物", "APK、Demo 视频、技术报告、官方提交材料", 76, 482, 420, 82, "4", C.moss),
    panel(570, 136, 222, 190, [
      t("工程交付", 0, 18, 222, 24, { fontSize: 16, bold: true, alignment: "center" }),
      t("Android\nAPK", 0, 62, 222, 72, { typeface: "Avenir Next Condensed", fontSize: 34, bold: true, color: C.moss, alignment: "center", anchor: 2 }),
      pill("Release Package", 34, 144, 154, C.pine, C.white),
    ]),
    panel(820, 136, 240, 190, [
      t("测试验证", 0, 18, 240, 24, { fontSize: 16, bold: true, alignment: "center" }),
      ...["功能测试", "集成测试", "离线测试", "稳定性测试"].map((label, i) => t("✓ " + label, 50, 58 + i * 30, 140, 22, { fontSize: 15, bold: true, color: C.moss })),
    ]),
    panel(570, 356, 490, 156, [
      t("Demo 视频", 30, 22, 150, 28, { fontSize: 18, bold: true }),
      card({ position: { x: 248, y: 34 }, width: 108, height: 74, padding: 0, fill: "#E24A35", line: noLine, borderRadius: 16 }, t("▶", 0, 0, 108, 70, { fontSize: 36, bold: true, color: C.white, alignment: "center", anchor: 2 })),
      t("youtube.com/watch?v=EkK9DF7OfXg", 30, 92, 196, 40, { fontSize: 13, color: C.ink }),
      t("官方提交材料\n技术报告 / APK / Demo / PR", 358, 42, 110, 70, { fontSize: 13, bold: true, alignment: "center", anchor: 2 }),
    ]),
  ]));
  return slide;
}

export async function slide09(presentation) {
  const slide = presentation.slides.add();
  page(slide, 9, "差异化：不是云端聊天机器人，\n而是端侧工具型 AI", { titleWidth: 760, titleSize: 29, darkMountain: true });
  slide.compose(layers({ width: W, height: H }, [
    miniTable(74, 156, 760, 300,
      ["vs 普通云端 Chatbot", "vs 单点 AI 功能", "vs 传统工具箱"],
      [
        ["端侧推理，离线可用", "接入完整户外工具体系", "自然语言 + 图片输入"],
        ["隐私保护，数据不出设备", "不是单纯问答", "Agent Skill 降低使用门槛"],
        ["户外场景约束下的可靠建议", "覆盖真实任务工作流", "从“找工具”到“做任务”"],
      ],
      2),
    panel(168, 548, 612, 62, [
      t("Trail Sense AI Assistant 把 AI 做进一个有明确场景、有离线价值、有真实工具资产的 App。", 24, 0, 564, 60, { fontSize: 18, bold: true, color: C.white, alignment: "center", anchor: 2 }),
    ], { fill: C.moss, line: C.moss, radius: 10 }),
    shape({ position: { x: 900, y: 370 }, width: 220, height: 190, geometry: "triangle", fill: "rgba(23,63,34,0.32)", line: noLine }),
    t("Edge Tool AI", 890, 540, 240, 34, { typeface: "Avenir Next Condensed", fontSize: 28, bold: true, color: C.pine, alignment: "center" }),
  ]));
  return slide;
}

export async function slide10(presentation) {
  const slide = presentation.slides.add();
  paper(slide, { mountain: true, darkMountain: false });
  slide.compose(layers({ width: W, height: H }, [
    marker(10, 24, 18),
    t("让户外工具在无网时也能听懂人话", 94, 28, 760, 44, { fontSize: 32, bold: true }),
    panel(112, 112, 760, 54, [
      t("Trail Sense AI Assistant = 成熟离线户外工具箱 + Gemma 4 本地 LLM + Agent Skill + Native Function Calling + 多模态输入", 22, 0, 716, 52, { fontSize: 17, bold: true, alignment: "center", anchor: 2 }),
    ]),
    panel(112, 208, 278, 228, [
      t("技术总结", 0, 22, 278, 26, { fontSize: 18, bold: true, alignment: "center" }),
      ...["本地 LLM（Gemma 4）", "图片识别与多模态理解", "Agent Skill 任务编排", "Native Function Calling", "本地工具执行"].map((label, i) => t("✓ " + label, 38, 64 + i * 30, 210, 22, { fontSize: 14.5, color: C.ink })),
    ]),
    panel(420, 208, 278, 228, [
      t("产品总结", 0, 22, 278, 26, { fontSize: 18, bold: true, alignment: "center" }),
      ...["弱网可用，离线可靠", "隐私友好，数据本地", "操作成本低，效率更高", "与真实户外工具深度结合"].map((label, i) => t("✓ " + label, 38, 74 + i * 34, 210, 22, { fontSize: 14.5, color: C.ink })),
    ]),
    logoMark(920, 156, 1.55),
    t("TRAIL SENSE\nAI ASSISTANT", 870, 282, 320, 76, { typeface: "Avenir Next Condensed", fontSize: 36, bold: true, alignment: "center", lineSpacing: 0.82 }),
    t("Track C  |  Gemma 4  |  Trail Sense", 870, 410, 320, 24, { fontSize: 14, bold: true, alignment: "center" }),
    t("Q&A", 888, 500, 280, 72, { typeface: "Avenir Next Condensed", fontSize: 58, bold: true, color: C.pine, alignment: "center" }),
  ]));
  return slide;
}
