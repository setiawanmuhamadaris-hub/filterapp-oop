package com.filterapp.filters.adjust;
import com.filterapp.filters.Filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ContrastFilter implements Filter {

    private int level;

    public ContrastFilter(int level) {
        this.level = level;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        
        // Convert slider value (-100 to 100) to contrast factor
        double contrast = (level + 100) / 100.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int cRed = (rgb >> 16) & 0xFF;
                int cGreen = (rgb >> 8) & 0xFF;
                int cBlue = rgb & 0xFF;
                int cAlpha = (rgb >> 24) & 0xFF;
                
                int r = applyContrast(cRed, contrast);
                int g = applyContrast(cGreen, contrast);
                int b = applyContrast(cBlue, contrast);
                
                result.setRGB(x, y, ((cAlpha << 24) | (r << 16) | (g << 8) | b));
            }
        }
        return result;
    }

    // Fungsi bantuan untuk menghitung rumus kontras
    private int applyContrast(int colorValue, double contrast) {
        int newValue = (int) (((((colorValue / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
        return Math.min(255, Math.max(0, newValue));
    }
}
