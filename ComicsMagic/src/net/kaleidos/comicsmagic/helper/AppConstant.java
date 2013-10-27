package net.kaleidos.comicsmagic.helper;

import java.util.Arrays;
import java.util.List;

public class AppConstant {
	// Number of columns of Grid View
    public static final int NUM_OF_COLUMNS = 3;
 
    // Gridview image padding
    public static final int GRID_PADDING = 8; // in dp
 
    // SD card image directory
    public static final String PHOTO_ALBUM = "komics";
 
    // supported file formats
    public static final List<String> COMIC_EXTN = Arrays.asList("cbz", "zip");
 // supported file formats
    public static final List<String> IMAGE_EXTN = Arrays.asList("jpg", "jpeg",
            "png");
}
