package net.kaleidos.comicsmagic.components;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class ImageViewPager extends ViewPager {
	private boolean avoidScroll = false;


	public void setAvoidScroll(boolean avoidScroll) {
		this.avoidScroll = avoidScroll;
	}

	public ImageViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageViewPager(Context context) {
		super(context);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (avoidScroll){
			return false;
		}
		if (v instanceof TouchImageView) {
			TouchImageView imageView = (TouchImageView) v;
			return imageView.avoidTurnPage(dx);
		}
		return super.canScroll(v, checkV, dx, x, y);
	}
}
