package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExcelHandler.
 */
public class ExcelHandlerTest {

    @TempDir
    File tempDir;

    @Test
    void testSaveAndLoadAnnotations() throws IOException {
        // Create test annotations
        List<RectangleAnnotation> annotations = new ArrayList<>();
        
        RectangleAnnotation ann1 = new RectangleAnnotation(0, 0, 100, 0, 100, 50, 0, 50);
        ann1.setMetadata("测试标注1");
        annotations.add(ann1);
        
        RectangleAnnotation ann2 = new RectangleAnnotation(200, 100, 300, 100, 300, 200, 200, 200);
        ann2.setMetadata("测试标注2");
        annotations.add(ann2);
        
        // Save annotations
        File outputFile = new File(tempDir, "test_image.xlsx");
        ExcelHandler.saveAnnotations(annotations, "test_image", outputFile);
        
        // Verify saved file exists
        assertTrue(outputFile.exists(), "保存的文件应该存在");
        
        // Load annotations
        List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(outputFile);
        
        // Verify
        assertEquals(2, loaded.size());
        
        RectangleAnnotation loaded1 = loaded.get(0);
        assertEquals(0, loaded1.getX1(), 0.001);
        assertEquals(0, loaded1.getY1(), 0.001);
        assertEquals(100, loaded1.getX2(), 0.001);
        assertEquals(0, loaded1.getY2(), 0.001);
        assertEquals(100, loaded1.getX3(), 0.001);
        assertEquals(50, loaded1.getY3(), 0.001);
        assertEquals(0, loaded1.getX4(), 0.001);
        assertEquals(50, loaded1.getY4(), 0.001);
        assertEquals("测试标注1", loaded1.getMetadata());
        
        RectangleAnnotation loaded2 = loaded.get(1);
        assertEquals("测试标注2", loaded2.getMetadata());
    }
    
    @Test
    void testRectangleAnnotationConversion() {
        // Create from visual bounds (top-left origin)
        RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
            100, 100, 200, 150);
        
        // Verify coordinates are stored as visual coordinates (top-left origin)
        assertEquals(100, annotation.getX1(), 0.001);  // Top-left x
        assertEquals(100, annotation.getY1(), 0.001);  // Top-left y
        assertEquals(300, annotation.getX2(), 0.001);  // Top-right x (100 + 200)
        assertEquals(100, annotation.getY2(), 0.001);  // Top-right y
        assertEquals(300, annotation.getX3(), 0.001);  // Bottom-right x
        assertEquals(250, annotation.getY3(), 0.001);  // Bottom-right y (100 + 150)
        assertEquals(100, annotation.getX4(), 0.001);  // Bottom-left x
        assertEquals(250, annotation.getY4(), 0.001);  // Bottom-left y
        
        // Test conversion back to visual bounds
        double[] bounds = annotation.getVisualBounds();
        assertEquals(100, bounds[0], 0.001);   // x
        assertEquals(100, bounds[1], 0.001);   // y (top-left in visual coords)
        assertEquals(200, bounds[2], 0.001);   // width
        assertEquals(150, bounds[3], 0.001);   // height
    }
}
