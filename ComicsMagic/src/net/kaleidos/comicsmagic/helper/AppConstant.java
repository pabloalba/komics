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
    public static final List<String> COMIC_EXTN = Arrays.asList("cbz", "zip", "cbr", "rar");
    public static final List<String> COMIC_EXTN_ZIP = Arrays.asList("cbz", "zip");
    public static final List<String> COMIC_EXTN_RAR = Arrays.asList("cbr", "rar");
 // supported file formats
    public static final List<String> IMAGE_EXTN = Arrays.asList("jpg", "jpeg",
            "png");
    
    public static final int FIT_WIDTH = 0;
    public static final int FIT_HEIGHT = 1;
    public static final int FIT_IMAGE = 2;
    public static final int FIT_NONE = 3;
    public static final int FIT_MAGIC = 4;
}
