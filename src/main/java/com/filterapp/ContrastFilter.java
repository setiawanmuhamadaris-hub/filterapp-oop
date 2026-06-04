package com.filterapp;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ContrastFilter implements Filter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        
        double contrast = 1.5; // Faktor kontras (di atas 1.0 akan meningkatkan kontras)

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color(image.getRGB(x, y));
                
                int r = applyContrast(c.getRed(), contrast);
                int g = applyContrast(c.getGreen(), contrast);
                int b = applyContrast(c.getBlue(), contrast);
                
                result.setRGB(x, y, new Color(r, g, b).getRGB());
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