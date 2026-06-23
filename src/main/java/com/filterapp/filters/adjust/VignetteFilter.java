package com.filterapp.filters.adjust;
import com.filterapp.filters.Filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class VignetteFilter implements Filter {

    private int level;

    public VignetteFilter(int level) {
        this.level = level;
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        if (level == 0) return image;
        
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        int centerX = width / 2;
        int centerY = height / 2;
        double maxDistance = Math.sqrt(centerX * centerX + centerY * centerY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int cRed = (rgb >> 16) & 0xFF;
                int cGreen = (rgb >> 8) & 0xFF;
                int cBlue = rgb & 0xFF;
                int cAlpha = (rgb >> 24) & 0xFF;
                
                // Hitung jarak piksel ini ke titik tengah
                double distance = Math.sqrt(Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2));
                
                // Hitung intensitas gelap
                double factor = 1.0 - (distance / maxDistance);
                factor = Math.pow(factor, 0.6); // Sedikit dilengkungkan agar halus
                
                // Campur faktor berdasarkan level slider (0-100)
                double strength = level / 100.0;
                factor = 1.0 - ((1.0 - factor) * strength);

                int r = Math.min(255, Math.max(0, (int) (cRed * factor)));
                int g = Math.min(255, Math.max(0, (int) (cGreen * factor)));
                int b = Math.min(255, Math.max(0, (int) (cBlue * factor)));
                
                result.setRGB(x, y, ((cAlpha << 24) | (r << 16) | (g << 8) | b));
            }
        }
        return result;
    }
}
