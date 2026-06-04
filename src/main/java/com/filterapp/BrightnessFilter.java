package com.filterapp;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BrightnessFilter implements Filter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        
        int brightness = 50; // Tingkat penambahan cahaya

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color c = new Color(image.getRGB(x, y));
                
                // Tambahkan nilai dan pastikan tidak melebihi batas maksimal 255
                int r = Math.min(255, Math.max(0, c.getRed() + brightness));
                int g = Math.min(255, Math.max(0, c.getGreen() + brightness));
                int b = Math.min(255, Math.max(0, c.getBlue() + brightness));
                
                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return result;
    }
}