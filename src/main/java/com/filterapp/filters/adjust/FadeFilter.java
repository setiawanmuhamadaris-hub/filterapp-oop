package com.filterapp.filters.adjust;
import com.filterapp.filters.Filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class FadeFilter implements Filter {

    private int level; // 0 to 100

    public FadeFilter(int level) {
        this.level = Math.max(-100, Math.min(100, level));
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        if (level == 0) return image;
        
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        
        // At 100 level, we want max fade effect. E.g. black becomes 80, white becomes 230
        int blackPoint = (int) (80 * (level / 100.0));
        int whitePoint = (int) (255 - 25 * (level / 100.0));
        double factor = (whitePoint - blackPoint) / 255.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int cRed = (rgb >> 16) & 0xFF;
                int cGreen = (rgb >> 8) & 0xFF;
                int cBlue = rgb & 0xFF;
                int cAlpha = (rgb >> 24) & 0xFF;
                
                int r = Math.min(255, Math.max(0, (int) (cRed * factor) + blackPoint));
                int g = Math.min(255, Math.max(0, (int) (cGreen * factor) + blackPoint));
                int b = Math.min(255, Math.max(0, (int) (cBlue * factor) + blackPoint));
                
                result.setRGB(x, y, ((cAlpha << 24) | (r << 16) | (g << 8) | b));
            }
        }
        return result;
    }
}
