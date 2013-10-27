package net.kaleidos.comicsmagic.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.R;
import net.kaleidos.comicsmagic.helper.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ComicAdapter extends BaseAdapter {
	private Context context;
	private final ArrayList<File> files;
	private Utils utils;
 
	public ComicAdapter(Context context, ArrayList<File> files, Utils utils) {
		this.context = context;
		this.files = files;
		this.utils = utils;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) {
 
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View gridView;
 
		if (convertView == null) {
			File file = files.get(position);
 
			gridView = new View(context);
 
			// get layout from mobile.xml
			gridView = inflater.inflate(R.layout.comic_file, null);
 
			// set value into textview
			TextView textView = (TextView) gridView
					.findViewById(R.id.grid_item_label);
			textView.setText(file.getName());
 
			// set image based on selected text
			ImageView imageView = (ImageView) gridView
					.findViewById(R.id.grid_item_image);
 
			imageView.setImageResource(R.drawable.comic);
			
			// get screen dimensions
			File f = utils.getFirstImageFile(file);
			if (f != null) {
				Log.e("2 " + f.getAbsolutePath(), "unzip", null);				
				Bitmap bmImg = BitmapFactory.decodeFile(f.getAbsolutePath());
		        imageView.setImageBitmap(bmImg);
			} else {
				imageView.setImageResource(R.drawable.comic);
			}
			

		} else {
			gridView = (View) convertView;
		}
 
		return gridView;
	}
	
	 /*
     * Resizing image size
     */
    public static Bitmap decodeFile(File f, int WIDTH, int HIGHT) {
        try { 
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
 
            final int REQUIRED_WIDTH = WIDTH;
            final int REQUIRED_HIGHT = HIGHT;
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_WIDTH
                    && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
                scale *= 2;
 
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
 
	@Override
	public int getCount() {
		return files.size();
	}
 
	@Override
	public Object getItem(int position) {
		return null;
	}
 
	@Override
	public long getItemId(int position) {
		return 0;
	}
 
}
