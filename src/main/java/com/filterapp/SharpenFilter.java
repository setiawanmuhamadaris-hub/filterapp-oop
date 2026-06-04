package com.filterapp;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class SharpenFilter implements Filter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        // Matriks (kernel) khusus untuk efek Sharpen
        float[] elements = {
             0.0f, -1.0f,  0.0f,
            -1.0f,  5.0f, -1.0f,
             0.0f, -1.0f,  0.0f
        };

        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        return op.filter(image, result);
    }
}