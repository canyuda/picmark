package com.example.demo;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Represents a rectangular annotation on an image.
 * Coordinates are stored with JavaFX visual origin (top-left as (0,0)).
 * Points are stored clockwise starting from top-left.
 */
public class RectangleAnnotation {
    private double x1, y1; // Top-left (origin)
    private double x2, y2; // Top-right
    private double x3, y3; // Bottom-right
    private double x4, y4; // Bottom-left
    private String metadata;
    private transient Rectangle visualRect;
    private transient boolean selected;
    
    public RectangleAnnotation() {
        this.metadata = "";
    }
    
    public RectangleAnnotation(double x1, double y1, double x2, double y2, 
                               double x3, double y3, double x4, double y4) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
        this.x4 = x4;
        this.y4 = y4;
        this.metadata = "";
    }
    
    // Getters and Setters
    public double getX1() { return x1; }
    public void setX1(double x1) { this.x1 = x1; }
    
    public double getY1() { return y1; }
    public void setY1(double y1) { this.y1 = y1; }
    
    public double getX2() { return x2; }
    public void setX2(double x2) { this.x2 = x2; }
    
    public double getY2() { return y2; }
    public void setY2(double y2) { this.y2 = y2; }
    
    public double getX3() { return x3; }
    public void setX3(double x3) { this.x3 = x3; }
    
    public double getY3() { return y3; }
    public void setY3(double y3) { this.y3 = y3; }
    
    public double getX4() { return x4; }
    public void setX4(double x4) { this.x4 = x4; }
    
    public double getY4() { return y4; }
    public void setY4(double y4) { this.y4 = y4; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public Rectangle getVisualRect() { return visualRect; }
    public void setVisualRect(Rectangle visualRect) { this.visualRect = visualRect; }
    
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { 
        this.selected = selected;
        if (visualRect != null) {
            visualRect.setStroke(selected ? Color.RED : Color.BLUE);
            visualRect.setStrokeWidth(selected ? 3 : 2);
        }
    }
    
    /**
     * Get the visual rectangle bounds (x, y, width, height)
     * Coordinates are already in JavaFX visual origin (top-left)
     */
    public double[] getVisualBounds() {
        double width = x2 - x1;
        double height = y3 - y1;
        return new double[]{x1, y1, width, height};
    }

    /**
     * Create annotation from visual coordinates (top-left origin)
     * Coordinates are stored directly in JavaFX visual coordinate system
     */
    public static RectangleAnnotation fromVisualBounds(double visualX, double visualY,
                                                       double width, double height) {
        // x1,y1: Top-left
        double x1 = visualX;
        double y1 = visualY;
        // x2,y2: Top-right
        double x2 = visualX + width;
        double y2 = visualY;
        // x3,y3: Bottom-right
        double x3 = visualX + width;
        double y3 = visualY + height;
        // x4,y4: Bottom-left
        double x4 = visualX;
        double y4 = visualY + height;

        return new RectangleAnnotation(x1, y1, x2, y2, x3, y3, x4, y4);
    }
    
    @Override
    public String toString() {
        return String.format("Rectangle[(%.2f, %.2f), (%.2f, %.2f), (%.2f, %.2f), (%.2f, %.2f)] - %s",
                x1, y1, x2, y2, x3, y3, x4, y4, metadata);
    }
}
