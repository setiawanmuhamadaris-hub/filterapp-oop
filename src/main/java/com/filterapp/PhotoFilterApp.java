package com.filterapp;

import com.filterapp.controllers.ImageController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PhotoFilterApp extends Application {

    private ImageController controller;
    private ImageView imageView;

    // Sliders
    private Slider brightnessSlider, contrastSlider, saturationSlider;
    private Slider fadeSlider, temperatureSlider, vignetteSlider;

    // Crop state
    private double currentPanX   = 0.5;
    private double currentPanY   = 0.5;
    private double currentCropRatio = 0.0;
    private double currentCropZoom  = 1.0;
    private boolean isCropMode = true;

    // Drag state
    private double dragStartX, dragStartY;
    private double dragStartPanX = 0.5, dragStartPanY = 0.5;

    // The unfiltered full-preview image – used as the "map tile" during drag
    private Image basePreviewFxImage = null;

    // Generation counter: prevents stale background results from overwriting fresh ones
    private int updateGeneration = 0;

    // UI overlays
    private GridPane gridOverlay;
    private ProgressIndicator loadingIndicator;
    private VBox uploadPlaceholder;
    private Button btnDelete;
    private Button btnPreview;
    private Stage currentPreviewStage;

    @Override
    public void start(Stage primaryStage) {
        controller = new ImageController();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");

        // ---------- Image View ----------
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);
        imageView.setFitHeight(500);

        gridOverlay = new GridPane();
        gridOverlay.setMouseTransparent(true);
        gridOverlay.setVisible(false);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                Region cell = new Region();
                cell.setStyle("-fx-border-color:rgba(255,255,255,0.5);-fx-border-width:1;");
                GridPane.setHgrow(cell, Priority.ALWAYS);
                GridPane.setVgrow(cell, Priority.ALWAYS);
                gridOverlay.add(cell, i, j);
            }

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);
        loadingIndicator.setVisible(false);

        uploadPlaceholder = new VBox(15);
        uploadPlaceholder.setAlignment(Pos.CENTER);
        uploadPlaceholder.setMaxSize(300, 200);
        uploadPlaceholder.setStyle("-fx-border-color: #555; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-color: transparent; -fx-cursor: hand;");
        
        javafx.scene.shape.SVGPath uploadIcon = new javafx.scene.shape.SVGPath();
        uploadIcon.setContent("M9 16h6v-6h4l-7-7-7 7h4v6zm-4 2h14v2H5v-2z");
        uploadIcon.setFill(javafx.scene.paint.Color.web("#888888"));
        uploadIcon.setScaleX(3);
        uploadIcon.setScaleY(3);
        uploadIcon.setTranslateY(10);
        
        Label uploadLabel = new Label("Klik untuk Upload Foto");
        uploadLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 16px; -fx-font-weight: bold;");
        uploadLabel.setTranslateY(15);
        
        uploadPlaceholder.getChildren().addAll(uploadIcon, uploadLabel);
        
        uploadPlaceholder.setOnMouseEntered(e -> {
            uploadPlaceholder.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-color: rgba(76, 175, 80, 0.1); -fx-cursor: hand;");
            uploadIcon.setFill(javafx.scene.paint.Color.web("#4CAF50"));
            uploadLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px; -fx-font-weight: bold;");
        });
        uploadPlaceholder.setOnMouseExited(e -> {
            uploadPlaceholder.setStyle("-fx-border-color: #555; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-color: transparent; -fx-cursor: hand;");
            uploadIcon.setFill(javafx.scene.paint.Color.web("#888888"));
            uploadLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 16px; -fx-font-weight: bold;");
        });
        uploadPlaceholder.setOnMouseClicked(e -> handleUpload(primaryStage));

        StackPane innerStack = new StackPane(imageView, gridOverlay);
        Group imageGroup = new Group(innerStack);

        StackPane imageContainer = new StackPane(imageGroup, uploadPlaceholder, loadingIndicator);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(20));

        // ---------- Mouse handlers ----------
        imageView.setOnMouseEntered(e -> {
            if (isCropMode && currentCropRatio > 0)
                imageView.setCursor(javafx.scene.Cursor.OPEN_HAND);
        });

        imageView.setOnMousePressed(e -> {
            if (!isCropMode || currentCropRatio <= 0 || basePreviewFxImage == null) return;
            dragStartX   = e.getSceneX();
            dragStartY   = e.getSceneY();
            dragStartPanX = currentPanX;
            dragStartPanY = currentPanY;
            imageView.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            // Switch to the full unfiltered image for instant viewport panning
            imageView.setImage(basePreviewFxImage);
            applyViewport();
            gridOverlay.setVisible(true);
        });

        imageView.setOnMouseDragged(e -> {
            if (!isCropMode || currentCropRatio <= 0 || basePreviewFxImage == null) return;

            double dx = dragStartX - e.getSceneX();
            double dy = dragStartY - e.getSceneY();

            // How much the crop window moves relative to image size, scaled by zoom
            double senX = (dx / imageView.getBoundsInLocal().getWidth())  / currentCropZoom;
            double senY = (dy / imageView.getBoundsInLocal().getHeight()) / currentCropZoom;

            currentPanX = Math.max(0, Math.min(1, dragStartPanX + senX));
            currentPanY = Math.max(0, Math.min(1, dragStartPanY + senY));

            // *** INSTANT update – zero image processing, pure JavaFX GPU ***
            applyViewport();
        });

        imageView.setOnMouseReleased(e -> {
            if (!isCropMode || currentCropRatio <= 0) return;
            imageView.setCursor(javafx.scene.Cursor.OPEN_HAND);
            gridOverlay.setVisible(false);
            // Process synchronously — 800px preview + fast filters = <5ms, unnoticeable
            showCropResultNow();
        });

        imageView.setOnScroll(e -> {
            if (!isCropMode || currentCropRatio <= 0 || basePreviewFxImage == null) return;
            currentCropZoom += (e.getDeltaY() > 0) ? 0.1 : -0.1;
            currentCropZoom = Math.max(1.0, Math.min(5.0, currentCropZoom));
            imageView.setImage(basePreviewFxImage);
            applyViewport();
            showCropResultNow();
        });

        root.setCenter(imageContainer);

        // ---------- Top bar ----------
        HBox topBar = new HBox(15);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setStyle("-fx-background-color:#2b2b2b;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        btnDelete = new Button("Hapus Foto");
        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;");
        btnDelete.setVisible(false);
        btnDelete.setManaged(false);
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-cursor: hand;"));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;"));
        btnDelete.setOnAction(e -> handleClearImage());

        btnPreview = new Button("Pratinjau");
        btnPreview.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;");
        btnPreview.setVisible(false);
        btnPreview.setManaged(false);
        btnPreview.setOnMouseEntered(e -> btnPreview.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-cursor: hand;"));
        btnPreview.setOnMouseExited(e -> btnPreview.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-cursor: hand;"));
        btnPreview.setOnAction(e -> showPreviewDialog());

        Button btnSave   = new Button("Simpan");
        Button btnReset  = new Button("Reset");
        btnSave  .setOnAction(e -> handleSave(primaryStage));
        btnReset .setOnAction(e -> handleReset());
        topBar.getChildren().addAll(btnPreview, btnDelete, btnSave, btnReset);
        root.setTop(topBar);

        // ---------- Sidebar ----------
        TabPane sidebar = new TabPane();
        sidebar.setPrefWidth(300);
        sidebar.setStyle("-fx-background-color:#2b2b2b;");

        Tab tabCrop    = new Tab("Crop",        createCropPane());
        Tab tabFilter  = new Tab("Filters",     createFiltersPane());
        Tab tabAdj     = new Tab("Adjustments", createAdjustmentsPane());
        tabCrop.setClosable(false);
        tabFilter.setClosable(false);
        tabAdj.setClosable(false);
        sidebar.getTabs().addAll(tabCrop, tabFilter, tabAdj);

        sidebar.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            isCropMode = (n == tabCrop);
            if (!isCropMode) {
                imageView.setCursor(javafx.scene.Cursor.DEFAULT);
                gridOverlay.setVisible(false);
            } else if (currentCropRatio > 0) {
                imageView.setCursor(javafx.scene.Cursor.OPEN_HAND);
            }
        });
        root.setRight(sidebar);

        // ---------- Scene ----------
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add("data:text/css,"
                + ".tab-pane .tab-header-area .tab-header-background{-fx-background-color:#2b2b2b;}"
                + ".tab-pane .tab{-fx-background-color:#3c3c3c;-fx-text-base-color:white;}"
                + ".tab-pane .tab:selected{-fx-background-color:#555;}"
                + ".button{-fx-background-color:#4CAF50;-fx-text-fill:white;-fx-cursor:hand;}"
                + ".button:hover{-fx-background-color:#45a049;}"
                + ".label{-fx-text-fill:white;}");

        primaryStage.setTitle("Aplikasi Filter Foto Pro");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // =======================================================================
    // Viewport (INSTANT – called directly on UI thread during drag)
    // =======================================================================
    private void applyViewport() {
        if (basePreviewFxImage == null || currentCropRatio <= 0) {
            imageView.setViewport(null);
            return;
        }

        double origW = basePreviewFxImage.getWidth();
        double origH = basePreviewFxImage.getHeight();
        double origRatio = origW / origH;

        double targetW, targetH;
        if (origRatio > currentCropRatio) {
            targetH = origH;
            targetW = origH * currentCropRatio;
        } else {
            targetW = origW;
            targetH = origW / currentCropRatio;
        }

        targetW = targetW / currentCropZoom;
        targetH = targetH / currentCropZoom;

        targetW = Math.min(targetW, origW);
        targetH = Math.min(targetH, origH);

        double maxOffsetX = origW - targetW;
        double maxOffsetY = origH - targetH;

        double startX = maxOffsetX * currentPanX;
        double startY = maxOffsetY * currentPanY;
        startX = Math.max(0, Math.min(startX, maxOffsetX));
        startY = Math.max(0, Math.min(startY, maxOffsetY));

        imageView.setViewport(new javafx.geometry.Rectangle2D(startX, startY, targetW, targetH));
    }

    // =======================================================================
    // Synchronous crop result
    // =======================================================================
    private void showCropResultNow() {
        if (controller.getOriginalImage() == null) return;
        controller.setCrop(currentCropRatio, currentPanX, currentPanY, currentCropZoom);
        controller.applyCropPreview();
        java.awt.image.BufferedImage buf = controller.getFilteredImage();
        if (buf != null) {
            imageView.setViewport(null);
            imageView.setImage(SwingFXUtils.toFXImage(buf, null));
        }
    }

    private void updateBaseImageAsync() {
        if (controller.getOriginalImage() == null) return;
        controller.setCrop(currentCropRatio, currentPanX, currentPanY, currentCropZoom);

        final int myGeneration = ++updateGeneration;

        loadingIndicator.setVisible(true);
        imageView.setOpacity(0.6);

        new Thread(() -> {
            controller.updateBaseImage();

            java.awt.image.BufferedImage rawBuf = controller.getOriginalImage();
            Image rawFx = (rawBuf != null) ? SwingFXUtils.toFXImage(rawBuf, null) : null;

            java.awt.image.BufferedImage filtBuf = controller.getFilteredImage();
            Image filtFx = (filtBuf != null) ? SwingFXUtils.toFXImage(filtBuf, null) : null;

            Platform.runLater(() -> {
                if (myGeneration != updateGeneration) return;

                if (rawFx != null) basePreviewFxImage = rawFx;
                loadingIndicator.setVisible(false);
                imageView.setOpacity(1.0);
                imageView.setViewport(null);
                if (filtFx != null) imageView.setImage(filtFx);
            });
        }).start();
    }

    private void updateAdjustmentsOnly() {
        if (controller.getOriginalImage() == null) return;
        controller.setAdjustments(
                (int) brightnessSlider.getValue(),
                (int) contrastSlider.getValue(),
                (int) saturationSlider.getValue(),
                (int) fadeSlider.getValue(),
                (int) temperatureSlider.getValue(),
                (int) vignetteSlider.getValue()
        );
        new Thread(() -> {
            controller.applyAdjustments();
            java.awt.image.BufferedImage buf = controller.getFilteredImage();
            if (buf != null) {
                Image img = SwingFXUtils.toFXImage(buf, null);
                Platform.runLater(() -> imageView.setImage(img));
            }
        }).start();
    }

    // =======================================================================
    // Sidebar panes
    // =======================================================================
    private VBox createCropPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        pane.setAlignment(Pos.TOP_CENTER);

        Label hint = new Label("Pilih rasio, geser gambar untuk memilih posisi, scroll untuk zoom.");
        hint.setWrapText(true);

        Button btnOrig = new Button("Original");
        Button btn1x1  = new Button("1:1 (Square)");
        Button btn4x5  = new Button("4:5 (Portrait)");
        Button btn16x9 = new Button("16:9 (Landscape)");
        for (Button b : new Button[]{btnOrig, btn1x1, btn4x5, btn16x9})
            b.setMaxWidth(Double.MAX_VALUE);

        btnOrig.setOnAction(e -> { currentCropRatio = 0.0; currentCropZoom = 1.0; imageView.setViewport(null); updateBaseImageAsync(); });
        btn1x1 .setOnAction(e -> { currentCropRatio = 1.0;        updateBaseImageAsync(); });
        btn4x5 .setOnAction(e -> { currentCropRatio = 4.0 / 5.0;  updateBaseImageAsync(); });
        btn16x9.setOnAction(e -> { currentCropRatio = 16.0 / 9.0; updateBaseImageAsync(); });

        pane.getChildren().addAll(hint, btnOrig, btn1x1, btn4x5, btn16x9);
        return pane;
    }

    private ScrollPane createFiltersPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        String[] names = {"None", "Grayscale", "Sepia", "Blur", "Sharpen", "Watercolor Paint", "BW Portrait"};
        String[] imageFiles = {
            "sample_normal.jpg",
            "sample_grayscale.jpg",
            "sample_sepia.jpg",
            "sample_blur.jpg",
            "sample_sharpen.jpg",
            "sample_watercolor_paint.jpg",
            "sample_BW_potrait.jpg"
        };

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String imagePath = "file:media/" + imageFiles[i];

            Button btn = new Button(name);
            btn.setContentDisplay(ContentDisplay.TOP);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle("-fx-background-color: transparent; -fx-border-color: #555; -fx-border-radius: 5; -fx-text-fill: white; -fx-cursor: hand;");

            try {
                Image img = new Image(imagePath, 120, 120, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(100);
                iv.setFitHeight(100);
                iv.setPreserveRatio(true);
                btn.setGraphic(iv);
            } catch (Exception ex) {
                // Fallback if image not found
            }

            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #4CAF50; -fx-border-color: #4CAF50; -fx-border-radius: 5; -fx-text-fill: white; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-border-color: #555; -fx-border-radius: 5; -fx-text-fill: white; -fx-cursor: hand;"));

            btn.setOnAction(e -> { controller.setBaseFilter(name); updateBaseImageAsync(); });

            grid.add(btn, i % 2, i / 2);
            GridPane.setHgrow(btn, Priority.ALWAYS);
        }

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background:#2b2b2b;-fx-border-color:transparent;");
        return sp;
    }

    private VBox createAdjustmentsPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(20));
        brightnessSlider  = mkSlider("Brightness",  -100, 100, 0, pane);
        contrastSlider    = mkSlider("Contrast",    -100, 100, 0, pane);
        saturationSlider  = mkSlider("Saturation",  -100, 100, 0, pane);
        fadeSlider        = mkSlider("Fade",        -100, 100, 0, pane);
        temperatureSlider = mkSlider("Temperature", -100, 100, 0, pane);
        vignetteSlider    = mkSlider("Vignette",       0, 100, 0, pane);
        return pane;
    }

    private Slider mkSlider(String name, double min, double max, double def, VBox parent) {
        Label lbl = new Label(name);
        Slider sl  = new Slider(min, max, def);
        Label val  = new Label(String.valueOf((int) def));
        val.setMinWidth(25);
        val.setAlignment(Pos.CENTER_RIGHT);
        Label rst = new Label("Reset");
        rst.setStyle("-fx-text-fill:#4CAF50;-fx-cursor:hand;-fx-underline:true;-fx-font-size:10px;");
        rst.setOnMouseClicked(e -> sl.setValue(def));
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox header = new HBox(5, lbl, sp, rst, val);
        header.setAlignment(Pos.CENTER_LEFT);
        sl.valueProperty().addListener((o, ov, nv) -> { val.setText(String.valueOf(nv.intValue())); updateAdjustmentsOnly(); });
        parent.getChildren().add(new VBox(5, header, sl));
        return sl;
    }

    // =======================================================================
    // Button handlers
    // =======================================================================
    private void handleUpload(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Gambar");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg","*.jpeg"));
        File file = fc.showOpenDialog(stage);
        if (file == null) return;
        try {
            controller.uploadImage(file);
            uploadPlaceholder.setVisible(false);
            btnDelete.setVisible(true);
            btnDelete.setManaged(true);
            btnPreview.setVisible(true);
            btnPreview.setManaged(true);
            currentCropRatio = 0; currentPanX = 0.5; currentPanY = 0.5; currentCropZoom = 1.0;
            brightnessSlider.setValue(0); contrastSlider.setValue(0); saturationSlider.setValue(0);
            fadeSlider.setValue(0); temperatureSlider.setValue(0); vignetteSlider.setValue(0);
            basePreviewFxImage = null;
            imageView.setViewport(null);
            updateBaseImageAsync();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat gambar: " + ex.getMessage());
        }
    }

    private void handleClearImage() {
        controller.clearImage();
        imageView.setImage(null);
        imageView.setViewport(null);
        basePreviewFxImage = null;

        currentCropRatio = 0; currentPanX = 0.5; currentPanY = 0.5; currentCropZoom = 1.0;
        brightnessSlider.setValue(0); contrastSlider.setValue(0); saturationSlider.setValue(0);
        fadeSlider.setValue(0); temperatureSlider.setValue(0); vignetteSlider.setValue(0);

        uploadPlaceholder.setVisible(true);
        btnDelete.setVisible(false);
        btnDelete.setManaged(false);
        btnPreview.setVisible(false);
        btnPreview.setManaged(false);
        gridOverlay.setVisible(false);
    }

    private void handleReset() {
        brightnessSlider.setValue(0); contrastSlider.setValue(0); saturationSlider.setValue(0);
        fadeSlider.setValue(0); temperatureSlider.setValue(0); vignetteSlider.setValue(0);
        controller.setBaseFilter("None");
        updateBaseImageAsync();
    }

    private void handleSave(Stage stage) {
        if (controller.getFilteredImage() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tidak ada gambar untuk disimpan!");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Simpan Gambar");
        fc.setInitialFileName("filtered_photo.jpg");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG files (*.jpg)","*.jpg","*.jpeg"),
                new FileChooser.ExtensionFilter("PNG files (*.png)","*.png"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;
        try {
            String fmt = file.getName().toLowerCase().endsWith(".png") ? "png" : "jpg";
            controller.saveImage(file, fmt);
            showAlert(Alert.AlertType.INFORMATION, "Sukses",
                    "Gambar berhasil disimpan di:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showPreviewDialog() {
        if (controller.getOriginalImage() == null || controller.getFilteredImage() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Gambar belum siap untuk dipratinjau!");
            return;
        }

        if (currentPreviewStage != null && currentPreviewStage.isShowing()) {
            currentPreviewStage.toFront();
            return;
        }

        currentPreviewStage = new Stage();
        currentPreviewStage.setTitle("Pratinjau Perbandingan - Asli vs Hasil Edit");

        Image originalImg = SwingFXUtils.toFXImage(controller.getOriginalImage(), null);
        Image filteredImg = SwingFXUtils.toFXImage(controller.getFilteredImage(), null);

        ImageView originalView = new ImageView(originalImg);
        originalView.setPreserveRatio(true);
        originalView.setFitWidth(400);
        originalView.setFitHeight(400);

        ImageView filteredView = new ImageView(filteredImg);
        filteredView.setPreserveRatio(true);
        filteredView.setFitWidth(400);
        filteredView.setFitHeight(400);

        VBox leftBox = new VBox(10, new Label("Original / Asli"), originalView);
        leftBox.setAlignment(Pos.CENTER);
        
        VBox rightBox = new VBox(10, new Label("Filtered / Hasil Edit"), filteredView);
        rightBox.setAlignment(Pos.CENTER);

        HBox layout = new HBox(20, leftBox, rightBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e1e1e;");

        // Set style label manually since we don't apply CSS file here
        leftBox.getChildren().get(0).setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        rightBox.getChildren().get(0).setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Scene scene = new Scene(layout);
        currentPreviewStage.setScene(scene);
        currentPreviewStage.show();
    }

    public static void main(String[] args) { launch(args); }
}