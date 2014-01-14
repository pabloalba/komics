package net.kaleidos.comicsmagic.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

import net.kaleidos.comicsmagic.R;
import net.kaleidos.comicsmagic.components.TouchImageView;
import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FullScreenImageAdapter extends PagerAdapter {

	private final Activity _activity;
	private final ArrayList<String> _imagePaths;
	private LayoutInflater inflater;
	private TouchImageView imgDisplay;
	private int fitStyle = AppConstant.FIT_WIDTH;
	private final OnTouchListener onTouchListener;
	String currentComic;
	Utils utils;

	public int getFitStyle() {
		return fitStyle;
	}

	public void setFitStyle(int fitStyle) {
		this.fitStyle = fitStyle;
		// imgDisplay.setFitStyle(fitStyle);
	}

	// constructor
	public FullScreenImageAdapter(Activity activity, String currentComic,
			ArrayList<String> imagePaths, int fitStyle,
			OnTouchListener onTouchListener) {
		this._activity = activity;
		this._imagePaths = imagePaths;
		this.fitStyle = fitStyle;
		this.currentComic = currentComic;
		this.onTouchListener = onTouchListener;
		utils = new Utils(activity.getApplicationContext());
	}

	@Override
	public int getCount() {
		return this._imagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((LinearLayout) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		inflater = (LayoutInflater) _activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image,
				container, false);
		imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
		imgDisplay.setBackgroundColor(Color.BLACK);
		// imgDisplay.setFitStyle(fitStyle);
		imgDisplay.setMaxZoom(8f);

		imgDisplay.setExternalTouchListener(onTouchListener);
		imgDisplay.setFitStyle(fitStyle);

		imgDisplay.setTag("imgDisplay" + position);
		imgDisplay.setCurrentImagePath("");

		try {

			String fileName = _imagePaths.get(position);
			InputStream in;
			File f = new File(fileName);

			if (!f.exists()) {
				HashSet<String> set = new HashSet<String>();
				set.add(fileName);
				utils.decompressImagesFile(currentComic, set, null);
				f = new File(fileName);
			}
			in = new FileInputStream(f);

			Bitmap bitmap = Utils.readBitmapFromStream(in);
			in.close();
			imgDisplay.setImageBitmap(bitmap);
			imgDisplay.setCurrentImagePath(fileName);
		} catch (Exception e) {
			// Do not show image
		}

		((ViewPager) container).addView(viewLayout);

		return viewLayout;
	}

	public TouchImageView getTouchImageView() {
		return imgDisplay;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((LinearLayout) object);

	}

}