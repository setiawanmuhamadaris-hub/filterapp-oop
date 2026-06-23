package com.filterapp.controllers;

import com.filterapp.filters.Filter;
import com.filterapp.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageController {
    private BufferedImage originalImage;
    private BufferedImage originalPreviewImage;
    
    private BufferedImage baseFilteredImage; // Full resolution
    private BufferedImage previewBaseImage;  // Fast 800px preview
    
    private BufferedImage filteredPreviewImage; // Preview with adjustments

    // States for the pipeline
    private double cropRatio = 0.0;
    private double cropPanX = 0.5;
    private double cropPanY = 0.5;
    private double cropZoom = 1.0;

    private String baseFilterName = "None";

    private int brightnessLevel = 0;
    private int contrastLevel = 0;
    private int saturationLevel = 0;
    private int fadeLevel = 0;
    private int temperatureLevel = 0;
    private int vignetteLevel = 0;

    public void uploadImage(File file) throws IOException {
        originalImage = ImageUtils.loadImage(file);
        originalPreviewImage = ImageUtils.resize(originalImage, 800);
        resetAllSettings();
    }

    public void resetAllSettings() {
        cropRatio = 0.0;
        cropPanX = 0.5;
        cropPanY = 0.5;
        cropZoom = 1.0;
        resetFilters();
    }

    public void resetFilters() {
        baseFilterName = "None";
        brightnessLevel = 0;
        contrastLevel = 0;
        saturationLevel = 0;
        fadeLevel = 0;
        temperatureLevel = 0;
        vignetteLevel = 0;
        updateBaseImage();
    }

    // Setters
    public void setCrop(double ratio, double panX, double panY, double zoom) {
        this.cropRatio = ratio;
        this.cropPanX = panX;
        this.cropPanY = panY;
        this.cropZoom = zoom;
    }

    public void setBaseFilter(String name) {
        this.baseFilterName = name;
    }

    public void setAdjustments(int brightness, int contrast, int saturation, int fade, int temp, int vignette) {
        this.brightnessLevel = brightness;
        this.contrastLevel = contrast;
        this.saturationLevel = saturation;
        this.fadeLevel = fade;
        this.temperatureLevel = temp;
        this.vignetteLevel = vignette;
    }

    public void applyCropPreview() {
        if (originalPreviewImage == null) return;
        
        BufferedImage img = originalPreviewImage;
        if (cropRatio > 0) {
            img = new com.filterapp.filters.CropFilter(cropRatio, cropPanX, cropPanY, cropZoom).apply(img);
        }
        previewBaseImage = img;
        applyAdjustments();
    }

    public void updateBaseImage() {
        if (originalImage == null) return;
        
        BufferedImage img = originalImage;
        
        // 1. Crop (Full Res)
        if (cropRatio > 0) {
            img = new com.filterapp.filters.CropFilter(cropRatio, cropPanX, cropPanY, cropZoom).apply(img);
        }
        
        // 2. Base Filter (Full Res)
        Filter baseFilter = getBaseFilterByName(baseFilterName);
        if (baseFilter != null) {
            img = baseFilter.apply(img);
        }
        
        baseFilteredImage = img;
        previewBaseImage = ImageUtils.resize(baseFilteredImage, 800);
        
        applyAdjustments();
    }

    public void applyAdjustments() {
        if (previewBaseImage == null) return;
        
        BufferedImage img = previewBaseImage;
        
        // 3. Adjustments (Preview)
        if (brightnessLevel != 0) img = new com.filterapp.filters.adjust.BrightnessFilter(brightnessLevel).apply(img);
        if (contrastLevel != 0) img = new com.filterapp.filters.adjust.ContrastFilter(contrastLevel).apply(img);
        if (saturationLevel != 0) img = new com.filterapp.filters.adjust.SaturationFilter(saturationLevel).apply(img);
        if (fadeLevel != 0) img = new com.filterapp.filters.adjust.FadeFilter(fadeLevel).apply(img);
        if (temperatureLevel != 0) img = new com.filterapp.filters.adjust.TemperatureFilter(temperatureLevel).apply(img);
        if (vignetteLevel != 0) img = new com.filterapp.filters.adjust.VignetteFilter(vignetteLevel).apply(img);
        
        filteredPreviewImage = img;
    }

    private Filter getBaseFilterByName(String name) {
        switch (name) {
            case "Grayscale": return new com.filterapp.filters.apply.GrayscaleFilter();
            case "Sepia": return new com.filterapp.filters.apply.SepiaFilter();
            case "Blur": return new com.filterapp.filters.apply.BlurFilter();
            case "Sharpen": return new com.filterapp.filters.apply.SharpenFilter();
            case "Watercolor Paint": return new com.filterapp.filters.apply.WatercolorPaintFilter();
            case "BW Portrait": return new com.filterapp.filters.apply.BWPortraitFilter();
            default: return null;
        }
    }

    // Menyimpan hasil gambar
    public void saveImage(File file, String format) throws IOException {
        if (baseFilteredImage != null) {
            BufferedImage img = baseFilteredImage;
            
            // Apply adjustments to Full Res before saving
            if (brightnessLevel != 0) img = new com.filterapp.filters.adjust.BrightnessFilter(brightnessLevel).apply(img);
            if (contrastLevel != 0) img = new com.filterapp.filters.adjust.ContrastFilter(contrastLevel).apply(img);
            if (saturationLevel != 0) img = new com.filterapp.filters.adjust.SaturationFilter(saturationLevel).apply(img);
            if (fadeLevel != 0) img = new com.filterapp.filters.adjust.FadeFilter(fadeLevel).apply(img);
            if (temperatureLevel != 0) img = new com.filterapp.filters.adjust.TemperatureFilter(temperatureLevel).apply(img);
            if (vignetteLevel != 0) img = new com.filterapp.filters.adjust.VignetteFilter(vignetteLevel).apply(img);
            
            // Konversi ke RGB tanpa Alpha (transparansi) jika formatnya JPG agar tidak terjadi error/warna kacau
            if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
                BufferedImage rgbImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = rgbImg.createGraphics();
                g.drawImage(img, 0, 0, java.awt.Color.WHITE, null);
                g.dispose();
                img = rgbImg;
            }
            
            ImageUtils.saveImage(img, file, format);
        }
    }

    // Getters
    public BufferedImage getOriginalImage() { return originalPreviewImage; } // UI should use preview
    public BufferedImage getFilteredImage() { return filteredPreviewImage; }
}
