import { addBase, addFooter, C, arrow, body, nodeBox, pill, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide08(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 8, "路演 Demo 应该讲一个完整户外故事，而不是逐项点功能");
  slide.compose(layers({ width: 1280, height: 720 }, [
    strong("推荐演示脚本：从“迷路无信号”进入，逐步展示工具协同。", 76, 148, 800, 76, 34),
    body("半决赛时建议把 demo 控制在 3-4 分钟：先讲原 App 的离线工具，再展示 AI 如何把这些工具串成行动。", 76, 238, 760, 54, 20, C.slate),
    nodeBox("1. 进入 AI Assistant", "展示这是 Trail Sense 内部新工具，而非外部 Web chatbot。", 94, 340, 210, 120, C.white, C.amber),
    arrow(314, 400, 372, 400, C.moss),
    nodeBox("2. 迷路无信号", "输入：我迷路了，手机没信号怎么办？", 382, 340, 210, 120, C.white, C.rust),
    arrow(602, 400, 660, 400, C.moss),
    nodeBox("3. 工具推荐", "AI 推荐导航、路径、信标、离线地图和安全步骤。", 670, 340, 210, 120, C.white, C.leaf),
    arrow(890, 400, 948, 400, C.moss),
    nodeBox("4. 应急动作", "输入打开 SOS 手电筒或用哨子求救，展示行动卡片。", 958, 340, 210, 120, C.white, C.sky),
    nodeBox("5. 读数解释", "天气/气压怎么看？展示本地上下文解释能力。", 382, 510, 210, 100, C.white, C.amber),
    nodeBox("6. 图片输入", "附加云层或截图，展示多模态入口。", 670, 510, 210, 100, C.white, C.leaf),
    pill("结论：用户说目标，Trail Sense 组织工具", 412, 612, 460, C.pine),
  ]));
  addFooter(slide);
  return slide;
}
