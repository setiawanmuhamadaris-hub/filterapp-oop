package com.filterapp;

import com.filterapp.controllers.ImageController;
import com.filterapp.filters.*;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class PhotoFilterApp extends Application {
    private ImageController controller;
    private ImageView originalView;
    private ImageView filteredView;
    private ComboBox<String> filterCombo;

    @Override
    public void start(Stage primaryStage) {
        // Inisialisasi Controller
        controller = new ImageController();

        // --- Setup Komponen GUI ---
        originalView = new ImageView();
        originalView.setFitWidth(400);
        originalView.setFitHeight(400);
        originalView.setPreserveRatio(true);

        filteredView = new ImageView();
        filteredView.setFitWidth(400);
        filteredView.setFitHeight(400);
        filteredView.setPreserveRatio(true);

        Button btnUpload = new Button("Upload Foto");
        Button btnApply = new Button("Terapkan Filter");
        Button btnReset = new Button("Reset");
        Button btnSave = new Button("Simpan");

        // Dropdown Daftar Filter
        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("Grayscale", "Sepia", "Blur", "Sharpen", "Brightness", "Contrast", "Vignette");
        filterCombo.setValue("Grayscale"); // Nilai default

        // --- Event Handlers (Aksi Tombol) ---
        btnUpload.setOnAction(e -> handleUpload(primaryStage));
        btnReset.setOnAction(e -> handleReset());
        btnSave.setOnAction(e -> handleSave(primaryStage));
        
        // btnApply.setOnAction akan disambungkan nanti setelah class filter fisik (seperti GrayscaleFilter) kita buat.
        btnApply.setOnAction(e -> {
            if (controller.getOriginalImage() == null) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Upload gambar terlebih dahulu!");
                return;
            }

            String selectedFilter = filterCombo.getValue();
            Filter filter = null;

           // Menentukan filter mana yang dijalankan berdasarkan pilihan ComboBox
            switch (selectedFilter) {
                case "Grayscale":
                    filter = new GrayscaleFilter();
                    break;
                case "Sepia":
                    filter = new SepiaFilter();
                    break;
                case "Blur":
                    filter = new BlurFilter();
                    break;
                case "Sharpen":
                    filter = new SharpenFilter();
                    break;
                case "Brightness":
                    filter = new BrightnessFilter();
                    break;
                case "Contrast":
                    filter = new ContrastFilter();
                    break;
                case "Vignette":
                    filter = new VignetteFilter();
                    break;
                default:
                    showAlert(Alert.AlertType.INFORMATION, "Info", "Filter belum diimplementasikan.");
                    return;
            }

            // Jalankan filter lewat controller dan update tampilan
            controller.applyFilter(filter);
            updateImageViews();
        });

        // --- Layouting ---
        HBox controlPanel = new HBox(15, btnUpload, filterCombo, btnApply, btnReset, btnSave);
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPadding(new Insets(10));

        HBox imagePanel = new HBox(20, originalView, filteredView);
        imagePanel.setAlignment(Pos.CENTER);
        imagePanel.setPadding(new Insets(10));

        VBox root = new VBox(15, controlPanel, imagePanel);
        root.setPadding(new Insets(20));

        // Membangun Scene dan Menampilkan Window
        Scene scene = new Scene(root, 900, 550);
        primaryStage.setTitle("Aplikasi Filter Foto");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleUpload(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Gambar");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                controller.uploadImage(file);
                updateImageViews();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat gambar: " + ex.getMessage());
            }
        }
    }

    private void handleReset() {
        controller.resetImage();
        updateImageViews();
    }

    private void handleSave(Stage stage) {
        if (controller.getFilteredImage() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tidak ada gambar untuk disimpan!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Gambar Hasil Filter");
        // Folder default sesuai spesifikasi bisa ditangani saat pemilihan direktori
        fileChooser.setInitialFileName("filtered_photo.png"); 
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                controller.saveImage(file, "png");
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Gambar berhasil disimpan di: " + file.getAbsolutePath());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan gambar: " + ex.getMessage());
            }
        }
    }

    // Utility untuk memperbarui tampilan ImageView
    private void updateImageViews() {
        if (controller.getOriginalImage() != null) {
            // Konversi dari BufferedImage (AWT) ke Image (JavaFX)
            originalView.setImage(SwingFXUtils.toFXImage(controller.getOriginalImage(), null));
        }
        if (controller.getFilteredImage() != null) {
            filteredView.setImage(SwingFXUtils.toFXImage(controller.getFilteredImage(), null));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}