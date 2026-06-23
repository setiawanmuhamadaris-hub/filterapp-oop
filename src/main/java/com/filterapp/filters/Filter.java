package com.filterapp.filters;

import java.awt.image.BufferedImage;

/**
 * Interface dasar untuk semua jenis filter foto.
 */
public interface Filter {
    BufferedImage apply(BufferedImage image);
}
