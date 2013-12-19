package net.kaleidos.comicsmagic.edgedetector;

import java.io.IOException;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.scenes.Scene;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class EdgeDetector {

	private static int BLACK_RED = 0;
	private static int BLACK_GREEN = 0;
	private static int BLACK_BLUE = 0;

	private static int WHITE_RED = 255;
	private static int WHITE_GREEN = 255;
	private static int WHITE_BLUE = 255;

	private static int RANGE = 15;



	private static boolean isBorderColor(int rgb){

		int red = Color.red(rgb);
		int green = Color.green(rgb);
		int blue = Color.blue(rgb);


		boolean black = (
				( ( (BLACK_RED-RANGE) < red ) && ( red < (BLACK_RED+RANGE) ) ) &&
				( ( (BLACK_GREEN-RANGE) < green ) && ( green < (BLACK_GREEN+RANGE) ) ) &&
				( ( (BLACK_BLUE-RANGE) < blue ) && ( blue < (BLACK_BLUE+RANGE) ) )
				);

		boolean white = (
				( ( (WHITE_RED-RANGE) < red ) && ( red < (WHITE_RED+RANGE) ) ) &&
				( ( (WHITE_GREEN-RANGE) < green ) && ( green < (WHITE_GREEN+RANGE) ) ) &&
				( ( (WHITE_BLUE-RANGE) < blue ) && ( blue < (WHITE_BLUE+RANGE) ) )
				);

		return black || white;

	}


	public static ArrayList<Scene> processImage(Drawable drawable) throws IOException{
		ArrayList<Scene> scenes = new ArrayList<Scene>();


		Bitmap img = ((BitmapDrawable)drawable).getBitmap();

		int width = img.getWidth();
		int height = img.getHeight();

		ArrayList<Integer> horizontalLines = new ArrayList<Integer>();
		ArrayList zones = new ArrayList();

		int tenPercentHeight = height/10;
		int tenPercentWidth = width/10;

		//Check horizontal lines
		horizontalLines.add(0);
		for (int y=tenPercentHeight; y<height-tenPercentHeight;y++){
			boolean blackLine = true;
			for (int x = 0; x < width; x=x+10) {
				if (!isBorderColor(img.getPixel(x, y))){
					blackLine = false;
					break;
				}
			}
			if (blackLine){
				horizontalLines.add(y);
				//Jump 10%
				y+=tenPercentHeight;
			}

		}

		horizontalLines.add(height-5);



		//Check vertical lines
		for (int i = 0; i< horizontalLines.size()-1; i++){
			ArrayList<Integer> zoneVerticalLines = new ArrayList<Integer>();
			zones.add(zoneVerticalLines);
			zoneVerticalLines.add(0);

			for (int x = tenPercentWidth; x < width-tenPercentWidth; x++) {
				boolean blackLine = true;
				for (int y = horizontalLines.get(i); y<horizontalLines.get(i+1);y++){
					if (!isBorderColor(img.getPixel(x, y))){
						blackLine = false;
						break;
					}
				}
				if (blackLine){
					zoneVerticalLines.add(x);
					//Jump 10%
					x+=tenPercentWidth;
				}
			}
			zoneVerticalLines.add(width-1);
		}


		for (int i = 0; i < zones.size(); i++) {
			ArrayList<Integer> zoneLines = (ArrayList<Integer>) zones.get(i);
			int y1 = horizontalLines.get(i);
			int y2 = height;
			if (i<horizontalLines.size()-1){
				y2 = horizontalLines.get(i+1);
			}
			for (int j=0; j< zoneLines.size()-1;j++){
				//center
				int x1 = zoneLines.get(j);
				int x2 = zoneLines.get(j+1);

				int x = (x1+x2) /2;
				int y = (y1+y2) /2;

				float deltaX = (x2 - x1);
				float deltaY = (y2 - y1);

				float scaleX = deltaX / width;
				float scaleY = deltaY / height;

				float zoom = 1 / Math.max(scaleX, scaleY);
				scenes.add(new Scene(x, y, zoom));
			}

		}

		return scenes;
	}

}
