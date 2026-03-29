package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Main application for PicMark - Image Annotation Tool.
 */
public class PicMarkApp extends Application {
    
    private Stage primaryStage;
    private BorderPane root;
    private Pane canvasContainer;
    private Canvas imageCanvas;
    private Pane overlayPane;
    private GraphicsContext gc;
    
    // Image data
    private Image currentImage;
    private String imageName;
    private double imageWidth;
    private double imageHeight;
    private File currentImageFile = null;  // 当前打开的图片文件
    
    // Viewport state
    private double viewportX = 0;
    private double viewportY = 0;
    private double scale = 1.0;
    
    // Dragging state
    private boolean isDragging = false;
    private double dragStartX;
    private double dragStartY;
    
    // Rectangle drawing state
    private boolean isRectangleToolActive = false;
    private boolean isDrawingRectangle = false;
    private double rectStartX;
    private double rectStartY;
    private Rectangle tempRectangle;
    
    // Annotations
    private List<RectangleAnnotation> annotations = new ArrayList<>();
    private RectangleAnnotation selectedAnnotation = null;
    
    // 当前关联的Excel文件路径（用于自动保存）
    private File currentExcelFile = null;
    
    // UI Components
    private ToggleButton rectToolButton;
    private Label statusLabel;
    private Label imageInfoLabel;
    private Button deleteButton;
    
    // Preferences for storing last opened image path
    private static final String PREF_LAST_IMAGE_PATH = "lastImagePath";
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("PicMark - 图片标注工具");
        
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Create toolbar
        createToolbar();
        
        // Create canvas area
        createCanvasArea();
        
        // Create status bar
        createStatusBar();
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        
        // 将窗口定位到鼠标当前所在的显示器
        positionStageOnCurrentScreen(primaryStage);
        
        primaryStage.show();
        
        // Load last opened image after stage is shown
        loadLastImage();
    }
    
    /**
     * Loads the last opened image from preferences if it exists.
     */
    /**
     * 将窗口定位到鼠标当前所在的显示器中央
     */
    private void positionStageOnCurrentScreen(Stage stage) {
        try {
            // 获取鼠标当前位置
            java.awt.PointerInfo pointerInfo = java.awt.MouseInfo.getPointerInfo();
            if (pointerInfo == null) {
                return; // 无法获取鼠标位置，使用默认位置
            }
            
            java.awt.Point mouseLocation = pointerInfo.getLocation();
            double mouseX = mouseLocation.getX();
            double mouseY = mouseLocation.getY();
            
            // 查找鼠标所在的显示器
            Screen targetScreen = null;
            for (Screen screen : Screen.getScreens()) {
                javafx.geometry.Rectangle2D bounds = screen.getBounds();
                if (bounds.contains(mouseX, mouseY)) {
                    targetScreen = screen;
                    break;
                }
            }
            
            // 如果没有找到，使用主显示器
            if (targetScreen == null) {
                targetScreen = Screen.getPrimary();
            }
            
            // 计算窗口位置（显示器中央）
            javafx.geometry.Rectangle2D screenBounds = targetScreen.getBounds();
            double stageWidth = 1200;
            double stageHeight = 800;
            
            // 如果显示器太小，适配显示器大小
            if (stageWidth > screenBounds.getWidth() * 0.9) {
                stageWidth = screenBounds.getWidth() * 0.9;
            }
            if (stageHeight > screenBounds.getHeight() * 0.9) {
                stageHeight = screenBounds.getHeight() * 0.9;
            }
            
            double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - stageWidth) / 2;
            double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - stageHeight) / 2;
            
            stage.setX(centerX);
            stage.setY(centerY);
            stage.setWidth(stageWidth);
            stage.setHeight(stageHeight);
            
        } catch (Exception e) {
            // 出错时使用默认位置
            System.err.println("无法定位到当前显示器: " + e.getMessage());
        }
    }
    
    private void loadLastImage() {
        Preferences prefs = Preferences.userNodeForPackage(PicMarkApp.class);
        String lastPath = prefs.get(PREF_LAST_IMAGE_PATH, null);
        if (lastPath != null) {
            File file = new File(lastPath);
            if (file.exists() && file.isFile()) {
                loadImage(file);
            }
        }
    }
    
    private void createToolbar() {
        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 10;");
        
        // === 左侧按钮组 ===
        // Open image button
        Button openButton = new Button("打开图片");
        openButton.setStyle(getButtonStyle("#2196F3"));
        openButton.setOnAction(e -> openImage());
        
        // Annotation mode button
        rectToolButton = new ToggleButton("标注模式: 关");
        rectToolButton.setStyle(getToggleButtonStyle());
        rectToolButton.setOnAction(e -> {
            isRectangleToolActive = rectToolButton.isSelected();
            if (isRectangleToolActive) {
                // 开启标注模式时，取消当前选中的矩形
                selectAnnotation(null);
                rectToolButton.setText("标注模式: 开");
                rectToolButton.setStyle(getToggleButtonStyleActive());
            } else {
                rectToolButton.setText("标注模式: 关");
                rectToolButton.setStyle(getToggleButtonStyle());
            }
            updateCursor();
        });
        
        // Import Excel button
        Button importButton = new Button("导入点坐标");
        importButton.setStyle(getButtonStyle("#FF9800"));
        importButton.setOnAction(e -> importAnnotations());
        
        // === 弹性间隔，将左右按钮分开 ===
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // === 右侧按钮组 ===
        // Save button
        Button saveButton = new Button("保存到Excel");
        saveButton.setStyle(getButtonStyle("#4CAF50"));
        saveButton.setOnAction(e -> saveAnnotations());
        
        // Clear button
        Button clearButton = new Button("清除全部标注");
        clearButton.setStyle(getButtonStyle("#f44336"));
        clearButton.setOnAction(e -> confirmAndClearAnnotations());
        
        // Help label
        Label helpLabel = new Label("提示: 拖拽移动 | 右键删除标注");
        helpLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12;");
        
        toolBar.getItems().addAll(openButton, new Separator(), 
                                  rectToolButton, new Separator(),
                                  importButton, spacer,
                                  saveButton, clearButton,
                                  new Separator(), helpLabel);
        
        root.setTop(toolBar);
    }
    
    private void createCanvasArea() {
        canvasContainer = new Pane();
        canvasContainer.setStyle("-fx-background-color: #ffffff;");
        
        // Image canvas
        imageCanvas = new Canvas();
        gc = imageCanvas.getGraphicsContext2D();
        
        // Overlay pane for interactive rectangles
        overlayPane = new Pane();
        overlayPane.setMouseTransparent(false);
        
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().addAll(imageCanvas, overlayPane);
        
        canvasContainer.getChildren().add(stackPane);
        
        // Setup mouse handlers
        setupCanvasMouseHandlers();
        
        root.setCenter(canvasContainer);
    }
    
    private void createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 8 15;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("就绪");
        statusLabel.setStyle("-fx-text-fill: #333333;");
        
        imageInfoLabel = new Label("");
        imageInfoLabel.setStyle("-fx-text-fill: #333333;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusBar.getChildren().addAll(statusLabel, spacer, imageInfoLabel);
        root.setBottom(statusBar);
    }
    
    private void setupCanvasMouseHandlers() {
        canvasContainer.setOnMousePressed(this::handleMousePressed);
        canvasContainer.setOnMouseDragged(this::handleMouseDragged);
        canvasContainer.setOnMouseReleased(this::handleMouseReleased);
        canvasContainer.setOnMouseMoved(this::handleMouseMoved);
    }
    
    private void handleMousePressed(MouseEvent e) {
        if (currentImage == null) return;
        
        if (e.getButton() == MouseButton.SECONDARY) {
            // Right click - delete annotation at position
            deleteAnnotationAt(e.getX(), e.getY());
            return;
        }
        
        if (e.getButton() != MouseButton.PRIMARY) return;
        
        if (isRectangleToolActive) {
            // Start drawing rectangle
            isDrawingRectangle = true;
            rectStartX = e.getX();
            rectStartY = e.getY();
            
            tempRectangle = new Rectangle(rectStartX, rectStartY, 0, 0);
            tempRectangle.setFill(Color.rgb(33, 150, 243, 0.2));
            tempRectangle.setStroke(Color.rgb(33, 150, 243, 0.8));
            tempRectangle.setStrokeWidth(2);
            tempRectangle.setMouseTransparent(true);
            overlayPane.getChildren().add(tempRectangle);
        } else {
            // Start panning
            isDragging = true;
            dragStartX = e.getX() - viewportX;
            dragStartY = e.getY() - viewportY;
            canvasContainer.setCursor(Cursor.CLOSED_HAND);
        }
    }
    
    private void handleMouseDragged(MouseEvent e) {
        if (currentImage == null) return;
        
        if (isDrawingRectangle && tempRectangle != null) {
            double x = Math.min(rectStartX, e.getX());
            double y = Math.min(rectStartY, e.getY());
            double width = Math.abs(e.getX() - rectStartX);
            double height = Math.abs(e.getY() - rectStartY);
            
            tempRectangle.setX(x);
            tempRectangle.setY(y);
            tempRectangle.setWidth(width);
            tempRectangle.setHeight(height);
        } else if (isDragging) {
            viewportX = e.getX() - dragStartX;
            viewportY = e.getY() - dragStartY;
            constrainViewport();
            render();
        }
    }
    
    private void handleMouseReleased(MouseEvent e) {
        if (isDrawingRectangle && tempRectangle != null) {
            double width = tempRectangle.getWidth();
            double height = tempRectangle.getHeight();
            
            // Only create annotation if rectangle is large enough
            if (width > 10 && height > 10) {
                createAnnotation(tempRectangle.getX(), tempRectangle.getY(), width, height);
            }
            
            overlayPane.getChildren().remove(tempRectangle);
            tempRectangle = null;
            isDrawingRectangle = false;
        }
        
        if (isDragging) {
            isDragging = false;
            updateCursor();
        }
    }
    
    private void handleMouseMoved(MouseEvent e) {
        if (currentImage == null) return;

        // Update status with coordinates (already in JavaFX visual origin)
        double imgX = e.getX() - viewportX;
        double imgY = e.getY() - viewportY;

        statusLabel.setText(String.format("坐标: (%.1f, %.1f)", imgX, imgY));
    }
    
    private void createAnnotation(double visualX, double visualY, double width, double height) {
        // Create annotation using JavaFX visual coordinates
        RectangleAnnotation annotation = RectangleAnnotation.fromVisualBounds(
            visualX - viewportX, visualY - viewportY, width, height);

        // Show dialog
        AnnotationDialog dialog = new AnnotationDialog(annotation, primaryStage);
        boolean saved = dialog.showAndWait();

        if (saved) {
            annotation.setMetadata(dialog.getMetadata());
            annotations.add(annotation);
            addVisualRectangle(annotation);
            statusLabel.setText("已添加标注: " + annotations.size());
            
            // 自动保存到Excel
            autoSaveAnnotations();
        }
    }
    
    private void addVisualRectangle(RectangleAnnotation annotation) {
        double[] bounds = annotation.getVisualBounds();
        // bounds 存储的是相对于图片的坐标，需要加上 viewport 偏移量
        Rectangle rect = new Rectangle(
            bounds[0] + viewportX,  // 屏幕 X 坐标
            bounds[1] + viewportY,  // 屏幕 Y 坐标
            bounds[2],              // 宽度
            bounds[3]               // 高度
        );
        updateRectStyle(rect, annotation.isSelected());

        // Tooltip with metadata
        Tooltip tooltip = new Tooltip(annotation.getMetadata().isEmpty() ?
            "无描述" : annotation.getMetadata());
        Tooltip.install(rect, tooltip);
        
        // 添加点击事件（非矩形工具模式下可选中/取消选中）
        rect.setOnMouseClicked(e -> {
            if (!isRectangleToolActive && e.getButton() == MouseButton.PRIMARY) {
                // 如果点击的是已选中的矩形，则取消选中
                if (annotation == selectedAnnotation) {
                    selectAnnotation(null);
                    statusLabel.setText("已取消选中");
                } else {
                    selectAnnotation(annotation);
                }
                e.consume();
            }
        });

        annotation.setVisualRect(rect);
        overlayPane.getChildren().add(rect);
    }
    
    private void updateRectStyle(Rectangle rect, boolean selected) {
        if (selected) {
            rect.setFill(Color.rgb(33, 150, 243, 0.3));
            rect.setStroke(Color.RED);
            rect.setStrokeWidth(3);
        } else {
            rect.setFill(Color.rgb(33, 150, 243, 0.1));
            rect.setStroke(Color.BLUE);
            rect.setStrokeWidth(2);
        }
    }
    
    private void selectAnnotation(RectangleAnnotation annotation) {
        // 取消之前的选中状态
        if (selectedAnnotation != null) {
            selectedAnnotation.setSelected(false);
            Rectangle prevRect = selectedAnnotation.getVisualRect();
            if (prevRect != null) {
                updateRectStyle(prevRect, false);
            }
        }
        
        // 设置新的选中状态
        selectedAnnotation = annotation;
        if (annotation != null) {
            annotation.setSelected(true);
            Rectangle rect = annotation.getVisualRect();
            if (rect != null) {
                updateRectStyle(rect, true);
            }
            showDeleteButton(annotation);
            statusLabel.setText("已选中标注" + (annotation.getMetadata().isEmpty() ? "" : ": " + annotation.getMetadata()));
        } else {
            hideDeleteButton();
        }
    }
    
    private void showDeleteButton(RectangleAnnotation annotation) {
        hideDeleteButton(); // 先隐藏旧的
        
        Rectangle rect = annotation.getVisualRect();
        if (rect == null) return;
        
        deleteButton = new Button("删除");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 8;");
        deleteButton.setOnAction(e -> confirmAndDeleteSelectedAnnotation());
        
        // 将按钮放在矩形右上角
        deleteButton.setLayoutX(rect.getX() + rect.getWidth() - 30);
        deleteButton.setLayoutY(rect.getY() - 15);
        
        overlayPane.getChildren().add(deleteButton);
    }
    
    private void hideDeleteButton() {
        if (deleteButton != null) {
            overlayPane.getChildren().remove(deleteButton);
            deleteButton = null;
        }
    }
    
    private void confirmAndDeleteSelectedAnnotation() {
        if (selectedAnnotation == null) return;
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.initOwner(primaryStage);
        confirmDialog.setTitle("确认删除");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("确定要删除此标注吗？\n此操作不可撤销。");
        
        ButtonType confirmButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(confirmButton, cancelButton);
        
        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == confirmButton) {
                deleteSelectedAnnotation();
            }
        });
    }
    
    private void deleteSelectedAnnotation() {
        if (selectedAnnotation == null) return;
        
        Rectangle rect = selectedAnnotation.getVisualRect();
        if (rect != null) {
            overlayPane.getChildren().remove(rect);
        }
        hideDeleteButton();
        annotations.remove(selectedAnnotation);
        selectedAnnotation = null;
        
        statusLabel.setText("已删除标注, 剩余: " + annotations.size());
        
        // 自动保存到Excel
        autoSaveAnnotations();
    }
    
    private void deleteAnnotationAt(double x, double y) {
        // 右键点击选中的标注弹出删除确认
        if (selectedAnnotation != null) {
            Rectangle selectedRect = selectedAnnotation.getVisualRect();
            if (selectedRect != null) {
                double rx = selectedRect.getX();
                double ry = selectedRect.getY();
                if (x >= rx && x <= rx + selectedRect.getWidth() &&
                    y >= ry && y <= ry + selectedRect.getHeight()) {
                    confirmAndDeleteSelectedAnnotation();
                    return;
                }
            }
        }
        
        // 右键点击空白处不执行任何操作（保留左键点击选中/取消选中的逻辑）
    }
    
    private void updateCursor() {
        if (isRectangleToolActive) {
            canvasContainer.setCursor(Cursor.CROSSHAIR);
        } else {
            canvasContainer.setCursor(Cursor.OPEN_HAND);
        }
    }
    
    private void constrainViewport() {
        if (currentImage == null) return;
        
        double containerWidth = canvasContainer.getWidth();
        double containerHeight = canvasContainer.getHeight();
        
        // Allow some slack so user can center the image
        double minX = Math.min(0, containerWidth - imageWidth);
        double minY = Math.min(0, containerHeight - imageHeight);
        double maxX = Math.max(0, containerWidth - imageWidth);
        double maxY = Math.max(0, containerHeight - imageHeight);
        
        viewportX = Math.max(minX, Math.min(maxX, viewportX));
        viewportY = Math.max(minY, Math.min(maxY, viewportY));
    }
    
    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            loadImage(file);
        }
    }
    
    private void loadImage(File file) {
        try {
            currentImageFile = file;
            currentExcelFile = null;  // 重置关联的Excel文件
            
            currentImage = new Image(file.toURI().toString());
            imageName = file.getName();
            int dotIndex = imageName.lastIndexOf('.');
            if (dotIndex > 0) {
                imageName = imageName.substring(0, dotIndex);
            }
            
            imageWidth = currentImage.getWidth();
            imageHeight = currentImage.getHeight();
            
            // Clear previous annotations
            clearAnnotations();
            
            // Center the image initially
            viewportX = (canvasContainer.getWidth() - imageWidth) / 2;
            viewportY = (canvasContainer.getHeight() - imageHeight) / 2;
            
            // Update canvas size
            imageCanvas.setWidth(canvasContainer.getWidth());
            imageCanvas.setHeight(canvasContainer.getHeight());
            
            imageInfoLabel.setText(String.format("%s (%.0f x %.0f)", 
                file.getName(), imageWidth, imageHeight));
            statusLabel.setText("图片已加载");
            
            // Save the path of successfully loaded image
            Preferences prefs = Preferences.userNodeForPackage(PicMarkApp.class);
            prefs.put(PREF_LAST_IMAGE_PATH, file.getAbsolutePath());
            
            render();
            
            // 检查同级目录下是否存在同名 Excel 文件，如果有则自动加载
            autoLoadAnnotations(file);
            
            // Setup resize listener
            canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
                imageCanvas.setWidth(newVal.doubleValue());
                render();
            });
            canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
                imageCanvas.setHeight(newVal.doubleValue());
                render();
            });
            
        } catch (Exception e) {
            showError("加载图片失败: " + e.getMessage());
        }
    }
    
    /**
     * 自动加载与图片同名的 Excel 标注文件
     */
    private void autoLoadAnnotations(File imageFile) {
        File parentDir = imageFile.getParentFile();
        String baseName = imageName;
        
        // 可能的 Excel 文件名
        File xlsxFile = new File(parentDir, baseName + ".xlsx");
        File xlsFile = new File(parentDir, baseName + ".xls");
        
        currentExcelFile = null;
        if (xlsxFile.exists()) {
            currentExcelFile = xlsxFile;
        } else if (xlsFile.exists()) {
            currentExcelFile = xlsFile;
        }
        
        if (currentExcelFile != null) {
            try {
                List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(currentExcelFile);
                
                // 清除现有标注并加载新的
                clearAnnotations();
                
                for (RectangleAnnotation annotation : loaded) {
                    annotations.add(annotation);
                    addVisualRectangle(annotation);
                }
                
                statusLabel.setText("图片已加载，自动导入 " + annotations.size() + " 个标注");
                
            } catch (Exception e) {
                // 自动加载失败不显示错误弹窗，仅在状态栏提示
                statusLabel.setText("图片已加载，自动导入标注失败: " + e.getMessage());
                currentExcelFile = null;
            }
        }
    }
    
    /**
     * 自动保存标注到当前关联的Excel文件
     * 如果没有关联的文件，则自动创建与图片同名的Excel文件
     */
    private void autoSaveAnnotations() {
        if (annotations.isEmpty()) {
            return;
        }
        
        // 如果没有关联的Excel文件，尝试查找或创建
        if (currentExcelFile == null) {
            // 获取图片所在目录
            if (currentImageFile == null) {
                return;
            }
            
            File parentDir = currentImageFile.getParentFile();
            if (parentDir == null || !parentDir.exists()) {
                return;
            }
            
            // 检查是否已存在同名Excel文件
            File xlsxFile = new File(parentDir, imageName + ".xlsx");
            File xlsFile = new File(parentDir, imageName + ".xls");
            
            if (xlsxFile.exists()) {
                currentExcelFile = xlsxFile;
            } else if (xlsFile.exists()) {
                currentExcelFile = xlsFile;
            } else {
                // 自动创建新的Excel文件
                currentExcelFile = xlsxFile;
            }
        }
        
        try {
            ExcelHandler.saveAnnotations(annotations, imageName, currentExcelFile);
            System.out.println("自动保存成功: " + currentExcelFile.getAbsolutePath());
            statusLabel.setText("已自动保存 " + annotations.size() + " 个标注到: " + currentExcelFile.getName());
        } catch (Exception e) {
            System.err.println("自动保存失败: " + e.getMessage());
            statusLabel.setText("自动保存失败: " + e.getMessage());
        }
    }
    
    private void render() {
        if (currentImage == null) return;
        
        gc.clearRect(0, 0, imageCanvas.getWidth(), imageCanvas.getHeight());
        gc.drawImage(currentImage, viewportX, viewportY);
        
        // Update annotation positions - 重新计算矩形位置
        for (RectangleAnnotation annotation : annotations) {
            Rectangle rect = annotation.getVisualRect();
            if (rect != null) {
                double[] bounds = annotation.getVisualBounds();
                rect.setX(bounds[0] + viewportX);
                rect.setY(bounds[1] + viewportY);
            }
        }
        
        // 更新删除按钮位置
        if (selectedAnnotation != null && deleteButton != null) {
            Rectangle rect = selectedAnnotation.getVisualRect();
            if (rect != null) {
                deleteButton.setLayoutX(rect.getX() + rect.getWidth() - 30);
                deleteButton.setLayoutY(rect.getY() - 15);
            }
        }
    }
    
    private void saveAnnotations() {
        if (annotations.isEmpty()) {
            showInfo("没有标注需要保存");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存标注");
        fileChooser.setInitialFileName(imageName + ".xlsx");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel文件", "*.xlsx")
        );
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                ExcelHandler.saveAnnotations(annotations, imageName, file);
                // 更新当前关联的Excel文件
                currentExcelFile = file;
                showInfo("标注已保存到: " + file.getName());
            } catch (Exception e) {
                showError("保存失败: " + e.getMessage());
            }
        }
    }
    
    private void importAnnotations() {
        if (currentImage == null) {
            showInfo("请先打开一张图片");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导入点坐标");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel文件", "*.xlsx", "*.xls")
        );
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                List<RectangleAnnotation> loaded = ExcelHandler.loadAnnotations(file);
                
                // Clear existing annotations
                clearAnnotations();
                
                // Add loaded annotations
                for (RectangleAnnotation annotation : loaded) {
                    annotations.add(annotation);
                    addVisualRectangle(annotation);
                }
                
                // 设置当前Excel文件路径，用于后续自动保存
                currentExcelFile = file;
                
                statusLabel.setText("已导入 " + annotations.size() + " 个标注，已关联: " + file.getName());
                showInfo("成功导入 " + annotations.size() + " 个标注");
                
            } catch (Exception e) {
                showError("导入失败: " + e.getMessage());
            }
        }
    }
    
    private void clearAnnotations() {
        annotations.clear();
        overlayPane.getChildren().clear();
        selectedAnnotation = null;
        deleteButton = null;
        statusLabel.setText("标注已清除");
    }
    
    private void confirmAndClearAnnotations() {
        if (annotations.isEmpty()) {
            showInfo("当前没有标注");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.initOwner(primaryStage);
        confirmDialog.setTitle("确认清除");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("确定要清除全部 " + annotations.size() + " 个标注吗？\n此操作不可撤销。");
        
        // 设置按钮文本
        ButtonType confirmButton = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(confirmButton, cancelButton);
        
        // 显示对话框并等待结果
        confirmDialog.showAndWait().ifPresent(result -> {
            if (result == confirmButton) {
                clearAnnotations();
            }
        });
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(primaryStage);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String getButtonStyle(String color) {
        return String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 8 16; -fx-background-radius: 4;", color);
    }
    
    private String getToggleButtonStyle() {
        return "-fx-background-color: #555555; -fx-text-fill: white; -fx-font-weight: bold; " +
               "-fx-padding: 8 16; -fx-background-radius: 4;";
    }
    
    private String getToggleButtonStyleActive() {
        return "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; " +
               "-fx-padding: 8 16; -fx-background-radius: 4;";
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
