package net.kaleidos.comicsmagic;

import java.util.ArrayList;

import net.kaleidos.comicsmagic.adapter.FullScreenImageAdapter;
import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.Window;

public class FullScreenViewActivity extends Activity {

	private Utils utils;
	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;
	SharedPreferences preferences;
	SharedPreferences.Editor editPreferences;
	float middleX;
	float topQuarter;
	OnTouchListener touchListener;

	int fitStyle = AppConstant.FIT_WIDTH;
	ArrayList<String> fileNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullScreen(true);

		setContentView(R.layout.activity_fullscreen_view);
		viewPager = (ViewPager) findViewById(R.id.pager);

		utils = new Utils(getApplicationContext());

		Intent intent = getIntent();
		preferences = getSharedPreferences("comicsMagic", MODE_PRIVATE);
		editPreferences = preferences.edit();
		int number = preferences.getInt("pageNumber", 0);
		fitStyle = preferences.getInt("fitStyle", AppConstant.FIT_WIDTH);

		String fileName = intent.getStringExtra("fileName");

		fileNames = utils.getAllImagesFile(fileName);
		regenerateAdapterPage(number);
		Point size = utils.getScreenSize();
		middleX = size.x / 2;
		topQuarter = size.y / 4;

	}

	private OnTouchListener getTouchListener() {
		if (touchListener == null) {
			touchListener = new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent e) {

					if (!isFullScreen()) {
						setFullScreen(true);
						return true;
					}

					// If the isn't a physical menu button, show the action bar
					// when taping the top of the image
					if (!ViewConfiguration.get(getApplicationContext())
							.hasPermanentMenuKey()) {
						if (e.getY() < topQuarter) {
							setFullScreen(false);
							return true;
						}
					}

					int number = viewPager.getCurrentItem();
					if ((e.getX() < middleX) && (number > 0)) {
						viewPager.setCurrentItem(number - 1, true);
					}
					if ((e.getX() > middleX) && (number < fileNames.size() - 1)) {
						viewPager.setCurrentItem(number + 1, true);
					}
					return true;
				}
			};
		}
		return touchListener;
	}

	private void regenerateAdapterPage(int number) {
		adapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
				fileNames, fitStyle, getTouchListener());

		viewPager.setAdapter(adapter);

		// displaying selected image first
		viewPager.setCurrentItem(number);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.page, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int menuFitStyle = Utils.saveFitPreferenceFromMenu(item,
				editPreferences);
		if (menuFitStyle != -1) {
			fitStyle = menuFitStyle;
			regenerateAdapterPage(viewPager.getCurrentItem());
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Utils.markSelectedItemAsChecked(menu, preferences);
		return true;
	}

	public boolean isFullScreen() {
		return !getActionBar().isShowing();
	}

	public void setFullScreen(boolean full) {
		if (full == isFullScreen()) {
			return;
		}

		Window window = getWindow();
		if (full) {
			// window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getActionBar().hide();
		} else {
			// window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getActionBar().show();
		}
	}

}
