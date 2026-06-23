package com.filterapp.filters.apply;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.filterapp.filters.Filter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Collections;

public class BWPortraitFilter implements Filter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        try {
            OrtEnvironment env = OrtEnvironment.getEnvironment();
            // Gunakan classloader agar bisa dipanggil jika nanti dibundel ke jar (atau taruh langsung onnx di resources)
            // Karena sekarang file onnx ada di root, kita muat dari path file system.
            OrtSession session = env.createSession("u2net_portrait.onnx", new OrtSession.SessionOptions());

            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();

            int maxSize = 512; 
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

            // Dimensi harus kelipatan 32 untuk arsitektur U2-Net
            targetWidth = targetWidth - (targetWidth % 32);
            targetHeight = targetHeight - (targetHeight % 32);
            if (targetWidth == 0) targetWidth = 32;
            if (targetHeight == 0) targetHeight = 32;

            BufferedImage resizedImage = resizeImage(image, targetWidth, targetHeight);

            float[][][][] tensorInput = convertImageToTensor(resizedImage);
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, tensorInput);

            // Nama input untuk u2net biasanya "input.1" atau "input"
            String inputName = session.getInputNames().iterator().next();
            OrtSession.Result result = session.run(Collections.singletonMap(inputName, inputTensor));

            // Output berbentuk probabilitas map [1, 1, H, W]
            float[][][][] tensorOutput = (float[][][][]) result.get(0).getValue();
            
            BufferedImage maskImage = createMaskImage(tensorOutput, targetWidth, targetHeight);

            // Resize kembali mask ke resolusi asli
            maskImage = resizeImage(maskImage, originalWidth, originalHeight);

            result.close();
            inputTensor.close();
            session.close();
            env.close();

            return maskImage;

        } catch (Exception e) {
            e.printStackTrace();
            return image;
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private float[][][][] convertImageToTensor(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float[][][][] tensor = new float[1][3][height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                float r_norm = ((rgb >> 16) & 0xFF) / 255.0f;
                float g_norm = ((rgb >> 8) & 0xFF) / 255.0f;
                float b_norm = (rgb & 0xFF) / 255.0f;
                
                // Normalisasi ImageNet (Wajib untuk model berbasis ResNet/VGG seperti U2-Net)
                float r = (r_norm - 0.485f) / 0.229f;
                float g = (g_norm - 0.456f) / 0.224f;
                float b = (b_norm - 0.406f) / 0.225f;
                
                tensor[0][0][y][x] = r;
                tensor[0][1][y][x] = g;
                tensor[0][2][y][x] = b;
            }
        }
        return tensor;
    }

    private BufferedImage createMaskImage(float[][][][] tensor, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float val = tensor[0][0][y][x]; 
                // U2-Net Portrait sebenarnya didesain untuk mengeluarkan gradasi pensil yang lembut (bukan hitam-putih mutlak).
                // Dengan menghapus "Threshold/Ambang Batas" paksa, kita mengizinkan AI menggambar bayangan dan tekstur tipisnya.
                
                // Sedikit penipisan garis (Gamma Correction) agar hasil pensilnya lebih bersih
                val = (float) Math.pow(val, 1.5);
                
                // Balik warnanya: 1.0 (tinta AI) menjadi 0 (hitam), 0.0 (latar AI) menjadi 255 (putih kertas)
                int gray = Math.min(255, Math.max(0, Math.round((1.0f - val) * 255.0f)));
                
                int rgb = (gray << 16) | (gray << 8) | gray;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }
}
