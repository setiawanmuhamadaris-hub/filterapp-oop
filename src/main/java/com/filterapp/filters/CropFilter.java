package com.filterapp.filters;

import java.awt.image.BufferedImage;

public class CropFilter implements Filter {
    private double ratio;

    private double panX;
    private double panY;
    private double zoom;

    public CropFilter(double ratio, double panX, double panY, double zoom) {
        this.ratio = ratio;
        this.panX = panX;
        this.panY = panY;
        this.zoom = Math.max(1.0, zoom);
    }

    @Override
    public BufferedImage apply(BufferedImage image) {
        if (ratio <= 0) return image; // 0 berarti original ratio
        
        int origW = image.getWidth();
        int origH = image.getHeight();
        
        double origRatio = (double) origW / origH;
        int targetW = origW;
        int targetH = origH;
        
        if (origRatio > ratio) {
            // Gambar asli lebih lebar dari rasio target
            targetW = (int) (origH * ratio);
        } else {
            // Gambar asli lebih tinggi dari rasio target
            targetH = (int) (origW / ratio);
        }
        
        // Apply zoom (zoom >= 1.0 means crop area becomes smaller)
        targetW = (int) (targetW / zoom);
        targetH = (int) (targetH / zoom);
        
        int maxOffsetX = origW - targetW;
        int maxOffsetY = origH - targetH;
        
        int x = (int) (maxOffsetX * panX);
        int y = (int) (maxOffsetY * panY);
        
        x = Math.max(0, Math.min(x, maxOffsetX));
        y = Math.max(0, Math.min(y, maxOffsetY));
        
        BufferedImage subImg = image.getSubimage(x, y, targetW, targetH);
        
        // Buat salinan baru untuk menghindari bug SwingFXUtils.toFXImage dengan getSubimage
        BufferedImage cropped = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = cropped.createGraphics();
        g.drawImage(subImg, 0, 0, null);
        g.dispose();
        
        return cropped;
    }
}
