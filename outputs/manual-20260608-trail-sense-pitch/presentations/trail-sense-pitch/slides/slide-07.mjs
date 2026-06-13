import { addBase, addFooter, C, arrow, body, featureCard, nodeBox, shape, strong, text, layers, noLine } from "./theme.mjs";

export default async function slide07(presentation) {
  const slide = presentation.slides.add();
  addBase(slide, 7, "多模态图片输入：从户外照片和 App 截图进入本地推理链路");
  slide.compose(layers({ width: 1280, height: 720 }, [
    strong("图片不是装饰，是户外观察上下文。", 76, 148, 560, 54, 36),
    body("用户可以附加天空、云层、户外环境，或 Trail Sense 截图；系统把图片压缩后和文本一起送入 Gemma 4 Conversation。", 76, 218, 580, 86, 21, C.slate),
    nodeBox("图片选择", "ActivityResultContracts.GetContent\n读取 image/*", 720, 150, 230, 112, C.white, C.amber),
    arrow(955, 206, 1015, 206, C.moss),
    nodeBox("Bitmap 处理", "按最大边缩放\n减少内存压力", 1022, 150, 230, 112, C.white, C.leaf),
    nodeBox("Prompt 图片提示", "Attached image\n截图读数、云层、动植物、安全风险", 720, 318, 230, 128, C.white, C.sky),
    arrow(955, 374, 1015, 374, C.moss),
    nodeBox("LiteRT-LM 输入", "Content.ImageBytes + Content.Text\n本地 Conversation", 1022, 318, 230, 128, C.white, C.rust),
    featureCard("云层识别", "解释云型和潜在天气风险，连接原有云识别/天气工具。", 76, 460, 340, 118, C.amber),
    featureCard("截图解释", "读取界面上的数字、单位、标签和面板，解释当前工具读数。", 452, 460, 340, 118, C.leaf),
    featureCard("环境观察", "结合户外照片和 Trail Sense 工具，给出观察建议和安全提醒。", 828, 460, 340, 118, C.sky),
  ]));
  addFooter(slide);
  return slide;
}
