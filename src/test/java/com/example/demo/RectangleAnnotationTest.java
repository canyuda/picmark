package com.example.demo;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 参数化单元测试用于 {@link RectangleAnnotation}。
 * 测试覆盖坐标转换、边界条件、选中状态等核心功能。
 *
 * @author Claude Code
 */
@DisplayName("RectangleAnnotation 参数化测试")
public class RectangleAnnotationTest {

    // ========== fromVisualBounds 方法测试 ==========

    @Nested
    @DisplayName("fromVisualBounds 方法测试")
    class FromVisualBoundsTest {

        @ParameterizedTest(name = "从视觉边界创建标注：原点({0},{1}), 宽{2}, 高{3}")
        @CsvSource({
            "0,     0,    100, 50",    // 原点
            "100,   100,  200, 150",  // 非原点
            "50.5,  25.7, 33.3, 66.6", // 浮点坐标
            "-100,  -50,  50,  100",  // 负坐标
            "1000,  2000, 3000, 4000"  // 大坐标
        })
        @DisplayName("从视觉边界创建标注应正确计算四个顶点坐标")
        void fromVisualBounds_ShouldCalculateAllFourVertices(
                double visualX, double visualY, double width, double height) {

            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                visualX, visualY, width, height);

            // 验证四个顶点坐标
            assertAll("四个顶点坐标应正确",
                // x1,y1: Top-left
                () -> assertEquals(visualX, annotation.getX1(), 0.001, "x1 应等于 visualX"),
                () -> assertEquals(visualY, annotation.getY1(), 0.001, "y1 应等于 visualY"),
                // x2,y2: Top-right
                () -> assertEquals(visualX + width, annotation.getX2(), 0.001, "x2 应等于 visualX + width"),
                () -> assertEquals(visualY, annotation.getY2(), 0.001, "y2 应等于 visualY"),
                // x3,y3: Bottom-right
                () -> assertEquals(visualX + width, annotation.getX3(), 0.001, "x3 应等于 visualX + width"),
                () -> assertEquals(visualY + height, annotation.getY3(), 0.001, "y3 应等于 visualY + height"),
                // x4,y4: Bottom-left
                () -> assertEquals(visualX, annotation.getX4(), 0.001, "x4 应等于 visualX"),
                () -> assertEquals(visualY + height, annotation.getY4(), 0.001, "y4 应等于 visualY + height")
            );
        }

        @ParameterizedTest(name = "metadata 应初始化为空字符串")
        @CsvSource({
            "0, 0, 100, 50",
            "100, 200, 300, 400"
        })
        @DisplayName("创建标注时 metadata 应初始化为空字符串")
        void fromVisualBounds_MetadataShouldBeEmpty(
                double visualX, double visualY, double width, double height) {

            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                visualX, visualY, width, height);

            assertNotNull(annotation.getMetadata(), "metadata 不应为 null");
            assertEquals("", annotation.getMetadata(), "metadata 应为空字符串");
        }

        @ParameterizedTest(name = "宽度为零的标注")
        @CsvSource({
            "100, 100, 0, 50",
            "100, 100, 0.0, 50.0"
        })
        @DisplayName("宽度为零时顶点应正确收缩")
        void fromVisualBounds_ZeroWidth_ShouldCollapseVertices(
                double visualX, double visualY, double width, double height) {

            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                visualX, visualY, width, height);

            // 所有 x 坐标应相等
            assertEquals(annotation.getX1(), annotation.getX2(), 0.001, "x1 应等于 x2");
            assertEquals(annotation.getX2(), annotation.getX3(), 0.001, "x2 应等于 x3");
            assertEquals(annotation.getX3(), annotation.getX4(), 0.001, "x3 应等于 x4");
        }

        @ParameterizedTest(name = "高度为零的标注")
        @CsvSource({
            "100, 100, 50, 0",
            "100, 100, 50.0, 0.0"
        })
        @DisplayName("高度为零时顶点应正确收缩")
        void fromVisualBounds_ZeroHeight_ShouldCollapseVertices(
                double visualX, double visualY, double width, double height) {

            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                visualX, visualY, width, height);

            // 所有 y 坐标应相等
            assertEquals(annotation.getY1(), annotation.getY2(), 0.001, "y1 应等于 y2");
            assertEquals(annotation.getY2(), annotation.getY4(), 0.001, "y2 应等于 y4");
            assertEquals(annotation.getY4(), annotation.getY3(), 0.001, "y4 应等于 y3");
        }
    }

    // ========== getVisualBounds 方法测试 ==========

    @Nested
    @DisplayName("getVisualBounds 方法测试")
    class GetVisualBoundsTest {

        @ParameterizedTest(name = "获取视觉边界：原输入({0},{1}), 宽{2}, 高{3}")
        @MethodSource("provideBoundsTestData")
        @DisplayName("getVisualBounds 应返回正确的边界值")
        void getVisualBounds_ShouldReturnCorrectBounds(
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

        @ParameterizedTest(name = "边界数组长度应为 4")
        @CsvSource({
            "0, 0, 100, 50",
            "50, 50, 200, 100"
        })
        @DisplayName("getVisualBounds 应返回长度为 4 的数组")
        void getVisualBounds_ShouldReturnArrayWithFourElements(
                double x1, double y1, double width, double height) {

            RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
                x1, y1, width, height);
            double[] bounds = annotation.getVisualBounds();

            assertEquals(4, bounds.length, "边界数组长度应为 4");
        }

        private static Stream<Arguments> provideBoundsTestData() {
            return Stream.of(
                Arguments.of(0, 0, 100, 50),
                Arguments.of(100, 100, 200, 150),
                Arguments.of(50.5, 25.7, 33.3, 66.6),
                Arguments.of(-100, -50, 50, 100),
                Arguments.of(1000, 2000, 3000, 4000)
            );
        }
    }

    // ========== 构造函数测试 ==========

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTest {

        @ParameterizedTest(name = "8 参数构造函数：({0},{1}), ({2},{3}), ({4},{5}), ({6},{7})")
        @CsvSource({
            "0, 0, 100, 0, 100, 50, 0, 50",
            "100, 200, 300, 200, 300, 400, 100, 400"
        })
        @DisplayName("8 参数构造函数应正确设置所有坐标")
        void constructorWith8Params_ShouldSetAllCoordinates(
                double x1, double y1, double x2, double y2,
                double x3, double y3, double x4, double y4) {

            RectangleAnnotation annotation = new RectangleAnnotation(
                x1, y1, x2, y2, x3, y3, x4, y4);

            assertAll("所有坐标应正确设置",
                () -> assertEquals(x1, annotation.getX1(), 0.001),
                () -> assertEquals(y1, annotation.getY1(), 0.001),
                () -> assertEquals(x2, annotation.getX2(), 0.001),
                () -> assertEquals(y2, annotation.getY2(), 0.001),
                () -> assertEquals(x3, annotation.getX3(), 0.001),
                () -> assertEquals(y3, annotation.getY3(), 0.001),
                () -> assertEquals(x4, annotation.getX4(), 0.001),
                () -> assertEquals(y4, annotation.getY4(), 0.001)
            );
        }

        @ParameterizedTest(name = "无参构造函数：metadata 应为空字符串")
        @ValueSource(strings = {"", "default"})
        @DisplayName("无参构造函数应初始化 metadata 为空字符串")
        void defaultConstructor_MetadataShouldBeEmpty(String placeholder) {
            RectangleAnnotation annotation = new RectangleAnnotation();

            assertNotNull(annotation.getMetadata(), "metadata 不应为 null");
            assertEquals("", annotation.getMetadata(), "metadata 应为空字符串");
        }

        @Test
        @DisplayName("无参构造函数应初始化所有坐标为 0")
        void defaultConstructor_CoordinatesShouldBeZero() {
            RectangleAnnotation annotation = new RectangleAnnotation();

            assertAll("所有坐标应初始化为 0",
                () -> assertEquals(0, annotation.getX1(), 0.001),
                () -> assertEquals(0, annotation.getY1(), 0.001),
                () -> assertEquals(0, annotation.getX2(), 0.001),
                () -> assertEquals(0, annotation.getY2(), 0.001),
                () -> assertEquals(0, annotation.getX3(), 0.001),
                () -> assertEquals(0, annotation.getY3(), 0.001),
                () -> assertEquals(0, annotation.getX4(), 0.001),
                () -> assertEquals(0, annotation.getY4(), 0.001)
            );
        }
    }

    // ========== Setter 测试 ==========

    @Nested
    @DisplayName("Setter 方法测试")
    class SetterTest {

        @ParameterizedTest(name = "设置坐标：第 {0} 个顶点")
        @ValueSource(ints = {1, 2, 3, 4})
        @DisplayName("每个坐标 setter 应正确设置值")
        void setters_ShouldSetCorrectValues(int vertex) {
            RectangleAnnotation annotation = new RectangleAnnotation();

            switch (vertex) {
                case 1:
                    annotation.setX1(100);
                    annotation.setY1(200);
                    assertEquals(100, annotation.getX1(), 0.001);
                    assertEquals(200, annotation.getY1(), 0.001);
                    break;
                case 2:
                    annotation.setX2(300);
                    annotation.setY2(400);
                    assertEquals(300, annotation.getX2(), 0.001);
                    assertEquals(400, annotation.getY2(), 0.001);
                    break;
                case 3:
                    annotation.setX3(500);
                    annotation.setY3(600);
                    assertEquals(500, annotation.getX3(), 0.001);
                    assertEquals(600, annotation.getY3(), 0.001);
                    break;
                case 4:
                    annotation.setX4(700);
                    annotation.setY4(800);
                    assertEquals(700, annotation.getX4(), 0.001);
                    assertEquals(800, annotation.getY4(), 0.001);
                    break;
            }
        }

        @ParameterizedTest(name = "设置 metadata: {0}")
        @CsvSource({
            "'',",
            "'测试标注',",
            "'Annotation #123',",
            "'Special chars: !@#$%^&*()',"
        })
        @DisplayName("metadata setter 应正确设置值")
        void setMetadata_ShouldSetCorrectValue(String metadata) {
            RectangleAnnotation annotation = new RectangleAnnotation();
            annotation.setMetadata(metadata);

            assertEquals(metadata, annotation.getMetadata());
        }

        @ParameterizedTest(name = "设置 null metadata")
        @NullSource
        @DisplayName("metadata setter 应接受 null 值")
        void setMetadata_ShouldAcceptNull(String metadata) {
            RectangleAnnotation annotation = new RectangleAnnotation();
            annotation.setMetadata(metadata);

            assertNull(annotation.getMetadata());
        }
    }

    // ========== setSelected 方法测试 ==========

    @Nested
    @DisplayName("setSelected 方法测试")
    class SetSelectedTest {

        @ParameterizedTest(name = "设置选中状态：selected={0}")
        @CsvSource({
            "true",
            "false"
        })
        @DisplayName("setSelected 应更新内部状态")
        void setSelected_ShouldUpdateInternalState(boolean selected) {
            RectangleAnnotation annotation = new RectangleAnnotation();
            annotation.setSelected(selected);

            assertEquals(selected, annotation.isSelected(),
                "isSelected() 应返回设定的状态");
        }

        @Test
        @DisplayName("设置选中状态且 visualRect 存在时应更新矩形样式")
        void setSelected_WithVisualRect_ShouldUpdateRectangleStyle() {
            RectangleAnnotation annotation = new RectangleAnnotation();
            Rectangle visualRect = new Rectangle();
            annotation.setVisualRect(visualRect);

            // 设置为选中状态
            annotation.setSelected(true);

            assertAll("选中时样式应更新",
                () -> assertEquals(Color.RED, visualRect.getStroke(), "选中时边框应为红色"),
                () -> assertEquals(3, visualRect.getStrokeWidth(), 0.001, "选中时边框宽度应为 3")
            );

            // 取消选中
            annotation.setSelected(false);

            assertAll("未选中时样式应更新",
                () -> assertEquals(Color.BLUE, visualRect.getStroke(), "未选中时边框应为蓝色"),
                () -> assertEquals(2, visualRect.getStrokeWidth(), 0.001, "未选中时边框宽度应为 2")
            );
        }

        @Test
        @DisplayName("设置选中状态但 visualRect 为 null 时不应抛出异常")
        void setSelected_WithoutVisualRect_ShouldNotThrowException() {
            RectangleAnnotation annotation = new RectangleAnnotation();

            assertAll("visualRect 为 null 时设置选中状态",
                () -> {
                    annotation.setSelected(true);
                    assertTrue(annotation.isSelected());
                },
                () -> {
                    annotation.setSelected(false);
                    assertFalse(annotation.isSelected());
                }
            );
        }
    }

    // ========== toString 方法测试 ==========

    @Nested
    @DisplayName("toString 方法测试")
    class ToStringTest {

        @ParameterizedTest(name = "toString 输出格式验证")
        @CsvSource({
            "0, 0, 100, 0, 100, 50, 0, 50, '标注1'",
            "100, 200, 300, 200, 300, 400, 100, 400, '标注2'"
        })
        @DisplayName("toString 应包含所有坐标和 metadata")
        void toString_ShouldContainAllCoordinatesAndMetadata(
                double x1, double y1, double x2, double y2,
                double x3, double y3, double x4, double y4, String metadata) {

            RectangleAnnotation annotation = new RectangleAnnotation(
                x1, y1, x2, y2, x3, y3, x4, y4);
            annotation.setMetadata(metadata);

            String result = annotation.toString();

            assertAll("toString 输出应包含所有信息",
                () -> assertTrue(result.contains(String.format("%.2f", x1)), "应包含 x1"),
                () -> assertTrue(result.contains(String.format("%.2f", y1)), "应包含 y1"),
                () -> assertTrue(result.contains(String.format("%.2f", x2)), "应包含 x2"),
                () -> assertTrue(result.contains(String.format("%.2f", y2)), "应包含 y2"),
                () -> assertTrue(result.contains(String.format("%.2f", x3)), "应包含 x3"),
                () -> assertTrue(result.contains(String.format("%.2f", y3)), "应包含 y3"),
                () -> assertTrue(result.contains(String.format("%.2f", x4)), "应包含 x4"),
                () -> assertTrue(result.contains(String.format("%.2f", y4)), "应包含 y4"),
                () -> assertTrue(result.contains("Rectangle["), "应包含 Rectangle 前缀"),
                () -> assertTrue(result.contains(metadata), "应包含 metadata")
            );
        }

        @ParameterizedTest(name = "空 metadata 时的 toString")
        @CsvSource({
            "0, 0, 100, 0, 100, 50, 0, 50, ''"
        })
        @DisplayName("toString 在 metadata 为空字符串时也应正常工作")
        void toString_WithEmptyMetadata_ShouldWork(
                double x1, double y1, double x2, double y2,
                double x3, double y3, double x4, double y4, String metadata) {

            RectangleAnnotation annotation = new RectangleAnnotation(
                x1, y1, x2, y2, x3, y3, x4, y4);
            annotation.setMetadata(metadata);

            String result = annotation.toString();

            assertNotNull(result, "toString 不应返回 null");
            assertFalse(result.isEmpty(), "toString 不应返回空字符串");
        }
    }

    // ========== visualRect 相关测试 ==========

    @Nested
    @DisplayName("visualRect 属性测试")
    class VisualRectTest {

        @Test
        @DisplayName("设置和获取 visualRect 应一致")
        void setAndGetVisualRect_ShouldBeConsistent() {
            RectangleAnnotation annotation = new RectangleAnnotation();
            Rectangle visualRect = new Rectangle(100, 100, 200, 150);

            annotation.setVisualRect(visualRect);
            Rectangle retrieved = annotation.getVisualRect();

            assertSame(visualRect, retrieved, "获取的应为同一对象");
        }

        @Test
        @DisplayName("visualRect 初始值应为 null")
        void visualRect_ShouldBeNullInitially() {
            RectangleAnnotation annotation = new RectangleAnnotation();

            assertNull(annotation.getVisualRect(), "visualRect 初始值应为 null");
        }
    }

    // ========== 场景测试 ==========

    @Nested
    @DisplayName("场景测试")
    class ScenarioTest {

        @Test
        @DisplayName("创建标注 -> 获取边界 -> 应能重建相同的标注")
        void fromVisualBounds_And_getVisualBounds_RoundTrip() {
            // 创建标注
            RectangleAnnotation original = RectangleAnnotation.fromVisualBounds(
                100, 100, 200, 150);
            original.setMetadata("测试标注");

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

        @Test
        @DisplayName("选中状态切换应正确工作")
        void toggleSelectedState_ShouldWorkCorrectly() {
            RectangleAnnotation annotation = new RectangleAnnotation();
            Rectangle visualRect = new Rectangle();
            annotation.setVisualRect(visualRect);

            // 初始状态（新 Rectangle 的 stroke 默认为 null）
            assertFalse(annotation.isSelected());
            assertNull(visualRect.getStroke());

            // 切换到选中
            annotation.setSelected(true);
            assertTrue(annotation.isSelected());
            assertEquals(Color.RED, visualRect.getStroke(), "选中时边框应为红色");
            assertEquals(3, visualRect.getStrokeWidth(), 0.001, "选中时边框宽度应为 3");

            // 切换回未选中
            annotation.setSelected(false);
            assertFalse(annotation.isSelected());
            assertEquals(Color.BLUE, visualRect.getStroke(), "未选中时边框应为蓝色");
            assertEquals(2, visualRect.getStrokeWidth(), 0.001, "未选中时边框宽度应为 2");
        }
    }
}
