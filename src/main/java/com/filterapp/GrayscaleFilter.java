package com.filterapp;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class GrayscaleFilter implements Filter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Buat gambar baru agar gambar asli tidak tertimpa
        BufferedImage result = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Ambil warna pixel saat ini
                Color c = new Color(image.getRGB(x, y));
                
                // Hitung nilai grayscale menggunakan rumus luminans standar
                int gray = (int) (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
                
                // Set warna baru
                Color newColor = new Color(gray, gray, gray);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }
}