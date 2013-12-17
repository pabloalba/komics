package net.kaleidos.comicsmagic.components;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class ImageViewPager extends ViewPager {

	public ImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageViewPager(Context context) {
		super(context);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof TouchImageView) {
			TouchImageView imageView = (TouchImageView) v;
			return imageView.avoidTurnPage(dx);
		}
		return super.canScroll(v, checkV, dx, x, y);
	}
}
