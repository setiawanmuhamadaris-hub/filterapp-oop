package com.filterapp.filters.adjust;
import com.filterapp.filters.Filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class TemperatureFilter implements Filter {

    private int level; // -100 to 100

    public TemperatureFilter(int level) {
        this.level = Math.max(-100, Math.min(100, level));
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        if (level == 0) return image;
        
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        
        // Positive level = warmer (more red/yellow, less blue)
        // Negative level = cooler (more blue, less red)
        int rAdjust = (int) (level * 0.5); // Max 50
        int bAdjust = (int) (-level * 0.5); // Max 50

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int cRed = (rgb >> 16) & 0xFF;
                int cGreen = (rgb >> 8) & 0xFF;
                int cBlue = rgb & 0xFF;
                int cAlpha = (rgb >> 24) & 0xFF;
                
                int r = Math.min(255, Math.max(0, cRed + rAdjust));
                int g = cGreen; // Green usually stays similar, or slightly tweaked, but simple R/B works well
                int b = Math.min(255, Math.max(0, cBlue + bAdjust));
                
                result.setRGB(x, y, ((cAlpha << 24) | (r << 16) | (g << 8) | b));
            }
        }
        return result;
    }
}
