package com.filterapp;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class VignetteFilter implements Filter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        int centerX = width / 2;
        int centerY = height / 2;
        double maxDistance = Math.sqrt(centerX * centerX + centerY * centerY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color(image.getRGB(x, y));
                
                // Hitung jarak piksel ini ke titik tengah
                double distance = Math.sqrt(Math.pow(centerX - x, 2) + Math.pow(centerY - y, 2));
                
                // Hitung intensitas gelap (semakin jauh = semakin mendekati 0.0)
                double factor = 1.0 - (distance / maxDistance);
                factor = Math.pow(factor, 0.6); // Sedikit dilengkungkan agar halus

                int r = Math.min(255, Math.max(0, (int) (c.getRed() * factor)));
                int g = Math.min(255, Math.max(0, (int) (c.getGreen() * factor)));
                int b = Math.min(255, Math.max(0, (int) (c.getBlue() * factor)));
                
                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return result;
    }
}