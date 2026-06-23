package com.filterapp.filters.apply;
import com.filterapp.filters.Filter;

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
                int rgb = image.getRGB(x, y);
                int cRed = (rgb >> 16) & 0xFF;
                int cGreen = (rgb >> 8) & 0xFF;
                int cBlue = rgb & 0xFF;
                int cAlpha = (rgb >> 24) & 0xFF;
                
                // Hitung nilai grayscale menggunakan rumus luminans standar
                int gray = (int) (0.299 * cRed + 0.587 * cGreen + 0.114 * cBlue);
                
                // Set warna baru
                result.setRGB(x, y, (cAlpha << 24) | (gray << 16) | (gray << 8) | gray);
            }
        }
        return result;
    }
}
