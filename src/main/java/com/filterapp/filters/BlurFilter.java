package com.filterapp.filters;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class BlurFilter implements Filter {

    @Override
    public BufferedImage apply(BufferedImage image) {
        float weight = 1.0f / 9.0f;
        float[] elements = new float[9];
        for (int i = 0; i < 9; i++) {
            elements[i] = weight;
        }

        Kernel kernel = new Kernel(3, 3, elements);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        return op.filter(image, result);
    }
}
