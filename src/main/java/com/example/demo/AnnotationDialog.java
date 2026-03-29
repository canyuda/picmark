package com.example.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Dialog for displaying rectangle coordinates and entering metadata.
 */
public class AnnotationDialog {
    private Stage dialogStage;
    private TextField metadataField;
    private boolean saved = false;
    private String metadata = "";
    
    public AnnotationDialog(RectangleAnnotation annotation, Window owner) {
        dialogStage = new Stage();
        dialogStage.setTitle("标注信息");
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(false);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        
        // Title
        Label titleLabel = new Label("矩形轮廓坐标信息");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Coordinates grid
        GridPane coordGrid = new GridPane();
        coordGrid.setHgap(10);
        coordGrid.setVgap(8);
        coordGrid.setAlignment(Pos.CENTER);
        
        // Add coordinate labels (clockwise from bottom-left)
        addCoordinateRow(coordGrid, 0, "左下点 (P1):", annotation.getX1(), annotation.getY1());
        addCoordinateRow(coordGrid, 1, "右下点 (P2):", annotation.getX2(), annotation.getY2());
        addCoordinateRow(coordGrid, 2, "右上点 (P3):", annotation.getX3(), annotation.getY3());
        addCoordinateRow(coordGrid, 3, "左上点 (P4):", annotation.getX4(), annotation.getY4());
        
        // Metadata input
        Label metadataLabel = new Label("信息:");
        metadataField = new TextField();
        metadataField.setPromptText("请输入标注描述...");
        metadataField.setPrefWidth(300);
        
        // Buttons
        Button saveButton = new Button("保存");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        saveButton.setOnAction(e -> {
            metadata = metadataField.getText();
            saved = true;
            dialogStage.close();
        });
        
        Button cancelButton = new Button("取消");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> {
            saved = false;
            dialogStage.close();
        });
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        
        // Add all to root
        root.getChildren().addAll(titleLabel, coordGrid, metadataLabel, metadataField, buttonBox);
        
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        
        // Set default button
        saveButton.setDefaultButton(true);
        
        // 设置对话框位置在主窗口中心
        dialogStage.setOnShown(e -> {
            if (owner != null) {
                double centerX = owner.getX() + (owner.getWidth() - dialogStage.getWidth()) / 2;
                double centerY = owner.getY() + (owner.getHeight() - dialogStage.getHeight()) / 2;
                dialogStage.setX(centerX);
                dialogStage.setY(centerY);
            }
        });
    }
    
    private void addCoordinateRow(GridPane grid, int row, String label, double x, double y) {
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label valueLabel = new Label(String.format("(%.2f, %.2f)", x, y));
        valueLabel.setStyle("-fx-font-family: monospace;");
        grid.add(nameLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }
    
    public boolean showAndWait() {
        dialogStage.showAndWait();
        return saved;
    }
    
    public String getMetadata() {
        return metadata;
    }
}
