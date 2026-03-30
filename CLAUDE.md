# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

PicMark 是一个基于 JavaFX 的桌面应用程序，用于在图片上绘制矩形标注并记录坐标信息。

## 技术栈

- Java 17
- JavaFX 21
- Apache POI 5.2.5 (Excel 处理)
- JUnit 5.10.0 (单元测试)
- Maven

## 常用命令

```bash
# 开发运行
mvn javafx:run

# 编译
mvn clean compile

# 运行测试
mvn test

# 打包（生成可执行 JAR）
mvn clean package

# 运行可执行 JAR
java -jar target/picmark-1.0-SNAPSHOT.jar

# macOS 用户可能需要的参数
java -Dprism.order=sw -jar target/picmark-1.0-SNAPSHOT.jar

# 打包为原生可执行文件
./package.sh        # macOS/Linux
package.bat         # Windows
```

## 架构概览

### 核心类

| 类 | 职责 |
|---|---|
| `PicMarkApp` | 主应用类，继承自 `Application`，负责 GUI 构建和交互逻辑 |
| `RectangleAnnotation` | 矩形标注数据模型，存储 4 个顶点坐标和 metadata |
| `AnnotationDialog` | 模态对话框，用于输入标注的描述信息 |
| `ExcelHandler` | 静态工具类，处理 Excel 文件的导入导出 |
| `Launcher` | 启动类，用于创建包含所有依赖的 fat JAR |

### 坐标系统

- **内部存储**：使用 JavaFX 视觉坐标系统，以左上角为原点 (0,0)，Y 轴向下为正方向
- **顶点顺序**：顺时针方向存储，从左上角开始：左上(x1,y1) → 右上(x2,y2) → 右下(x3,y3) → 左下(x4,y4)
- **坐标系转换**：`RectangleAnnotation` 提供 `fromVisualBounds()` 和 `getVisualBounds()` 方法进行转换

### 关键状态机

应用有两种主要模式：

1. **标注模式** (`isRectangleToolActive = true`)：光标为十字形，点击并拖拽创建新矩形
2. **选择模式** (`isRectangleToolActive = false`)：光标为手形，可选中已有矩形或拖拽移动图片

### 自动保存机制

- 当打开图片时，会自动检查同级目录下是否存在同名 Excel 文件并加载
- 添加或删除标注时，会自动保存到与图片同名的 Excel 文件
- Excel 文件名格式：`{图片名称}.xlsx`

### UI 架构

```
BorderPane (root)
├── ToolBar (顶部)
│   ├── 左侧按钮组：打开图片、标注模式、导入
│   ├── Spacer (弹性间隔)
│   └── 右侧按钮组：保存到Excel、清除全部标注、提示标签
├── StackPane (中央画布区域)
│   ├── Canvas (imageCanvas) - 绘制图片
│   └── Pane (overlayPane) - 覆盖层，用于显示矩形标注
└── HBox (底部状态栏)
    ├── 状态标签
    ├── Spacer
    └── 图片信息标签
```

## 关键实现细节

### 视口系统

- `viewportX`, `viewportY`: 图片在画布中的偏移量
- `scale`: 缩放比例（当前固定为 1.0）
- `render()` 方法负责重绘图片并更新矩形位置

### 矩形渲染

- 矩形使用 `Rectangle` 对象在 `overlayPane` 上渲染
- 矩形坐标包含 `viewport` 偏移量：`rect.setX(bounds[0] + viewportX)`
- 矩形颜色根据模式变化：标注模式为绿色，选择模式为蓝色，选中状态为红色

### 文件持久化

- 使用 `java.util.prefs.Preferences` 存储上次打开的图片路径
- Excel 文件使用 Apache POI 读写，格式为：`(x1,y1)`, `(x2,y2)`, `(x3,y3)`, `(x4,y4)`, `metadata`

## 开发注意事项

- Java 17 或更高版本是必需的
- macOS 用户可能需要 `-Dprism.order=sw` 参数解决显示问题
- 窗口会自动定位到鼠标当前所在的显示器中央
- 所有 UI 文本使用中文，代码注释使用中文
- 修改坐标相关逻辑时需注意坐标系的一致性
