import { C, header, footer, h1, p, proof, node, layers } from "./theme.mjs";

export default async function slide03(presentation) {
  const slide = presentation.slides.add();
  header(slide, 3, "完整产品：成熟离线户外工具箱 + AI 操作层");
  slide.compose(layers({ width: 1280, height: 720 }, [
    h1("AI Assistant 建在已有 Trail Sense 工具体系上。", 76, 156, 560, 72, 38),
    p("项目不是从零重写 App，而是在原有离线工具、传感器能力和生存指南基础上，新增一个理解工具体系的端侧 AI 助手。", 76, 246, 570, 84, 21),
    node("导航与定位", "指南针、导航、路径、信标、离线地图", 720, 150, 240, 112, C.leaf),
    node("天气与环境", "天气、气压、云识别、雷电距离、气候", 1000, 150, 240, 112, C.amber),
    node("生存与应急", "生存指南、SOS 手电筒、哨子、装备清单", 720, 292, 240, 112, C.rust),
    node("天文与时间", "日出日落、月相、天文信息、返程判断", 1000, 292, 240, 112, C.sky),
    proof("新增 AI Assistant", "自然语言入口、Gemma 4 本地推理、Agent Skill、Native Function Calling、图片输入和行动卡片。", 76, 450, 520, 118, C.amber),
    proof("项目价值", "AI 不是替代原工具，而是把原工具组织成更低操作成本的户外工作流。", 660, 450, 520, 118, C.leaf),
  ]));
  footer(slide);
  return slide;
}
