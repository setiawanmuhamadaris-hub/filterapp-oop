package com.filterapp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    // Memuat gambar dari penyimpanan lokal
    public static BufferedImage loadImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    // Menyimpan gambar hasil filter ke file
    public static void saveImage(BufferedImage image, File file, String format) throws IOException {
        // Memastikan folder output/ direktori penyimpanannya tersedia
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ImageIO.write(image, format, file);
    }
}