package com.filterapp.filters.apply;
import com.filterapp.filters.Filter;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;

public class WatercolorPaintFilter implements Filter {

    // Arahkan ke file yang baru saja kamu unduh
    private final String modelPath = "AnimeGANv2_Hayao.onnx";

    @Override
    public BufferedImage apply(BufferedImage image) {
        try {
            // 1. Inisialisasi Mesin ONNX secara lokal (tanpa internet)
            OrtEnvironment env = OrtEnvironment.getEnvironment();
            OrtSession session = env.createSession(modelPath, new OrtSession.SessionOptions());

            // 2. Persiapan Gambar (Mempertahankan rasio aspek & ukuran kelipatan 32)
            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();

            int maxSize = 512; // Batasi ukuran maksimal agar tidak memakan RAM berlebihan
            int targetWidth = originalWidth;
            int targetHeight = originalHeight;

            if (targetWidth > maxSize || targetHeight > maxSize) {
                if (targetWidth > targetHeight) {
                    targetHeight = (int) ((float) targetHeight / targetWidth * maxSize);
                    targetWidth = maxSize;
                } else {
                    targetWidth = (int) ((float) targetWidth / targetHeight * maxSize);
                    targetHeight = maxSize;
                }
            }

            // Pastikan dimensi kelipatan 32 (syarat banyak model AI)
            targetWidth = targetWidth - (targetWidth % 32);
            targetHeight = targetHeight - (targetHeight % 32);
            if (targetWidth == 0) targetWidth = 32;
            if (targetHeight == 0) targetHeight = 32;

            BufferedImage resizedImage = resizeImage(image, targetWidth, targetHeight);

            // 3. Pemrosesan Tensor (Area Komputasi)
            float[][][][] tensorInput = convertImageToTensor(resizedImage);
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, tensorInput);

            OrtSession.Result result = session.run(Collections.singletonMap("generator_input:0", inputTensor));
            float[][][][] outputData = (float[][][][]) result.get(0).getValue();
            BufferedImage finalImage = convertTensorToImage(outputData);

            // Tutup sesi untuk membebaskan memori RAM
            session.close();
            env.close();

            // Mengembalikan hasil (Resize kembali ke ukuran aslinya agar tidak mengecil/gepeng)
            return resizeImage(finalImage, originalWidth, originalHeight);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal memuat model AI. Pastikan file .onnx ada di folder yang benar.");
            return image;
        }
    }

    // Fungsi utilitas untuk mengubah ukuran gambar (mencegah memory leak)
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }

    private float[][][][] convertImageToTensor(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[][][][] tensor = new float[1][height][width][3];
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                float r = ((rgb >> 16) & 0xFF) / 127.5f - 1.0f;
                float g = ((rgb >> 8) & 0xFF) / 127.5f - 1.0f;
                float b = (rgb & 0xFF) / 127.5f - 1.0f;
                
                tensor[0][y][x][0] = r;
                tensor[0][y][x][1] = g;
                tensor[0][y][x][2] = b;
            }
        }
        return tensor;
    }

    private BufferedImage convertTensorToImage(float[][][][] tensor) {
        int height = tensor[0].length;
        int width = tensor[0][0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float rFloat = (tensor[0][y][x][0] + 1.0f) * 127.5f;
                float gFloat = (tensor[0][y][x][1] + 1.0f) * 127.5f;
                float bFloat = (tensor[0][y][x][2] + 1.0f) * 127.5f;
                
                int r = Math.min(255, Math.max(0, Math.round(rFloat)));
                int g = Math.min(255, Math.max(0, Math.round(gFloat)));
                int b = Math.min(255, Math.max(0, Math.round(bFloat)));
                
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }
}
