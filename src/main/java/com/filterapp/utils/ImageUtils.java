package com.filterapp.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    // Memuat gambar dari penyimpanan lokal
    public static BufferedImage loadImage(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        if (img == null) return null;
        
        // Konversi ke tipe standar (TYPE_INT_ARGB) untuk menghindari error TYPE_CUSTOM (0) saat menyimpan atau memproses gambar resolusi penuh
        BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = convertedImg.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        
        return convertedImg;
    }

    // Menyimpan gambar hasil filter ke file
    public static void saveImage(BufferedImage image, File file, String format) throws IOException {
        // Memastikan folder output/ direktori penyimpanannya tersedia
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ImageIO.write(image, format, file);
    }

    // Resize gambar untuk keperluan preview agar performa UI cepat
    public static BufferedImage resize(BufferedImage original, int maxDim) {
        if (original == null) return null;
        int w = original.getWidth();
        int h = original.getHeight();
        if (w <= maxDim && h <= maxDim) return original;
        
        double scale = Math.min((double) maxDim / w, (double) maxDim / h);
        int newW = Math.max(1, (int) (w * scale));
        int newH = Math.max(1, (int) (h * scale));
        
        int type = original.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : original.getType();
        BufferedImage resized = new BufferedImage(newW, newH, type);
        java.awt.Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newW, newH, null);
        g2d.dispose();
        
        return resized;
    }
}
