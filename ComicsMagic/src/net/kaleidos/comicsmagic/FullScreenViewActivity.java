package net.kaleidos.comicsmagic;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.kaleidos.comicsmagic.adapter.FullScreenImageAdapter;
import net.kaleidos.comicsmagic.components.TouchImageView;
import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import net.kaleidos.comicsmagic.scenes.Scene;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;

public class FullScreenViewActivity extends Activity {

	private Utils utils;
	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;
	SharedPreferences preferences;
	SharedPreferences.Editor editPreferences;
	float quarterX;
	float topQuarter;
	OnTouchListener touchListener;

	int fitStyle = AppConstant.FIT_WIDTH;
	ArrayList<String> fileNames;

	String pageNumberName;
	ArrayList<Scene> scenes = new ArrayList<Scene>();
	int currentScene = 0;

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

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

		fitStyle = preferences.getInt("fitStyle", AppConstant.FIT_WIDTH);
		String fileName = intent.getStringExtra("fileName");
		String md5Name = intent.getStringExtra("md5Name");
		pageNumberName = "pageNumber_" + md5Name;
		int number = preferences.getInt(pageNumberName, 0);

		fileNames = utils.getAllImagesFile(fileName);
		regenerateAdapterPage(number);
		Point size = utils.getScreenSize();
		quarterX = size.x / 4;
		topQuarter = size.y / 4;

		recalculateScenes();

		getActionBar().setTitle(
				getResources().getString(R.string.app_name) + " ("
						+ (number + 1) + " / " + fileNames.size() + ")");

		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				// Check if this is the page you want.
				editPreferences.putInt(pageNumberName, position);
				editPreferences.commit();
				getActionBar().setTitle(
						getResources().getString(R.string.app_name) + " ("
								+ (viewPager.getCurrentItem() + 1) + " / "
								+ fileNames.size() + ")");
			}
		});

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

					// Show the action bar when taping the top of the image
					if (e.getY() < topQuarter) {
						setFullScreen(false);
						return true;
					} else {
						if (fitStyle != AppConstant.FIT_MAGIC) {
							// Pass page on tap on the left/right borders of the
							// screen
							int number = viewPager.getCurrentItem();
							if ((e.getX() < quarterX) && (number > 0)) {
								viewPager.setCurrentItem(number - 1, true);
							}
							if ((e.getX() > quarterX * 3)
									&& (number < fileNames.size() - 1)) {
								viewPager.setCurrentItem(number + 1, true);
							}
						} else {
							// Magic mode! Pass scene on tap on the left/right
							// borders of the
							// screen
							if ((e.getX() < quarterX) && (currentScene > 0)) {
								currentScene--;
								magic(scenes.get(currentScene).getX(), scenes
										.get(currentScene).getY(),
										scenes.get(currentScene).getZoom());

							}
							if ((e.getX() > quarterX * 3)
									&& (currentScene < scenes.size() - 1)) {
								currentScene++;
								magic(scenes.get(currentScene).getX(), scenes
										.get(currentScene).getY(),
										scenes.get(currentScene).getZoom());
							}
						}
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
		inflater.inflate(R.menu.menu_view_comic, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.select_page) {
			showSelectPageDialog();
		} else {
			int menuFitStyle = Utils.saveFitPreferenceFromMenu(item,
					editPreferences);
			if (menuFitStyle != -1) {
				fitStyle = menuFitStyle;
				regenerateAdapterPage(viewPager.getCurrentItem());
				if (fitStyle == AppConstant.FIT_MAGIC) {
					recalculateScenes();
					currentScene = 0;
					/*
					 * getTouchImageView().zoomToPoint(
					 * scenes.get(currentScene).getX(),
					 * scenes.get(currentScene).getY(),
					 * scenes.get(currentScene).getZoom());
					 */
				}
			}
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

	public void showSelectPageDialog() {
		final Dialog d = new Dialog(FullScreenViewActivity.this,
				android.R.style.Theme_Holo_Dialog_NoActionBar);
		d.setTitle("NumberPicker");
		d.setContentView(R.layout.select_page_dialog);

		Button b1 = (Button) d.findViewById(R.id.button_select_page);
		final NumberPicker np = (NumberPicker) d
				.findViewById(R.id.numberPicker1);
		np.setMaxValue(fileNames.size());
		np.setMinValue(0);
		np.setValue(viewPager.getCurrentItem() + 1);
		np.setWrapSelectorWheel(false);
		// Do not show keyboard
		np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// np.setOnValueChangedListener(this);
		b1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				d.dismiss();
				viewPager.setCurrentItem(np.getValue() - 1, false);
			}
		});
		d.show();

	}

	private void recalculateScenes() {
		// Sample points
		scenes.clear();
		scenes.add(new Scene(100, 400, 5));
		scenes.add(new Scene(300, 400, 4));
		scenes.add(new Scene(500, 400, 5));
		scenes.add(new Scene(100, 500, 2));

	}

	private TouchImageView getTouchImageView() {
		return (TouchImageView) viewPager.findViewWithTag("imgDisplay"
				+ viewPager.getCurrentItem());

	}

	private void magic(final float x, final float y, final float zoom) {
		getTouchImageView().zoomToPoint(x, y, zoom);
	}
}
