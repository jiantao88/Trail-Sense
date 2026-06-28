# Trail Sense Code Wiki

> 本文档是 Trail Sense 项目（含 AI Assistant 扩展）的完整代码知识库，涵盖项目架构、模块职责、关键类、依赖关系和运行方式。

---

## 目录

1. [项目概述](#项目概述)
2. [技术栈与依赖](#技术栈与依赖)
3. [项目整体架构](#项目整体架构)
4. [核心模块详解](#核心模块详解)
5. [工具注册系统](#工具注册系统)
6. [数据持久化层](#数据持久化层)
7. [AI Assistant 模块](#ai-assistant-模块)
8. [共享基础模块](#共享基础模块)
9. [项目运行与构建](#项目运行与构建)
10. [关键类索引](#关键类索引)

---

## 项目概述

### 项目定位

Trail Sense 是一款面向徒步、露营、地理定位和生存场景的 Android 离线工具箱应用。本项目是原 [kylecorry31/Trail-Sense](https://github.com/kylecorry31/Trail-Sense) 的 fork，在原有功能基础上新增了基于 Gemma 4 的端侧 AI Assistant。

### 核心能力

- **离线优先**：所有核心工具无需网络即可运行
- **传感器驱动**：利用手机 GPS、气压计、指南针、陀螺仪、摄像头等硬件
- **49+ 内置工具**：天气、导航、信标、手电筒、天文、生存指南等
- **端侧 AI**：基于 Gemma 4 和 LiteRT-LM 的本地大语言模型助手
- **多模态支持**：AI 聊天支持图片输入
- **Function Calling**：AI 可以调用本地 Trail Sense 工具

### 基本信息

| 属性 | 值 |
|------|-----|
| 包名 | `com.kylecorry.trail_sense` |
| 版本 | 8.0.0 (versionCode 143) |
| minSdk | 23 (Android 6.0) |
| targetSdk | 37 |
| compileSdk | 37 |
| 语言 | Kotlin |
| 架构 | Clean Architecture / Feature Modular |

---

## 技术栈与依赖

### 核心技术栈

| 类别 | 技术/库 | 版本 | 用途 |
|------|---------|------|------|
| 语言 | Kotlin | - | 主要开发语言 |
| UI 框架 | Android View + Jetpack Compose | BOM 2026.05.01 | 混合 UI 架构 |
| 异步 | Kotlin Coroutines | 1.10.2 | 协程异步处理 |
| 数据库 | Room | 2.8.4 | SQLite ORM |
| 后台任务 | WorkManager | 2.11.2 | 定时任务/后台服务 |
| 导航 | AndroidX Navigation | 2.9.8 | Fragment 导航 |
| DI | Service Registry (自定义) | - | 服务注册与获取 |
| 相机 | CameraX | 1.6.0 | 相机相关工具 |
| 地图 | Mapsforge | 2d514f37cd | 离线地图渲染 |
| AI 推理 | LiteRT-LM | 0.13.1 | Gemma 4 端侧推理 |
| 代码质量 | Detekt | 2.0.0-alpha.3 | Kotlin 静态分析 |
| 测试 | JUnit 4/5 + Mockito | 5.13.4 / 6.3.0 | 单元测试 |

### 自定义库 (Andromeda)

项目大量使用作者自研的 `com.kylecorry.andromeda` 库（v21.0.0），包含以下模块：

- `core` - 核心工具类
- `fragments` - Fragment 基础架构
- `forms` - 表单组件
- `csv` / `gpx` - 数据导入导出
- `background` - 后台服务
- `camera` - 相机封装
- `sound` - 音频处理
- `sense` - 传感器封装
- `signal` - 信号处理
- `preferences` - 偏好设置
- `permissions` - 权限处理
- `canvas` - 自定义绘制
- `files` - 文件操作
- `notify` - 通知
- `alerts` - 对话框/提示
- `pickers` - 选择器
- `list` - 列表组件
- `qr` - 二维码
- `markdown` - Markdown 渲染
- `haptics` - 触觉反馈
- `bitmaps` - 图像处理
- `torch` - 手电筒
- `battery` - 电池监控
- `compression` - 压缩
- `pdf` - PDF 处理
- `exceptions` - 异常处理
- `print` - 打印
- `widgets` - App Widget
- `ipc` - 进程间通信
- `geojson` - GeoJSON 支持

### 其他重要库

- `sol` (v17.0.2) - 太阳位置计算
- `luna` (v1.3.0) - 月亮位置计算
- `orion` (v1.1.0) - Detekt 规则扩展

---

## 项目整体架构

### 目录结构

```
app/src/main/
├── assets/                    # 静态资源
│   ├── dem/                   # 数字高程模型瓦片
│   ├── dewpoint/              # 露点温度图表
│   ├── field_guide/           # 野外指南图片
│   ├── survival_guide/        # 生存指南图片
│   ├── temperatures/          # 温度图表
│   └── tides/                 # 潮汐图表
├── java/com/kylecorry/trail_sense/
│   ├── main/                  # 应用入口与核心配置
│   ├── shared/                # 共享基础模块
│   ├── tools/                 # 所有工具模块（核心业务）
│   │   ├── tools/             # 工具注册系统（基础设施）
│   │   ├── ai_assistant/      # AI 助手（新增模块）
│   │   ├── astronomy/         # 天文工具
│   │   ├── navigation/        # 导航（指南针）
│   │   ├── weather/           # 天气
│   │   ├── beacons/           # 信标
│   │   ├── paths/             # 路径记录
│   │   ├── flashlight/        # 手电筒
│   │   ├── whistle/           # 哨子
│   │   └── ... (49 个工具)
│   ├── settings/              # 设置模块
│   ├── onboarding/            # 引导页
│   ├── plugins/               # 插件系统
│   └── receivers/             # 广播接收器
├── res/                       # Android 资源
│   ├── raw/                   # 原始资源（含 AI 知识库）
│   └── values-*/              # 多语言字符串
└── AndroidManifest.xml        # 应用清单
```

### 分层架构

项目采用类似 Clean Architecture 的分层设计，每个工具模块内部遵循以下分层：

```
tools/<tool_name>/
├── domain/           # 领域层：业务逻辑、模型、服务接口
├── infrastructure/   # 基础设施层：Repository 实现、持久化、平台服务
│   └── persistence/  # 数据库 DAO/Entity
├── ui/               # 展示层：Fragment、View、ViewModel、列表适配器
├── services/         # Android 前台服务
├── widgets/          # App Widget
├── quickactions/     # 快捷操作
└── <Tool>ToolRegistration.kt  # 工具注册入口
```

### 应用启动流程

```
TrailSenseApplication.onCreate()
    ↓
TrailSenseApplicationInitializer.initialize()
    ↓
TrailSenseServiceRegister.setup(context)
    ├── 注册共享服务（StringLoader, FormatService, Preferences 等）
    ├── 注册传感器/位置/文件/通知等子系统
    ├── 注册 Room 数据库
    ├── 注册地图图层加载器
    └── 遍历所有工具注册，注册每个工具的单例服务
    ↓
MainActivity 启动
    ↓
导航图加载，显示工具列表
```

---

## 核心模块详解

### 1. 主入口模块 (`main/`)

**路径**：[main/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main)

| 类/文件 | 职责 |
|---------|------|
| [TrailSenseApplication.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/TrailSenseApplication.kt) | Application 入口，初始化 CameraX 配置 |
| [TrailSenseServiceRegister.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/TrailSenseServiceRegister.kt) | 全局服务注册中心，使用 `AppServiceRegistry` 管理单例 |
| [TrailSenseApplicationInitializer.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/TrailSenseApplicationInitializer.kt) | 应用初始化器，协调各子系统启动 |
| [MainActivity.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/MainActivity.kt) | 主 Activity，托管导航图和底部导航 |
| [AppDatabase.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/persistence/AppDatabase.kt) | Room 数据库主类，包含所有 Entity 和 DAO |
| [NotificationChannels.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/NotificationChannels.kt) | 通知渠道定义 |
| [errors/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/errors) | 全局异常处理和安全模式 |

**关键函数**：

```kotlin
// 获取已注册的应用服务
inline fun <reified T : Any> getAppService(): T
```

### 2. 工具基础设施模块 (`tools/tools/`)

**路径**：[tools/tools/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/tools)

这是整个应用的工具注册和管理核心。

#### 核心类：Tool

[Tool.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/tools/infrastructure/Tool.kt) 是工具的数据类定义：

```kotlin
data class Tool(
    val id: Long,                          // 工具唯一 ID
    val name: String,                      // 工具名称
    val icon: Int,                         // 图标资源
    val navAction: Int,                    // 导航目的地 ID
    val category: ToolCategory,            // 分类
    val description: String?,              // 描述
    val settingsNavAction: Int?,           // 设置页导航 ID
    val quickActions: List<ToolQuickAction>, // 快捷操作
    val widgets: List<ToolWidget>,         // App Widget
    val services: List<ToolService>,       // 后台服务
    val diagnostics: List<ToolDiagnostic>, // 诊断项
    val isAvailable: (Context) -> Boolean, // 可用性检查
    val singletons: List<(Context) -> Any>,// 单例服务工厂
    val mapLayers: List<MapLayerDefinition>,// 地图图层
    // ...
)
```

#### 核心类：Tools (工具注册表)

[Tools.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/tools/infrastructure/Tools.kt) 是单例对象，维护所有工具的注册列表：

| 常量 ID | 值 | 工具名称 |
|---------|-----|---------|
| FLASHLIGHT | 1 | 手电筒 |
| WHISTLE | 2 | 哨子 |
| RULER | 3 | 尺子 |
| PEDOMETER | 4 | 计步器 |
| CLIFF_HEIGHT | 5 | 悬崖高度 |
| NAVIGATION | 6 | 导航（指南针） |
| BEACONS | 7 | 信标 |
| OFFLINE_MAPS | 8 | 离线地图 |
| PATHS | 9 | 路径 |
| WEATHER | 20 | 天气 |
| ASTRONOMY | 14 | 天文 |
| CLOCK | 13 | 时钟 |
| CLINOMETER | 11 | 倾角仪 |
| BUBBLE_LEVEL | 12 | 水平仪 |
| CLOUDS | 23 | 云识别 |
| TIDES | 16 | 潮汐 |
| BATTERY | 17 | 电池 |
| AI_ASSISTANT | 49 | AI 助手（新增） |
| ... | ... | 共 49 个工具 |

**关键方法**：

| 方法 | 职责 |
|------|------|
| `getTools(context, availableOnly)` | 获取所有工具列表 |
| `getTool(context, toolId)` | 根据 ID 获取单个工具 |
| `isToolAvailable(context, toolId)` | 检查工具是否可用 |
| `getQuickActions(context)` | 获取所有快捷操作 |
| `getService(context, serviceId)` | 获取工具服务 |
| `broadcast(toolBroadcastId, data)` | 发送工具广播 |
| `subscribe(toolBroadcastId, callback)` | 订阅工具广播 |
| `triggerWidgetUpdate(context, widgetId)` | 触发 Widget 更新 |

### 3. 主要工具模块概览

| 工具模块 | 路径 | 主要功能 |
|---------|------|---------|
| **AI Assistant** | [tools/ai_assistant/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant) | Gemma 4 端侧 AI 助手、聊天、Function Calling |
| **Astronomy** | [tools/astronomy/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/astronomy) | 日出日落、月相、日食月食、行星位置 |
| **Navigation** | [tools/navigation/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/navigation) | 指南针、方位导航、目的地导航 |
| **Weather** | [tools/weather/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/weather) | 气压趋势、天气预报、风暴预警 |
| **Beacons** | [tools/beacons/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/beacons) | 位置信标、导航点管理、分组 |
| **Paths** | [tools/paths/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/paths) | 轨迹记录、回溯导航（Backtrack） |
| **Flashlight** | [tools/flashlight/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/flashlight) | 手电筒、SOS 闪光、屏幕手电 |
| **Whistle** | [tools/whistle/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/whistle) | 摩尔斯码哨子、SOS 求救 |
| **Clouds** | [tools/clouds/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/clouds) | 云层拍照识别、云分类 |
| **Clinometer** | [tools/clinometer/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/clinometer) | 倾角测量（树木/悬崖高度） |
| **Bubble Level** | [tools/level/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/level) | 水平仪 |
| **Clock** | [tools/clock/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/clock) | 时钟、日出日落提醒 |
| **Tides** | [tools/tides/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/tides) | 潮汐表、潮汐预报 |
| **Battery** | [tools/battery/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/battery) | 电池监控、电量统计、低电量模式 |
| **Map** | [tools/map/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/map) | 离线地图、等高线 |
| **Augmented Reality** | [tools/augmented_reality/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/augmented_reality) | AR 导航、相机叠加信息 |
| **Field Guide** | [tools/field_guide/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/field_guide) | 动植物野外指南 |
| **Survival Guide** | [tools/survival_guide/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/survival_guide) | 生存技能指南 |
| **Convert** | [tools/convert/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/convert) | 单位换算（距离/温度/重量/坐标等） |
| **Sensors** | [tools/sensors/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/sensors) | 传感器详情、诊断 |
| **Diagnostics** | [tools/diagnostics/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/diagnostics) | 系统诊断、传感器状态 |
| **Metal Detector** | [tools/metaldetector/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/metaldetector) | 金属探测（磁力计） |
| **Light Meter** | [tools/light/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/light) | 照度计 |
| **Magnifier** | [tools/magnifier/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/magnifier) | 放大镜（相机放大） |
| **Ruler** | [tools/ruler/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ruler) | 尺子（屏幕测量） |
| **White Noise** | [tools/whitenoise/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/whitenoise) | 白噪音 |
| **Notes** | [tools/notes/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/notes) | 笔记 |
| **Packing Lists** | [tools/packs/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/packs) | 打包清单 |
| **QR Scanner** | [tools/qr/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/qr) | 二维码扫描 |
| **Water Boil Timer** | [tools/waterpurification/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/waterpurification) | 净水煮沸计时器 |
| **Turn Back** | [tools/turn_back/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/turn_back) | 返程提醒 |
| **Climate** | [tools/climate/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/climate) | 气候数据、物候 |
| **Lightning Strike** | [tools/lightning/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/lightning) | 闪电距离计算 |
| **Solar Panel Aligner** | [tools/solarpanel/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/solarpanel) | 太阳能板对准 |
| **Temperature Estimation** | [tools/temperature_estimation/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/temperature_estimation) | 温度估算 |
| **Declination** | [tools/declination/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/declination) | 磁偏角 |
| **Ballistics** | [tools/ballistics/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ballistics) | 弹道计算 |
| **Triangulate** | [tools/triangulate/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/triangulate) | 三角定位 |
| **Mirror Camera** | [tools/mirror/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/mirror) | 镜子（前置相机） |
| **Local Messaging/Talk** | [tools/comms/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/comms) | 近场通信 |
| **Signal Finder** | [tools/signal_finder/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/signal_finder) | 信号搜索 |
| **Permits** | [tools/permits/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/permits) | 许可证管理 |
| **Settings** | [settings/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/settings) | 设置（作为工具注册） |
| **User Guide** | [tools/guide/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/guide) | 用户指南 |

---

## 数据持久化层

### Room 数据库

[AppDatabase.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/persistence/AppDatabase.kt) 是应用的主数据库，当前版本 59，包含 28 张表：

| 表名 | Entity | DAO | 所属模块 | 用途 |
|------|--------|-----|---------|------|
| `packs` | PackEntity | PackDao | packs | 打包清单 |
| `items` | PackItemEntity | PackItemDao | packs | 清单项 |
| `notes` | Note | NoteDao | notes | 笔记 |
| `waypoints` | WaypointEntity | WaypointDao | paths | 路径航点 |
| `paths` | PathEntity | PathDao | paths | 路径 |
| `path_groups` | PathGroupEntity | PathGroupDao | paths | 路径分组 |
| `pressures` | PressureReadingEntity | PressureReadingDao | weather | 气压读数 |
| `beacons` | BeaconEntity | BeaconDao | beacons | 信标 |
| `beacon_groups` | BeaconGroupEntity | BeaconGroupDao | beacons | 信标分组 |
| `maps` / `offline_map_files` | PhotoMapEntity / VectorMapEntity | PhotoMapDao / VectorMapDao | offline_maps | 离线地图 |
| `map_groups` | MapGroupEntity | MapGroupDao | offline_maps | 地图分组 |
| `battery` | BatteryReadingEntity | BatteryDao | battery | 电池读数 |
| `clouds` | CloudReadingEntity | CloudReadingDao | clouds | 云观测记录 |
| `tide_tables` | TideTableEntity | TideTableDao | tides | 潮汐表 |
| `tide_table_rows` | TideTableRowEntity | - | tides | 潮汐数据行 |
| `tide_constituents` | TideConstituentEntry | - | tides | 潮汐调和常数 |
| `lightning` | LightningStrikeEntity | LightningStrikeDao | lightning | 闪电记录 |
| `field_guide_pages` | FieldGuidePageEntity | FieldGuidePageDao | field_guide | 野外指南页面 |
| `field_guide_sightings` | FieldGuideSightingEntity | FieldGuideSightingDao | field_guide | 野外观察记录 |
| `dem` | DigitalElevationModelEntity | DigitalElevationModelDao | shared/dem | 数字高程模型 |
| `navigation_bearings` | NavigationBearingEntity | NavigationBearingDao | navigation | 导航方位记录 |
| `cached_tiles` | CachedTileEntity | CachedTileDao | shared/map_layers | 地图瓦片缓存 |
| `plugins` | PluginEntity | PluginDao | plugins | 插件信息 |
| `plugin_registrations` | PluginRegistrationEntity | PluginRegistrationDao | plugins | 插件注册 |
| `ai_chat_sessions` | ChatSessionEntity | AiChatDao | ai_assistant | AI 聊天会话 |
| `ai_chat_messages` | ChatMessageEntity | AiChatDao | ai_assistant | AI 聊天消息 |

### 数据库迁移

数据库从版本 1 到 59 有完整的 Migration 链，每次升级都通过显式的 `Migration` 对象处理，确保用户数据平滑升级。AI 相关表在迁移 56→57→58→59 中添加：

- MIGRATION_56_57: 创建 `ai_chat_sessions` 和 `ai_chat_messages` 表
- MIGRATION_57_58: 添加 `image_path` 字段（支持图片消息）
- MIGRATION_58_59: 添加 `tool_calls_json` 字段（支持 Tool Call 记录）

### SharedPreferences

除了 Room 数据库，应用还使用 SharedPreferences 存储配置项，通过 [PreferencesSubsystem](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/preferences/PreferencesSubsystem.kt) 和 [UserPreferences](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/UserPreferences.kt) 统一管理。

---

## AI Assistant 模块

**路径**：[tools/ai_assistant/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant)

这是本项目的核心扩展模块，基于 Gemma 4 和 LiteRT-LM 实现端侧 AI 助手。

### 模块结构

```
ai_assistant/
├── domain/                           # 领域层
│   ├── AiContext.kt                  # AI 上下文数据类
│   ├── AiContextProvider.kt          # 上下文提供者接口
│   ├── AiFastPathDetector.kt         # 快速路径检测器（性能优化）
│   ├── AiGuideExcerptExtractor.kt    # 指南摘录提取器
│   ├── AiPromptBuilder.kt            # 系统 Prompt 构建器
│   ├── AiSkillRun.kt                 # 技能运行结果
│   ├── AiToolCallCard.kt             # Tool Call 卡片
│   ├── AiToolKnowledgeEntry.kt       # 工具知识条目
│   ├── AiToolKnowledgeMatcher.kt     # 工具知识匹配器
│   ├── AiToolKnowledgeParser.kt      # 工具知识解析器
│   ├── AiToolKnowledgeService.kt     # 工具知识服务
│   ├── AiToolRunResult.kt            # 工具运行结果
│   ├── AiToolRunStatus.kt            # 工具运行状态
│   ├── AiToolSkillEntry.kt           # 工具技能条目
│   ├── AiToolSkillMatcher.kt         # 工具技能匹配器
│   ├── AiToolSkillParser.kt          # 工具技能解析器
│   ├── AiToolSkillService.kt         # 工具技能服务
│   ├── WeatherAiContextProvider.kt   # 天气上下文提供者
│   ├── CloudAiContextProvider.kt     # 云层上下文提供者
│   └── NavigationAiContextProvider.kt # 导航上下文提供者
├── infrastructure/                   # 基础设施层
│   ├── persistence/                  # 持久化
│   │   ├── AiChatDao.kt              # 聊天 DAO
│   │   ├── AiChatRepo.kt             # 聊天仓库
│   │   ├── ChatSessionEntity.kt      # 会话 Entity
│   │   └── ChatMessageEntity.kt      # 消息 Entity
│   ├── AiAssistantTools.kt           # AI 可调用工具定义
│   ├── AiInferenceSubsystem.kt       # LiteRT-LM 推理封装（核心）
│   ├── AiPerformanceMonitor.kt       # 性能监控
│   ├── AiToolExecutionService.kt     # 工具执行服务
│   ├── AiTrailSenseToolRunner.kt     # TrailSense 工具运行器
│   ├── ModelManager.kt               # 模型下载与管理
│   └── TrailSenseAiToolRunner.kt     # 工具运行器接口
├── ui/                               # 展示层
│   ├── AiAssistantFragment.kt        # 聊天主界面
│   └── AiSettingsFragment.kt         # AI 设置页
└── AiAssistantToolRegistration.kt    # 工具注册入口
```

### AI 推理核心：AiInferenceSubsystem

[AiInferenceSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiInferenceSubsystem.kt) 是 LiteRT-LM 的核心封装，单例模式。

**关键配置**：

| 参数 | 值 | 说明 |
|------|-----|------|
| MAX_TOKENS | 4096 | 最大生成 token 数 |
| MAX_IMAGE_SIZE | 1024 | 图片最大边长（像素） |
| IMAGE_QUALITY | 85 | JPEG 压缩质量 |
| DEFAULT_TOP_K | 64 | Top-K 采样 |
| DEFAULT_TOP_P | 0.95 | Top-P 采样 |
| DEFAULT_TEMPERATURE | 1.0 | 温度系数 |

**关键方法**：

| 方法 | 职责 |
|------|------|
| `initialize()` | 初始化引擎（优先 GPU，失败回退 CPU） |
| `createConversation(systemInstruction, tools)` | 创建带系统指令和工具的会话 |
| `sendMessage(input, images, callback)` | 发送文本和图片消息，异步回调 |
| `stopResponse()` | 停止生成 |
| `isModelAvailable()` | 检查模型是否已下载 |
| `supportsImages()` | 检查模型是否支持多模态 |
| `cleanup()` | 释放资源 |

**Backend 选择策略**：
1. 首先尝试 GPU Backend
2. GPU 初始化失败（如设备不支持）自动回退到 CPU Backend
3. 多模态模型同时设置 visionBackend

### 模型管理：ModelManager

[ModelManager.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/ModelManager.kt) 负责 Gemma 4 模型的生命周期管理。

**支持的模型**：

| 模型 | 参数 | 多模态 | 说明 |
|------|------|--------|------|
| Gemma-4-E2B-it | 2B | 是 | 默认模型，平衡速度和质量 |
| Gemma-4-E4B-it | 4B | 是 | 更高质量，更慢 |

**功能**：
- 模型列表管理
- 默认模型选择
- 断点续传下载（Hugging Face）
- 本地模型路径管理
- 模型删除和磁盘大小统计

### AI 可调用工具：AiAssistantTools

[AiAssistantTools.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiAssistantTools.kt) 定义了 Gemma 4 可以通过 Function Calling 调用的本地工具。

AI 可调用的能力包括：

| 工具类型 | 功能 |
|---------|------|
| 天气上下文 | 查询当前气压、天气趋势、预报 |
| 导航上下文 | 查询位置、方位、目的地信息 |
| 云层上下文 | 查询云识别结果、云层观察 |
| SOS 手电筒 | 生成打开 SOS 手电筒的行动卡片 |
| 哨子求救 | 生成打开 SOS 哨子的行动卡片 |
| 工具导航 | 返回打开某个 Trail Sense 工具的导航 Action |
| 工具推荐 | 根据工具知识库推荐合适工具 |

**隐私保证**：所有工具调用只读取本机 App 数据或准备本机动作，不上传云端。

### Prompt 与知识库

AI 系统 Prompt 由 [AiPromptBuilder](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiPromptBuilder.kt) 构建，整合以下资源：

1. **系统指令**：定义 AI 角色、安全约束、回答风格
2. **工具知识库**：[ai_tool_knowledge.md](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/res/raw/ai_tool_knowledge.md) - 每个工具的详细说明、入口、读数解释
3. **工具技能库**：[ai_tool_skills.md](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/res/raw/ai_tool_skills.md) - 技能分类和触发条件
4. **实时上下文**：来自 WeatherAiContextProvider、CloudAiContextProvider、NavigationAiContextProvider

**性能优化 - 快速路径**：

[AiFastPathDetector](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiFastPathDetector.kt) 检测用户问题是否为通用知识问答（无需实时工具数据）：
- **快速路径**（通用知识）：跳过工具数据加载和技能匹配，响应快 30-50%
- **完整路径**（需要实时数据）：加载完整上下文和工具

### 聊天持久化

- [ChatSessionEntity](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/persistence/ChatSessionEntity.kt) - 会话：ID、标题、创建/更新时间
- [ChatMessageEntity](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/persistence/ChatMessageEntity.kt) - 消息：会话ID、文本、是否用户、时间戳、图片路径、Tool Call JSON
- [AiChatRepo](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/persistence/AiChatRepo.kt) - 聊天数据仓库

---

## 共享基础模块

**路径**：[shared/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared)

### 核心子系统

| 子系统 | 文件/目录 | 职责 |
|--------|----------|------|
| 传感器服务 | [sensors/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/sensors) | GPS、指南针、气压计、加速度计、陀螺仪等统一封装 |
| 位置子系统 | [sensors/LocationSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/sensors/LocationSubsystem.kt) | 位置管理、坐标转换 |
| 偏好设置 | [preferences/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/preferences) | SharedPreferences 封装、Flag 管理 |
| 通知 | [alerts/NotificationSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/alerts/NotificationSubsystem.kt) | 通知渠道、通知发送 |
| 触觉反馈 | [haptics/HapticSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/haptics) | 震动反馈 |
| 文件操作 | [io/FileSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/io) | 文件读写、导入导出（GPX/CSV） |
| 设备信息 | [device/DeviceSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/device) | 电池、网络、屏幕等设备状态 |
| 格式化服务 | [FormatService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/FormatService.kt) | 距离、方位、时间、坐标等格式化 |
| 单位管理 | [Units.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/Units.kt) | 公制/英制单位转换 |
| 地图图层 | [map_layers/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/map_layers) | 瓦片地图、图层管理、瓦片缓存 |
| 数字高程模型 | [dem/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/dem) | DEM 加载、海拔查询、坡度计算 |
| 磁偏角 | [declination/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/declination) | 磁偏角计算（WMM 模型） |
| 摩尔斯码 | [morse/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/morse) | 摩尔斯码编码、信号播放 |
| 权限处理 | [permissions/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/permissions) | Android 权限请求封装 |
| 分组管理 | [grouping/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/grouping) | 可分组项目的通用管理（信标、路径等） |
| 并发工具 | [concurrency/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/concurrency) | 锁、调度器 |
| 扩展函数 | [extensions/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/extensions) | Kotlin 扩展函数集合 |
| 绘图工具 | [canvas/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/canvas) | 自定义 Canvas 绘制工具 |
| 颜色工具 | [colors/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/colors) | 颜色定义、颜色比例 |

### 服务获取机制

应用使用自定义的服务注册机制，而非 Hilt/Dagger 等 DI 框架：

```kotlin
// 注册服务
AppServiceRegistry.register(SomeService(context))

// 获取服务
val service = getAppService<SomeService>()
```

全局服务在 [TrailSenseServiceRegister.setup()](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/TrailSenseServiceRegister.kt#L28-L70) 中统一注册。

---

## 项目运行与构建

### 环境要求

| 工具 | 版本要求 |
|------|---------|
| Android Studio | Narwhal 或更新版本 |
| JDK | 21 |
| Android SDK | compileSdk 37 |
| Gradle | 项目自带 wrapper |
| Kotlin | 与 AGP 匹配 |

### 构建命令

```bash
# 构建 Debug APK
./gradlew :app:assembleDebug

# 构建 Release APK (unsigned)
./gradlew :app:assembleRelease

# 构建 GitHub Release (签名)
./gradlew :app:assembleGithub

# 构建 Nightly 版本
./gradlew :app:assembleNightly

# 运行单元测试
./gradlew :app:testDebugUnitTest

# 运行 AI 相关性能测试
./gradlew :app:testDebugUnitTest --tests "*AiAssistantPerformanceTest"

# 安装 Debug APK 到设备
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 构建变体

| Build Type | 签名 | 说明 |
|-----------|------|------|
| debug | debug 签名 | 本地开发 |
| staging | debug 签名 | 预发布测试，applicationIdSuffix=.staging |
| release | 无（unsigned）| F-Droid 发布 |
| playStore | 无 | Google Play 发布 |
| github | dev 签名 | GitHub Release |
| nightly | nightly 签名 | 每夜构建 |
| nightlyRelease | nightly 签名 | 每夜构建 release 版 |

### APK 输出路径

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

### 权限说明

[AndroidManifest.xml](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/AndroidManifest.xml) 声明的权限：

| 权限 | 用途 |
|------|------|
| ACCESS_FINE_LOCATION | GPS 定位 |
| ACCESS_COARSE_LOCATION | 网络定位 |
| ACCESS_BACKGROUND_LOCATION | 后台定位（路径记录/天气监测） |
| CAMERA | 相机（云识别、放大镜、AR、镜子） |
| FLASHLIGHT | 手电筒 |
| VIBRATE | 触觉反馈 |
| WAKE_LOCK | 后台服务唤醒 |
| RECEIVE_BOOT_COMPLETED | 开机自启（恢复后台服务） |
| FOREGROUND_SERVICE | 前台服务 |
| ACTIVITY_RECOGNITION | 计步器 |
| POST_NOTIFICATIONS | 通知 |
| INTERNET | 模型下载、插件下载（核心功能离线） |
| SCHEDULE_EXACT_ALARM | 精确闹钟（日出日落提醒） |

### 前台服务

| 服务 | 类型 | 功能 |
|------|------|------|
| FlashlightService | - | 手电筒（屏幕常亮） |
| WaterPurificationTimerService | specialUse | 净水计时器 |
| BacktrackService | location | 轨迹回溯记录 |
| WeatherMonitorService | location\|specialUse | 天气监测 |
| StepCounterService | health | 计步器 |
| WhiteNoiseService | mediaPlayback | 白噪音播放 |

### App Widget

应用提供 15+ 个桌面小组件：
- 月亮/月亮相位、太阳/日出日落
- 天气、气压、气压图
- 潮汐、潮汐图
- 计步器
- 回溯（Backtrack）
- 位置、海拔
- 手电筒
- 日月图表
- 附近信标
- 地图

### Quick Settings Tiles

- Backtrack（路径回溯）
- Weather Monitor（天气监测）
- Pedometer（计步器）

---

## 关键类索引

### 应用核心

| 类名 | 文件路径 | 一句话说明 |
|------|---------|-----------|
| TrailSenseApplication | [TrailSenseApplication.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/TrailSenseApplication.kt) | Application 入口 |
| TrailSenseServiceRegister | [TrailSenseServiceRegister.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/TrailSenseServiceRegister.kt) | 全局服务注册中心 |
| MainActivity | [MainActivity.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/MainActivity.kt) | 主 Activity |
| AppDatabase | [AppDatabase.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/main/persistence/AppDatabase.kt) | Room 数据库 |
| Tools | [Tools.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/tools/infrastructure/Tools.kt) | 工具注册表单例 |
| Tool | [Tool.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/tools/infrastructure/Tool.kt) | 工具数据类 |
| UserPreferences | [UserPreferences.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/UserPreferences.kt) | 用户偏好设置 |
| FormatService | [FormatService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/FormatService.kt) | 数据格式化服务 |

### AI Assistant

| 类名 | 文件路径 | 一句话说明 |
|------|---------|-----------|
| AiInferenceSubsystem | [AiInferenceSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiInferenceSubsystem.kt) | LiteRT-LM 推理封装 |
| ModelManager | [ModelManager.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/ModelManager.kt) | Gemma 4 模型管理 |
| AiAssistantTools | [AiAssistantTools.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiAssistantTools.kt) | AI 可调用工具定义 |
| AiPromptBuilder | [AiPromptBuilder.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiPromptBuilder.kt) | 系统 Prompt 构建 |
| AiToolKnowledgeService | [AiToolKnowledgeService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolKnowledgeService.kt) | 工具知识服务 |
| AiToolSkillService | [AiToolSkillService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiToolSkillService.kt) | 工具技能服务 |
| AiFastPathDetector | [AiFastPathDetector.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/AiFastPathDetector.kt) | 快速路径检测器 |
| AiToolExecutionService | [AiToolExecutionService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiToolExecutionService.kt) | 工具执行服务 |
| WeatherAiContextProvider | [WeatherAiContextProvider.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/domain/WeatherAiContextProvider.kt) | 天气上下文提供者 |
| AiAssistantFragment | [AiAssistantFragment.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/ui/AiAssistantFragment.kt) | AI 聊天界面 |
| AiSettingsFragment | [AiSettingsFragment.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/ui/AiSettingsFragment.kt) | AI 设置页 |
| AiPerformanceMonitor | [AiPerformanceMonitor.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/infrastructure/AiPerformanceMonitor.kt) | 性能监控 |

### 传感器与位置

| 类名 | 文件路径 | 一句话说明 |
|------|---------|-----------|
| SensorService | [sensors/SensorService.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/sensors/SensorService.kt) | 传感器统一服务 |
| LocationSubsystem | [sensors/LocationSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/sensors/LocationSubsystem.kt) | 位置子系统 |
| SensorSubsystem | [sensors/SensorSubsystem.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/shared/sensors/SensorSubsystem.kt) | 传感器子系统 |

### 主要工具入口

每个工具的入口都是一个 `*ToolRegistration.kt` 单例对象，实现 `ToolRegistration` 接口：

| 工具 | 注册类 |
|------|--------|
| AI Assistant | [AiAssistantToolRegistration.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/ai_assistant/AiAssistantToolRegistration.kt) |
| Weather | [WeatherToolRegistration.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/weather/WeatherToolRegistration.kt) |
| Navigation | [NavigationToolRegistration.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/navigation/NavigationToolRegistration.kt) |
| Astronomy | [AstronomyToolRegistration.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/astronomy/AstronomyToolRegistration.kt) |
| Beacons | [BeaconsToolRegistration.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/beacons/BeaconsToolRegistration.kt) |
| Flashlight | [FlashlightToolRegistration.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/flashlight/FlashlightToolRegistration.kt) |

---

## 工具注册流程示例

以 AI Assistant 为例，工具注册的最小实现：

```kotlin
// AiAssistantToolRegistration.kt
object AiAssistantToolRegistration : ToolRegistration {
    override fun getTool(context: Context): Tool {
        return Tool(
            Tools.AI_ASSISTANT,                          // 工具 ID
            context.getString(R.string.tool_ai_assistant_title),  // 名称
            R.drawable.ic_ai_assistant,                  // 图标
            R.id.aiAssistantFragment,                    // 导航目的地
            ToolCategory.Other,                          // 分类
            settingsNavAction = R.id.aiSettingsFragment  // 设置页（可选）
            // 还可以配置: quickActions, widgets, services, 
            //           singletons, diagnostics, mapLayers 等
        )
    }
}
```

然后在 [Tools.kt](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/tools/tools/infrastructure/Tools.kt#L74-L124) 的 `registry` 列表中添加该注册对象即可完成工具注册。

---

## AI 对话流程

```
用户输入问题
    ↓
AiAssistantFragment 接收输入
    ↓
AiFastPathDetector 判断路径
    ├── 快速路径：通用知识问答 → 精简 Prompt → 直接推理
    └── 完整路径：
        ├── AiToolSkillService 匹配技能
        ├── AiContextProvider 收集实时上下文（天气/导航/云层）
        └── AiPromptBuilder 构建完整系统 Prompt
    ↓
AiInferenceSubsystem.sendMessage()
    ↓
LiteRT-LM 流式生成回复
    ├── 普通文本 → 显示在聊天界面
    └── Tool Call 请求 → AiToolExecutionService 执行
        ├── 查询本地数据 → 返回结果给模型继续生成
        └── 生成 Action Card（打开工具/SOS）→ 显示可点击卡片
    ↓
回复完成 → 保存到本地数据库
```

---

## 插件系统

项目包含一个基础的插件系统（[plugins/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/app/src/main/java/com/kylecorry/trail_sense/plugins)），支持：

- 第三方地图图层（GeoJSON、瓦片源）
- 插件签名验证
- 插件生命周期管理

插件开发文档参见 [docs/plugins/](file:///Users/zhangjiantao/Documents/GitHub/Trail-Sense/docs/plugins)。

---

## 测试

### 运行测试

```bash
# 所有单元测试
./gradlew :app:testDebugUnitTest

# 特定测试类
./gradlew :app:testDebugUnitTest --tests "*AiFastPathDetectorTest"
./gradlew :app:testDebugUnitTest --tests "*AiAssistantPerformanceTest"
```

### AI 模块已覆盖的测试

- Prompt 构建测试
- 工具知识解析和匹配测试
- 工具技能解析和匹配测试
- 模型管理测试
- 工具执行服务测试
- 天气上下文 Provider 测试
- 快速路径检测测试
- 性能基准测试

---

## 多语言支持

应用支持 30+ 种语言，字符串资源位于 `app/src/main/res/values-*/` 目录：

- 英语 (默认)、中文、日语、韩语
- 西班牙语、法语、德语、意大利语、葡萄牙语
- 俄语、波兰语、荷兰语、丹麦语、芬兰语
- 阿拉伯语、希伯来语、印地语、孟加拉语
- 以及更多...

野外指南和用户指南的翻译文本位于 `guides/` 目录。

---

## 安全注意事项

1. **AI 只是辅助**：Trail Sense 和 AI Assistant 都只能作为户外辅助工具，不能替代真实地图、指南针、通信装备和专业救援
2. **端侧部署**：AI 模型在本地运行，聊天记录、位置、传感器数据不上传云端
3. **网络使用**：仅首次下载模型需要网络，核心功能完全离线
4. **位置权限**：后台位置权限用于路径记录和天气监测，数据仅在本地使用

---

*文档版本：1.0 | 最后更新：2026-06-25*
