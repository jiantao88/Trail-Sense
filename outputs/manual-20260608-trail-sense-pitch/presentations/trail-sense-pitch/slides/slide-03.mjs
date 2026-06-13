import { addBase, addFooter, C, body, featureCard, label, metric, nodeBox, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide03(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 3, "完整项目不是从零造 App：是在成熟离线户外工具箱上加智能入口");
  slide.compose(layers({ width: 1280, height: 720 }, [
    label("Original Trail Sense", 76, 148, C.moss),
    strong("保留原 App 的离线工具、传感器和生存指南。", 76, 174, 530, 80, 34),
    body("这让 AI 不是悬浮在外面的聊天窗口，而是接入已有工具、已有数据和已有户外工作流。", 76, 265, 560, 64, 21, C.slate),
    nodeBox("导航与定位", "指南针、导航、路径、信标、离线地图", 720, 142, 250, 116, C.white, C.leaf),
    nodeBox("天气与环境", "天气、气压、云识别、雷电距离、气候", 1000, 142, 250, 116, C.white, C.amber),
    nodeBox("生存与应急", "生存指南、SOS 手电筒、哨子、装备清单", 720, 286, 250, 116, C.white, C.rust),
    nodeBox("天文与时间", "日出日落、月相、天文信息、返程判断", 1000, 286, 250, 116, C.white, C.sky),
    featureCard("新增 AI Assistant", "自然语言入口 + Gemma 4 本地推理 + Agent Skill + Native Function Calling + 图片输入 + 行动卡片。", 76, 430, 550, 136, C.amber),
    featureCard("项目价值", "把工具箱从“用户自己查”升级为“用户说目标，系统组织工具、解释数据、给出下一步动作”。", 670, 430, 550, 136, C.leaf),
  ]));
  addFooter(slide);
  return slide;
}
