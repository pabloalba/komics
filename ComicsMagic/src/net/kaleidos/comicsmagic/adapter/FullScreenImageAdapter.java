package net.kaleidos.comicsmagic.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.FullScreenViewActivity;
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
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FullScreenImageAdapter extends PagerAdapter {

	private final Activity _activity;
	private final ArrayList<String> _imagePaths;
	private LayoutInflater inflater;
	private TouchImageView imgDisplay;
	private int fitStyle = AppConstant.FIT_WIDTH;

	public int getFitStyle() {
		return fitStyle;
	}

	public void setFitStyle(int fitStyle) {
		this.fitStyle = fitStyle;
		//imgDisplay.setFitStyle(fitStyle);
	}

	// constructor
	public FullScreenImageAdapter(Activity activity,
			ArrayList<String> imagePaths, int fitStyle) {
		this._activity = activity;
		this._imagePaths = imagePaths;
		this.fitStyle = fitStyle;
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
		View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
				false);
		imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
		imgDisplay.setBackgroundColor(Color.BLACK);
		//imgDisplay.setFitStyle(fitStyle);
		imgDisplay.setMaxZoom(8f);
		imgDisplay.setFullScreenViewActivity ((FullScreenViewActivity) _activity);

		try {
			File f = new File(_imagePaths.get(position));
			InputStream in = new FileInputStream(f);
			Bitmap bitmap = Utils.readBitmapFromStream(in);
			in.close();
			imgDisplay.setImageBitmap(bitmap);
		} catch(Exception e){
			//Do not show image
		}


		((ViewPager) container).addView(viewLayout);

		return viewLayout;
	}

	public TouchImageView getTouchImageView(){
		return imgDisplay;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((LinearLayout) object);

	}
}