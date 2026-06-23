package com.filterapp.filters.adjust;
import com.filterapp.filters.Filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class SaturationFilter implements Filter {
    private int level; // -100 to 100

    public SaturationFilter(int level) {
        this.level = level;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        
        // Convert slider value (-100 to 100) to saturation multiplier (0.0 to 2.0)
        double saturationMultiplier = (level + 100) / 100.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int cRed = (rgb >> 16) & 0xFF;
                int cGreen = (rgb >> 8) & 0xFF;
                int cBlue = rgb & 0xFF;
                int cAlpha = (rgb >> 24) & 0xFF;
                
                // Convert RGB to HSB
                float[] hsb = Color.RGBtoHSB(cRed, cGreen, cBlue, null);
                
                // Adjust saturation
                hsb[1] = (float) Math.min(1.0, Math.max(0.0, hsb[1] * saturationMultiplier));
                
                // Convert back to RGB
                int outRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
                result.setRGB(x, y, outRgb);
            }
        }
        return result;
    }
}
