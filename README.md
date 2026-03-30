# PicMark - 图片标注工具

一个基于 JavaFX 的桌面应用程序，用于在图片上绘制矩形标注并记录坐标信息。

## 功能特性

1. **图片浏览**
   - 打开 PNG、JPG、JPEG、GIF、BMP 格式图片
   - 按原始大小显示图片
   - 鼠标拖拽移动图片位置

2. **矩形标注**
   - 矩形工具框选轮廓
   - 使用 JavaFX 视觉坐标系统，左上角为坐标系原点 (0,0)
   - X轴向右为正方向，Y轴向下为正方向
   - 4个顶点坐标按顺时针记录（左上→右上→右下→左下）
   - 右键点击标注可删除

3. **Metadata 管理**
   - 框选后弹出对话框显示4点坐标
   - 支持输入文字描述（信息）
   - 保存/取消操作

4. **Excel 导入导出**
   - 保存：将标注坐标和metadata导出为 Excel
   - 文件名格式：`图片名称.xlsx`（与图片同名）
   - 导入：从Excel文件加载标注信息并显示

## 运行方式

### 方式1：使用 Maven
```bash
mvn javafx:run
```

### 方式2：运行可执行JAR
```bash
java -jar target/picmark-1.0-SNAPSHOT.jar
```

## 构建项目

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 打包（包含所有依赖的 JAR）
mvn clean package
```

## 打包为原生可执行文件

使用 `jpackage` 工具将应用打包为各平台原生的可执行文件（无需用户安装 Java）：

### macOS (.app)
```bash
./package.sh
```
生成位置：`target/dist/PicMark.app`

### Windows (.exe 安装包)
```batch
package.bat
```
生成位置：`target/dist/PicMark-1.0.0.exe`

### Linux (.deb 安装包)
```bash
./package.sh
```
生成位置：`target/dist/picmark_1.0.0-1_amd64.deb`

### 手动打包
```bash
# macOS
jpackage --name "PicMark" --input target --main-jar picmark-1.0-SNAPSHOT.jar \
    --main-class com.example.demo.Launcher --dest target/dist --type app-image

# Windows
jpackage --name "PicMark" --input target --main-jar picmark-1.0-SNAPSHOT.jar \
    --main-class com.example.demo.Launcher --dest target/dist --type exe \
    --win-dir-chooser --win-menu --win-shortcut

# Linux
jpackage --name "PicMark" --input target --main-jar picmark-1.0-SNAPSHOT.jar \
    --main-class com.example.demo.Launcher --dest target/dist --type deb \
    --linux-shortcut --linux-menu-group "PicMark"
```

## 使用说明

1. 点击"打开图片"选择要标注的图片
2. 点击"矩形工具"激活框选模式
3. 在图片上拖拽绘制矩形
4. 在弹出的对话框中查看坐标和输入描述
5. 点击"保存"完成标注
6. 点击"保存到Excel"导出所有标注
7. 点击"导入点坐标"可从Excel加载标注

## Excel文件格式

| (x1,y1) | (x2,y2) | (x3,y3) | (x4,y4) | metadata |
|----------|----------|----------|----------|----------|
| 左上x,左上y | 右上x,右上y | 右下x,右下y | 左下x,左下y | 描述信息 |

## 技术栈

- Java 17
- JavaFX 21
- Apache POI 5.2.5 (Excel处理)
- Maven

## 项目结构

```
picmark/
├── pom.xml                          # Maven配置
├── src/
│   ├── main/java/com/example/demo/
│   │   ├── PicMarkApp.java          # 主应用
│   │   ├── Launcher.java            # 启动器
│   │   ├── RectangleAnnotation.java # 标注数据类
│   │   ├── AnnotationDialog.java    # 标注对话框
│   │   └── ExcelHandler.java        # Excel处理
│   └── test/
│       └── ExcelHandlerTest.java    # 单元测试
└── target/
    └── picmark-1.0-SNAPSHOT.jar     # 可执行JAR
```

## 快捷键

- **拖拽模式**：鼠标左键拖拽移动图片
- **矩形工具**：点击按钮后拖拽绘制矩形
- **删除标注**：右键点击标注区域

## 注意事项

- 确保系统已安装 Java 17 或更高版本
- 应用会自动在鼠标当前所在的显示器上打开
- OCR 功能需要安装 Tesseract（详见上方安装说明）
- macOS 用户可能需要添加 JVM 参数：`-Dprism.order=sw` 解决显示问题
