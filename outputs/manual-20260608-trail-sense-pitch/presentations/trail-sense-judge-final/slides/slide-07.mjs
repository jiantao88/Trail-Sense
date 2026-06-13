import { C, header, footer, h1, p, proof, node, flow, layers } from "./theme.mjs";

export default async function slide07(presentation) {
  const slide = presentation.slides.add();
  header(slide, 7, "多模态输入：户外照片和 App 截图进入本地推理链路");
  slide.compose(layers({ width: 1280, height: 720 }, [
    h1("图片是户外观察上下文。", 76, 150, 560, 58, 38),
    p("用户可以附加天空、云层、户外环境，或 Trail Sense 截图；系统把图片压缩后和文本一起送入 Gemma 4 Conversation。", 76, 230, 610, 78, 21),
    node("图片选择", "ActivityResultContracts.GetContent\n读取 image/*", 720, 150, 230, 112, C.amber),
    flow(956, 206, 1014, 206),
    node("Bitmap 处理", "按最大边缩放，减少内存压力", 1022, 150, 230, 112, C.leaf),
    node("Prompt 图片提示", "截图读数、云层、动植物、安全风险", 720, 318, 230, 128, C.sky),
    flow(956, 374, 1014, 374),
    node("LiteRT-LM 输入", "Content.ImageBytes + Content.Text\n本地 Conversation", 1022, 318, 230, 128, C.rust),
    proof("云层识别", "解释云型和潜在天气风险，连接原有云识别与天气工具。", 76, 468, 340, 112, C.amber),
    proof("截图解释", "读取界面数字、单位、标签和面板，解释当前工具读数。", 452, 468, 340, 112, C.leaf),
    proof("环境观察", "结合户外照片和 Trail Sense 工具给出观察建议。", 828, 468, 340, 112, C.sky),
  ]));
  footer(slide);
  return slide;
}
