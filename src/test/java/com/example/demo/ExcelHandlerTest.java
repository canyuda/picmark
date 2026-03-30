package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 参数化单元测试用于 {@link ExcelHandler}。
 * 测试覆盖 Excel 文件的导入导出、坐标解析、边界条件等核心功能。
 *
 * @author Claude Code
 */
public class ExcelHandlerTest {

    @TempDir
    File tempDir;

    // ========== saveAnnotations 方法测试 ==========

    @Nested
    @DisplayName("saveAnnotations 方法测试")
    class SaveAnnotationsTest {

        @ParameterizedTest(name = "保存 {0} 个标注")
        @ValueSource(ints = {0, 1, 5, 10, 50})
        @DisplayName("保存不同数量的标注应成功")
        void saveAnnotations_WithDifferentCounts_ShouldSucceed(int count) throws IOException {
            List<RectangleAnnotation> annotations = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                RectangleAnnotation ann = new RectangleAnnotation(
                    i * 100, i * 100, (i + 1) * 100, i * 100,
                    (i + 1) * 100, (i + 1) * 50, i * 100, (i + 1) * 50);
                ann.setMetadata("标注" + i);
                annotations.add(ann);
            }

            File outputFile = new File(tempDir, "test_" + count + ".xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_" + count, outputFile);

            assertTrue(outputFile.exists(), "保存的文件应该存在");
            assertTrue(outputFile.length() > 0, "文件内容不应为空");
        }

        @ParameterizedTest(name = "保存坐标：({0},{1}) - ({2},{3})")
        @CsvSource({
            "0,     0,    100, 50",
            "100,   100,  200, 150",
            "50.5,  25.7, 33.3, 66.6",
            "-100,  -50,  50,  100"
        })
        @DisplayName("保存不同类型坐标的标注应成功")
        void saveAnnotations_WithDifferentCoordinates_ShouldSucceed(
                double x1, double y1, double width, double height) throws IOException {

            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                x1, y1, width, height);
            annotation.setMetadata("测试标注");

            List<RectangleAnnotation> annotations = List.of(annotation);

            File outputFile = new File(tempDir, "test_coords.xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_coords", outputFile);

            assertTrue(outputFile.exists());

            // 加载验证坐标保持不变
            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);
            assertEquals(1, loaded.size());

            RectangleAnnotation loadedAnn = loaded.get(0);
            assertEquals(x1, loadedAnn.getX1(), 0.01, "x1 应保持不变");
            assertEquals(y1, loadedAnn.getY1(), 0.01, "y1 应保持不变");
            assertEquals(x1 + width, loadedAnn.getX2(), 0.01, "x2 应保持不变");
            assertEquals(y1, loadedAnn.getY2(), 0.01, "y2 应保持不变");
            assertEquals(x1 + width, loadedAnn.getX3(), 0.01, "x3 应保持不变");
            assertEquals(y1 + height, loadedAnn.getY3(), 0.01, "y3 应保持不变");
            assertEquals(x1, loadedAnn.getX4(), 0.01, "x4 应保持不变");
            assertEquals(y1 + height, loadedAnn.getY4(), 0.01, "y4 应保持不变");
        }

        @ParameterizedTest(name = "保存 metadata: '{0}'")
        @CsvSource({
            "'',",
            "'普通文本',",
            "'Special chars: !@#$%^&*()',",
            "'中文测试',",
            "'Very long text that exceeds normal length expectations for metadata field',",
            "'Line\nBreak',",
            "'Tab\tSeparated',"
        })
        @DisplayName("保存不同类型 metadata 的标注应成功")
        void saveAnnotations_WithDifferentMetadata_ShouldSucceed(String metadata) throws IOException {
            RectangleAnnotation annotation = new RectangleAnnotation(0, 0, 100, 0, 100, 50, 0, 50);
            annotation.setMetadata(metadata);

            List<RectangleAnnotation> annotations = List.of(annotation);

            File outputFile = new File(tempDir, "test_metadata.xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_metadata", outputFile);

            assertTrue(outputFile.exists());

            // 加载验证 metadata
            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);
            assertEquals(1, loaded.size());
            assertEquals(metadata, loaded.get(0).getMetadata(), "metadata 应保持不变");
        }

        @ParameterizedTest(name = "保存 null metadata")
        @NullSource
        @DisplayName("保存 null metadata 的标注应成功")
        void saveAnnotations_WithNullMetadata_ShouldSucceed(String metadata) throws IOException {
            RectangleAnnotation annotation = new RectangleAnnotation(0, 0, 100, 0, 100, 50, 0, 50);
            annotation.setMetadata(metadata);

            List<RectangleAnnotation> annotations = List.of(annotation);

            File outputFile = new File(tempDir, "test_null_metadata.xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_null_metadata", outputFile);

            assertTrue(outputFile.exists());

            // 加载验证 metadata（Excel 中空单元格会被读取为空字符串）
            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);
            assertEquals(1, loaded.size());
            assertEquals("", loaded.get(0).getMetadata(), "metadata 应为空字符串");
        }
    }

    // ========== loadAnnotations 方法测试 ==========

    @Nested
    @DisplayName("loadAnnotations 方法测试")
    class LoadAnnotationsTest {

        @Test
        @DisplayName("从空列表保存并加载应返回空列表")
        void loadAnnotations_FromEmptyList_ShouldReturnEmptyList() throws IOException {
            List<RectangleAnnotation> annotations = Collections.emptyList();

            File outputFile = new File(tempDir, "test_empty.xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_empty", outputFile);

            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);

            assertNotNull(loaded, "返回的列表不应为 null");
            assertTrue(loaded.isEmpty(), "返回的列表应为空");
        }

        @ParameterizedTest(name = "文件名包含特殊字符：{0}")
        @CsvSource({
            "'test_file',",
            "'测试文件',",
            "'test-file',",
            "'test.file',",
            "'test file',"
        })
        @DisplayName("从不同文件名保存和加载应成功")
        void loadAnnotations_WithDifferentFileNames_ShouldSucceed(String fileName) throws IOException {
            List<RectangleAnnotation> annotations = List.of(
                new RectangleAnnotation(0, 0, 100, 0, 100, 50, 0, 50)
            );

            File outputFile = new File(tempDir, fileName + ".xlsx");
            ExcelHandler.saveAnnotations(annotations, fileName, outputFile);

            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);

            assertEquals(1, loaded.size(), "应加载 1 个标注");
        }
    }

    // ========== 往返测试 ==========

    @Nested
    @DisplayName("保存和加载往返测试")
    class RoundTripTest {

        @ParameterizedTest(name = "往返测试：标注数量 {0}")
        @ValueSource(ints = {1, 2, 5, 10})
        @DisplayName("保存后加载应保持所有数据")
        void saveAndLoad_ShouldPreserveAllData(int count) throws IOException {
            List<RectangleAnnotation> original = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                RectangleAnnotation ann = RectangleAnnotation.fromVisualBounds(
                    i * 150, i * 100, 200 + i * 50, 150 + i * 30);
                ann.setMetadata("标注" + i);
                original.add(ann);
            }

            File outputFile = new File(tempDir, "test_roundtrip_" + count + ".xlsx");
            ExcelHandler.saveAnnotations(original, "test_roundtrip", outputFile);
            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);

            assertEquals(count, loaded.size(), "标注数量应一致");

            for (int i = 0; i < count; i++) {
                RectangleAnnotation orig = original.get(i);
                RectangleAnnotation load = loaded.get(i);

                assertAll("标注 " + i + " 数据应一致",
                    () -> assertEquals(orig.getX1(), load.getX1(), 0.01),
                    () -> assertEquals(orig.getY1(), load.getY1(), 0.01),
                    () -> assertEquals(orig.getX2(), load.getX2(), 0.01),
                    () -> assertEquals(orig.getY2(), load.getY2(), 0.01),
                    () -> assertEquals(orig.getX3(), load.getX3(), 0.01),
                    () -> assertEquals(orig.getY3(), load.getY3(), 0.01),
                    () -> assertEquals(orig.getX4(), load.getX4(), 0.01),
                    () -> assertEquals(orig.getY4(), load.getY4(), 0.01),
                    () -> assertEquals(orig.getMetadata(), load.getMetadata())
                );
            }
        }

        @Test
        @DisplayName("往返测试：使用 8 参数构造函数")
        void saveAndLoad_With8ParamConstructor_ShouldPreserveData() throws IOException {
            List<RectangleAnnotation> annotations = List.of(
                new RectangleAnnotation(10, 20, 30, 40, 50, 60, 70, 80)
            );
            annotations.get(0).setMetadata("8 参数构造");

            File outputFile = new File(tempDir, "test_8param.xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_8param", outputFile);
            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);

            assertEquals(1, loaded.size());
            RectangleAnnotation ann = loaded.get(0);

            assertAll("8 参数构造的标注数据应正确保存和加载",
                () -> assertEquals(10, ann.getX1(), 0.01),
                () -> assertEquals(20, ann.getY1(), 0.01),
                () -> assertEquals(30, ann.getX2(), 0.01),
                () -> assertEquals(40, ann.getY2(), 0.01),
                () -> assertEquals(50, ann.getX3(), 0.01),
                () -> assertEquals(60, ann.getY3(), 0.01),
                () -> assertEquals(70, ann.getX4(), 0.01),
                () -> assertEquals(80, ann.getY4(), 0.01),
                () -> assertEquals("8 参数构造", ann.getMetadata())
            );
        }
    }

    // ========== 边界条件测试 ==========

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTest {

        @ParameterizedTest(name = "零尺寸标注：宽={0}, 高={1}")
        @CsvSource({
            "0,   50",
            "100, 0",
            "0,   0"
        })
        @DisplayName("零尺寸标注应能正确保存和加载")
        void zeroSizeAnnotations_ShouldWork(double width, double height) throws IOException {
            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                100, 100, width, height);
            annotation.setMetadata("零尺寸标注");

            List<RectangleAnnotation> annotations = List.of(annotation);

            File outputFile = new File(tempDir, "test_zerosize.xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_zerosize", outputFile);
            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);

            assertEquals(1, loaded.size());

            RectangleAnnotation ann = loaded.get(0);
            // 对于零尺寸，所有对应坐标应该相等
            if (width == 0) {
                assertEquals(ann.getX1(), ann.getX2(), 0.01, "零宽度时 x1 应等于 x2");
                assertEquals(ann.getX2(), ann.getX3(), 0.01, "零宽度时 x2 应等于 x3");
                assertEquals(ann.getX3(), ann.getX4(), 0.01, "零宽度时 x3 应等于 x4");
            }
            if (height == 0) {
                assertEquals(ann.getY1(), ann.getY2(), 0.01, "零高度时 y1 应等于 y2");
                assertEquals(ann.getY2(), ann.getY4(), 0.01, "零高度时 y2 应等于 y4");
                assertEquals(ann.getY4(), ann.getY3(), 0.01, "零高度时 y4 应等于 y3");
            }
        }

        @ParameterizedTest(name = "负坐标标注：({0},{1})")
        @CsvSource({
            "-100, -50",
            "-50.5, -25.7"
        })
        @DisplayName("负坐标标注应能正确保存和加载")
        void negativeCoordinates_ShouldWork(double x, double y) throws IOException {
            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                x, y, 100, 50);
            annotation.setMetadata("负坐标标注");

            List<RectangleAnnotation> annotations = List.of(annotation);

            File outputFile = new File(tempDir, "test_negative.xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_negative", outputFile);
            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);

            assertEquals(1, loaded.size());

            RectangleAnnotation ann = loaded.get(0);
            assertEquals(x, ann.getX1(), 0.01, "x1 应保持为负值");
            assertEquals(y, ann.getY1(), 0.01, "y1 应保持为负值");
        }

        @ParameterizedTest(name = "极大坐标：{0}")
        @CsvSource({
            "10000",
            "1000000"
        })
        @DisplayName("极大坐标标注应能正确保存和加载")
        void veryLargeCoordinates_ShouldWork(double value) throws IOException {
            RectangleAnnotation annotation = new RectangleAnnotation(
                value, value, value + 100, value, value + 100, value + 50, value, value + 50);
            annotation.setMetadata("极大坐标标注");

            List<RectangleAnnotation> annotations = List.of(annotation);

            File outputFile = new File(tempDir, "test_large.xlsx");
            ExcelHandler.saveAnnotations(annotations, "test_large", outputFile);
            List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);

            assertEquals(1, loaded.size());

            RectangleAnnotation ann = loaded.get(0);
            assertEquals(value, ann.getX1(), 1.0, "大数值 x1 应保持");
            assertEquals(value, ann.getY1(), 1.0, "大数值 y1 应保持");
        }
    }

    // ========== RectangleAnnotation 转换测试 ==========

    @Nested
    @DisplayName("RectangleAnnotation 坐标转换测试")
    class RectangleAnnotationConversionTest {

        @ParameterizedTest(name = "从视觉边界创建：原点({0},{1}), 宽{2}, 高{3}")
        @CsvSource({
            "0,     0,    100, 50",
            "100,   100,  200, 150",
            "50.5,  25.7, 33.3, 66.6",
            "-100,  -50,  50,  100"
        })
        @DisplayName("从视觉边界创建标注应正确计算四个顶点")
        void fromVisualBounds_ShouldCalculateAllFourVertices(
                double visualX, double visualY, double width, double height) {

            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                visualX, visualY, width, height);

            assertAll("四个顶点坐标应正确",
                () -> assertEquals(visualX, annotation.getX1(), 0.001, "x1 应等于 visualX"),
                () -> assertEquals(visualY, annotation.getY1(), 0.001, "y1 应等于 visualY"),
                () -> assertEquals(visualX + width, annotation.getX2(), 0.001, "x2 应等于 visualX + width"),
                () -> assertEquals(visualY, annotation.getY2(), 0.001, "y2 应等于 visualY"),
                () -> assertEquals(visualX + width, annotation.getX3(), 0.001, "x3 应等于 visualX + width"),
                () -> assertEquals(visualY + height, annotation.getY3(), 0.001, "y3 应等于 visualY + height"),
                () -> assertEquals(visualX, annotation.getX4(), 0.001, "x4 应等于 visualX"),
                () -> assertEquals(visualY + height, annotation.getY4(), 0.001, "y4 应等于 visualY + height")
            );
        }

        @ParameterizedTest(name = "获取视觉边界：原输入({0},{1}), 宽{2}, 高{3}")
        @CsvSource({
            "0,     0,    100, 50",
            "100,   100,  200, 150",
            "50.5,  25.7, 33.3, 66.6"
        })
        @DisplayName("获取视觉边界应返回正确的值")
        void getVisualBounds_ShouldReturnCorrectValues(
                double x1, double y1, double width, double height) {

            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                x1, y1, width, height);
            double[] bounds = annotation.getVisualBounds();

            assertAll("边界值应正确",
                () -> assertEquals(x1, bounds[0], 0.001, "bounds[0] 应等于 x1"),
                () -> assertEquals(y1, bounds[1], 0.001, "bounds[1] 应等于 y1"),
                () -> assertEquals(width, bounds[2], 0.001, "bounds[2] 应等于 width"),
                () -> assertEquals(height, bounds[3], 0.001, "bounds[3] 应等于 height")
            );
        }

        @Test
        @DisplayName("往返转换应保持数据一致性")
        void roundTripConversion_ShouldPreserveData() {
            // 创建标注
            RectangleAnnotation original = RectangleAnnotation.fromVisualBounds(
                100, 100, 200, 150);

            // 获取边界
            double[] bounds = original.getVisualBounds();

            // 从边界重建标注
            RectangleAnnotation reconstructed = RectangleAnnotation.fromVisualBounds(
                bounds[0], bounds[1], bounds[2], bounds[3]);

            assertAll("重建的标注应与原始标注相同",
                () -> assertEquals(original.getX1(), reconstructed.getX1(), 0.001),
                () -> assertEquals(original.getY1(), reconstructed.getY1(), 0.001),
                () -> assertEquals(original.getX2(), reconstructed.getX2(), 0.001),
                () -> assertEquals(original.getY2(), reconstructed.getY2(), 0.001),
                () -> assertEquals(original.getX3(), reconstructed.getX3(), 0.001),
                () -> assertEquals(original.getY3(), reconstructed.getY3(), 0.001),
                () -> assertEquals(original.getX4(), reconstructed.getX4(), 0.001),
                () -> assertEquals(original.getY4(), reconstructed.getY4(), 0.001)
            );
        }
    }
}
