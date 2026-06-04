package com.filterapp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageController {
    // Menerapkan enkapsulasi dengan modifier private
    private BufferedImage originalImage;
    private BufferedImage filteredImage;

    // Menangani aksi upload
    public void uploadImage(File file) throws IOException {
        originalImage = ImageUtils.loadImage(file);
        filteredImage = originalImage; // Default awal adalah gambar tanpa filter
    }

    // Menerapkan filter (menggunakan polimorfisme dari interface Filter)
    public void applyFilter(Filter filter) {
        if (originalImage != null && filter != null) {
            filteredImage = filter.apply(originalImage);
        }
    }

    // Mengembalikan ke gambar asli
    public void resetImage() {
        if (originalImage != null) {
            filteredImage = originalImage;
        }
    }

    // Menyimpan hasil gambar
    public void saveImage(File file, String format) throws IOException {
        if (filteredImage != null) {
            ImageUtils.saveImage(filteredImage, file, format);
        }
    }

    // Getter untuk diakses oleh GUI
    public BufferedImage getOriginalImage() { 
        return originalImage; 
    }
    
    public BufferedImage getFilteredImage() { 
        return filteredImage; 
    }
}