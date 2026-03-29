# PicMark - 项目开发者指南

本文件面向 AI 编码助手，提供项目架构、开发规范和技术细节的全面说明。

## 项目概述

**PicMark** 是一个基于 JavaFX 的桌面应用程序，用于在图片上绘制矩形标注并记录坐标信息。主要功能包括：

- 打开并浏览 PNG、JPG、JPEG、GIF、BMP 格式图片
- 使用矩形工具在图片上框选区域并记录坐标
- 坐标系以图片左下角为原点 (0,0)，X轴向右为正，Y轴向上为正
- 支持为每个标注添加文字描述（metadata）
- 将标注数据导出为 Excel 文件，或从 Excel 导入标注

## 技术栈

- **Java**: 17
- **JavaFX**: 21（GUI 框架）
- **Apache POI**: 5.2.5（Excel 文件处理）
- **JUnit**: 5.10.0（单元测试）
- **Maven**: 构建工具

## 项目结构

```
picmark/
├── pom.xml                          # Maven 主配置文件
├── dependency-reduced-pom.xml       # maven-shade-plugin 生成的依赖简化配置
├── README.md                        # 用户说明文档（中文）
├── AGENTS.md                        # 本文件 - AI 开发者指南
├── src/
│   ├── main/java/com/example/demo/
│   │   ├── PicMarkApp.java          # 主应用类，包含 GUI 和交互逻辑
│   │   ├── Launcher.java            # 可执行 JAR 的入口类
│   │   ├── RectangleAnnotation.java # 矩形标注数据模型
│   │   ├── AnnotationDialog.java    # 标注信息对话框
│   │   └── ExcelHandler.java        # Excel 导入导出处理器
│   ├── main/resources/              # 资源目录（当前为空）
│   └── test/java/com/example/demo/
│       └── ExcelHandlerTest.java    # ExcelHandler 单元测试
└── target/                          # Maven 构建输出目录
```

## 代码组织

### 1. PicMarkApp.java（主应用）
- 继承 `javafx.application.Application`
- 负责构建整个 GUI 界面（工具栏、画布区域、状态栏）
- 处理用户交互：图片拖拽、矩形绘制、标注删除
- 协调其他组件：调用 `ExcelHandler` 进行文件操作，使用 `AnnotationDialog` 收集用户输入
- **坐标系统转换**：JavaFX 使用左上角为原点，而业务逻辑使用左下角为原点，需要双向转换

### 2. RectangleAnnotation.java（数据模型）
- 存储矩形的 4 个顶点坐标（按顺时针：左下→右下→右上→左上）
- 坐标存储使用**左下角为原点**的坐标系
- 提供 `fromVisualBounds()` 和 `getVisualBounds()` 方法进行坐标系转换
- 包含 metadata 字段存储标注描述
- 持有 `Rectangle` 视觉对象的引用（transient，不序列化）

### 3. AnnotationDialog.java（对话框）
- 模态对话框，显示矩形 4 点坐标信息
- 提供文本输入框供用户输入 metadata
- 返回用户操作结果（保存/取消）

### 4. ExcelHandler.java（文件处理）
- `saveAnnotations()`: 将标注列表导出为 Excel 文件，文件名格式：`图片名称.xlsx`（与图片同名）
- `loadAnnotations()`: 从 Excel 文件加载标注列表
- Excel 格式：x1, y1, x2, y2, x3, y3, x4, y4, metadata

### 5. Launcher.java（启动器）
- 用于创建包含所有依赖的 fat JAR
- 作为 `Main-Class` 入口，避免 JavaFX 模块系统问题

## 构建和运行

### 开发运行
```bash
# 使用 Maven JavaFX 插件运行
mvn javafx:run
```

### 构建打包
```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 打包（生成包含所有依赖的可执行 JAR）
mvn clean package
```

### 运行可执行 JAR
```bash
java -jar target/picmark-1.0-SNAPSHOT.jar

# macOS 用户可能需要添加参数解决显示问题
java -Dprism.order=sw -jar target/picmark-1.0-SNAPSHOT.jar
```

### 打包为原生可执行文件
项目支持使用 `jpackage`（Java 14+ 内置工具）打包为原生可执行文件：

```bash
# 使用打包脚本（自动检测平台）
./package.sh        # macOS/Linux
package.bat         # Windows
```

生成文件位置：
- **macOS**: `target/dist/PicMark.app` （应用包）
- **Windows**: `target/dist/PicMark-1.0.0.exe` （安装程序）
- **Linux**: `target/dist/picmark_1.0.0-1_amd64.deb` （Debian 安装包）

打包脚本会自动将 Java 运行时打包到可执行文件中，用户无需单独安装 Java。

## 测试

项目使用 JUnit 5 进行单元测试：

- **ExcelHandlerTest.java**: 测试 Excel 导入导出功能
  - `testSaveAndLoadAnnotations()`: 验证保存和加载标注数据
  - `testRectangleAnnotationConversion()`: 验证坐标系转换逻辑

运行测试：
```bash
mvn test
```

## 代码规范

### 坐标系约定
- **内部存储**：使用左下角为原点 (0,0)，Y轴向上
- **视觉显示**：JavaFX 使用左上角为原点，需要转换
- 转换方法已封装在 `RectangleAnnotation` 类中

### 命名规范
- 类名：PascalCase（如 `RectangleAnnotation`）
- 方法名：camelCase（如 `getVisualBounds`）
- 包名：小写（`com.example.demo`）
- 中文 UI 文本：界面显示使用中文，代码注释使用中文

### 注释风格
- 类和方法使用 Javadoc 注释
- 关键逻辑使用行内注释说明
- 注释语言：中文

## 关键实现细节

### 坐标系转换
```java
// 从视觉坐标（左上角原点）转换为存储坐标（左下角原点）
public static RectangleAnnotation fromVisualBounds(double visualX, double visualY, 
                                                      double width, double height, 
                                                      double imageHeight)

// 从存储坐标转换为视觉坐标
public double[] getVisualBounds(double imageHeight)
```

### 标注删除
- 右键点击标注区域即可删除
- 通过遍历 `annotations` 列表，检测鼠标位置是否在矩形内

### 文件命名
导出 Excel 文件时自动命名：`{imageName}_{timestamp}.xlsx`
时间戳格式：`yyyyMMdd_HHmmss`

## 依赖说明

主要依赖（见 `pom.xml`）：

| 依赖 | 版本 | 用途 |
|------|------|------|
| javafx-controls | 21 | GUI 控件 |
| poi-ooxml | 5.2.5 | Excel 文件读写 |
| junit-jupiter | 5.10.0 | 单元测试 |

## 注意事项

1. **Java 版本**：项目需要 Java 17 或更高版本
2. **JavaFX 模块**：使用 Maven 插件处理 JavaFX 依赖
3. **Fat JAR**：使用 maven-shade-plugin 打包所有依赖
4. **资源目录**：`src/main/resources` 当前为空，如需添加资源文件（如样式表、图标），可直接放入
5. **多显示器支持**：应用会自动检测鼠标位置，在鼠标当前所在的显示器上打开窗口
6. **macOS 兼容性**：macOS 用户可能需要添加 `-Dprism.order=sw` JVM 参数

## 扩展建议

如需添加新功能，常见扩展点：

- **新标注类型**：继承 `RectangleAnnotation` 或创建新的标注类
- **新文件格式**：在 `ExcelHandler` 中添加新的导入/导出方法
- **UI 定制**：修改 `PicMarkApp` 中的样式字符串或添加 CSS 文件
- **快捷键**：在 `PicMarkApp` 中添加 KeyEvent 处理器
