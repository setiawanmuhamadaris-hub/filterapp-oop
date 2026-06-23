package com.filterapp.filters.adjust;
import com.filterapp.filters.Filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BrightnessFilter implements Filter {

    private int level;

    public BrightnessFilter(int level) {
        this.level = level;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        
        int brightness = level; // Tingkat penambahan cahaya

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int cRed = (rgb >> 16) & 0xFF;
                int cGreen = (rgb >> 8) & 0xFF;
                int cBlue = rgb & 0xFF;
                int cAlpha = (rgb >> 24) & 0xFF;
                
                // Tambahkan nilai dan pastikan tidak melebihi batas maksimal 255
                int r = Math.min(255, Math.max(0, cRed + brightness));
                int g = Math.min(255, Math.max(0, cGreen + brightness));
                int b = Math.min(255, Math.max(0, cBlue + brightness));
                
                result.setRGB(x, y, ((cAlpha << 24) | (r << 16) | (g << 8) | b));
            }
        }
        return result;
    }
}
