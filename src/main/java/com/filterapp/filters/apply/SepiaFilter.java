package com.filterapp.filters.apply;
import com.filterapp.filters.Filter;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class SepiaFilter implements Filter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int cRed = (rgb >> 16) & 0xFF;
                int cGreen = (rgb >> 8) & 0xFF;
                int cBlue = rgb & 0xFF;
                int cAlpha = (rgb >> 24) & 0xFF;
                int r = cRed;
                int g = cGreen;
                int b = cBlue;

                // Rumus standar matriks warna Sepia
                int tr = (int) (0.393 * r + 0.769 * g + 0.189 * b);
                int tg = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                int tb = (int) (0.272 * r + 0.534 * g + 0.131 * b);

                // Pastikan nilai tidak melebihi 255 (batas maksimum RGB)
                r = Math.min(255, tr);
                g = Math.min(255, tg);
                b = Math.min(255, tb);

                result.setRGB(x, y, (cAlpha << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }
}
