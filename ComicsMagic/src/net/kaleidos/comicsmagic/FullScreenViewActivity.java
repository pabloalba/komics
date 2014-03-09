package net.kaleidos.comicsmagic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.kaleidos.comicsmagic.adapter.FullScreenImageAdapter;
import net.kaleidos.comicsmagic.components.ImageViewPager;
import net.kaleidos.comicsmagic.components.TouchImageView;
import net.kaleidos.comicsmagic.edgedetector.EdgeDetector;
import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import net.kaleidos.comicsmagic.listener.LoadImageListener;
import net.kaleidos.comicsmagic.scenes.Scene;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;

public class FullScreenViewActivity extends Activity implements
		LoadImageListener {

	private Utils utils;
	private FullScreenImageAdapter adapter;
	private ImageViewPager viewPager;
	SharedPreferences preferences;
	SharedPreferences.Editor editPreferences;
	float quarterX;
	OnTouchListener touchListener;
	String currentComic;
	String md5Name;

	int fitStyle = AppConstant.FIT_WIDTH;
	ArrayList<String> fileNames;

	String pageNumberName;
	ArrayList<Scene> scenes = new ArrayList<Scene>();
	int currentScene = 0;
	boolean goForward = true;

	final Handler mHandler = new Handler();
	// Create runnable for posting
	final Runnable regenerateAdapterPageRunnable = new Runnable() {
		@Override
		public void run() {
			regenerateAdapterPage(viewPager.getCurrentItem());
		}
	};

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFullScreen(true);
		setContentView(R.layout.activity_fullscreen_view);
		viewPager = (ImageViewPager) findViewById(R.id.pager);

		utils = new Utils(getApplicationContext());

		Intent intent = getIntent();
		preferences = getSharedPreferences("comicsMagic", MODE_PRIVATE);
		editPreferences = preferences.edit();

		fitStyle = preferences.getInt("fitStyle", AppConstant.FIT_WIDTH);
		currentComic = intent.getStringExtra("fileName");
		md5Name = intent.getStringExtra("md5Name");
		pageNumberName = "pageNumber_" + md5Name;
		int number = preferences.getInt(pageNumberName, 0);

		fileNames = utils.getAllImagesNamesFromFile(currentComic);
		regenerateAdapterPage(number);
		Point size = utils.getScreenSize();
		quarterX = size.x / 4;

		checkMagicMode();

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
				editPreferences.putInt(pageNumberName, position);
				editPreferences.commit();
				checkLastPage(position);
				getActionBar().setTitle(
						getResources().getString(R.string.app_name) + " ("
								+ (viewPager.getCurrentItem() + 1) + " / "
								+ fileNames.size() + ")");

				if (fitStyle == AppConstant.FIT_MAGIC) {
					checkMagicMode();
				}

			}
		});
		if (!preferences.getBoolean("coachShowed", false)) {
			onCoachMark();
			editPreferences.putBoolean("coachShowed", true);
			editPreferences.commit();
		}

		new UncompressComic().execute();

	}

	public void onCoachMark() {

		final Dialog dialog = new Dialog(this) {
			@Override
			public boolean onTouchEvent(MotionEvent event) {
				// Tap anywhere to close dialog.
				this.dismiss();
				return true;
			}
		};

		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(R.color.semitransparentblack));
		if (fitStyle == AppConstant.FIT_MAGIC) {
			dialog.setContentView(R.layout.coach_mark_magic);
		} else {
			dialog.setContentView(R.layout.coach_mark);
		}
		dialog.setCanceledOnTouchOutside(true);

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(dialog.getWindow().getAttributes());
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;

		dialog.show();
		dialog.getWindow().setAttributes(lp);
	}

	private void checkMagicMode() {
		if (fitStyle == AppConstant.FIT_MAGIC) {
			if ((getTouchImageView() != null)
					&& (getTouchImageView().getMatchViewWidth() > 0)) {

				if (!preferences.getBoolean("coachShowedMagic", false)) {
					FullScreenViewActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onCoachMark();
						}
					});

					editPreferences.putBoolean("coachShowedMagic", true);
					editPreferences.commit();
				}

				viewPager.setAvoidScroll(true);
				recalculateScenes();
				if (goForward) {
					currentScene = 0;
				} else {
					currentScene = scenes.size() - 1;
				}

				moveToCurrentScene();

			} else {
				scheduler.schedule(new Runnable() {
					@Override
					public void run() {
						checkMagicMode();
					}
				}, 2000, TimeUnit.MILLISECONDS);
			}
		} else {
			viewPager.setAvoidScroll(false);
		}
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

					int number = viewPager.getCurrentItem();

					if (e.getX() < quarterX) {
						if ((fitStyle == AppConstant.FIT_MAGIC)
								&& (currentScene > 0)) {
							currentScene--;
							moveToCurrentScene();
							return true;
						}

						if (number > 0) {
							goForward = false;
							viewPager.setCurrentItem(number - 1, true);
						}

					} else if (e.getX() > quarterX * 3) {
						if ((fitStyle == AppConstant.FIT_MAGIC)
								&& (currentScene < scenes.size() - 1)) {
							currentScene++;
							moveToCurrentScene();
							return true;
						}

						if (number < fileNames.size() - 1) {
							goForward = true;
							viewPager.setCurrentItem(number + 1, true);
						}
					} else {
						setFullScreen(false);
					}
					return true;
				}
			};
		}
		return touchListener;
	}

	private void checkLastPage(int number) {
		if (number == fileNames.size() - 1) {
			editPreferences.putBoolean("readed_" + md5Name, true);
			editPreferences.commit();
		}
	}

	private void regenerateAdapterPage(int number) {
		adapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
				currentComic, fileNames, fitStyle, getTouchListener());

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
		} else if (item.getItemId() == R.id.show_coach) {
			onCoachMark();
		} else {
			int menuFitStyle = Utils.saveFitPreferenceFromMenu(item,
					editPreferences);
			if (menuFitStyle != -1) {
				setFullScreen(true);
				fitStyle = menuFitStyle;
				regenerateAdapterPage(viewPager.getCurrentItem());
				checkMagicMode();
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

	private void moveToCurrentScene() {
		if ((getTouchImageView() != null)
				&& (getTouchImageView().getMatchViewWidth() > 0)) {
			getTouchImageView().zoomToPoint(scenes.get(currentScene).getX(),
					scenes.get(currentScene).getY(),
					scenes.get(currentScene).getZoom());
		} else {
			scheduler.schedule(new Runnable() {
				@Override
				public void run() {
					moveToCurrentScene();
				}
			}, 2000, TimeUnit.MILLISECONDS);
		}
	}

	public boolean isFullScreen() {
		return !getActionBar().isShowing();
	}

	public void setFullScreen(boolean full) {
		if (full == isFullScreen()) {
			return;
		}

		if (full) {
			getActionBar().hide();
		} else {
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
		np.setMinValue(1);
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
		Drawable drawable = getTouchImageView().getDrawable();

		try {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int width = size.x;
			int height = size.y;
			scenes = EdgeDetector.processImage(drawable, width, height);
		} catch (IOException e) {
			// Only one big scene
			scenes.add(new Scene(getTouchImageView().getMatchViewWidth() / 2,
					getTouchImageView().getMatchViewHeight() / 2, 1));
		}

	}

	private TouchImageView getTouchImageView() {
		return (TouchImageView) viewPager.findViewWithTag("imgDisplay"
				+ viewPager.getCurrentItem());
	}

	/********************************************************
	 * Sound buttons stuff
	 ********************************************************/

	public void changeScreenBrightness(float brightnessModifier) {
		WindowManager.LayoutParams layout = getWindow().getAttributes();
		layout.screenBrightness += brightnessModifier;
		layout.screenBrightness = Math.max(layout.screenBrightness, 0F);
		layout.screenBrightness = Math.min(layout.screenBrightness, 1F);

		getWindow().setAttributes(layout);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			event.startTracking();
			changeScreenBrightness(0.1F);
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			event.startTracking();
			changeScreenBrightness(-0.1F);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			event.startTracking();
			changeScreenBrightness(0.1F);
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			event.startTracking();
			changeScreenBrightness(-0.1F);
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ((event.getFlags() & KeyEvent.FLAG_CANCELED_LONG_PRESS) == 0) {
			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				event.startTracking();
				changeScreenBrightness(0.1F);
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
				event.startTracking();
				changeScreenBrightness(-0.1F);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private class UncompressComic extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			/*
			 * HashSet<String> set = new HashSet<String>(); for (int i = 5; i <
			 * fileNames.size(); i++) { set.add(fileNames.get(i)); }
			 * 
			 * utils.decompressImagesFile(currentComic, set,
			 * FullScreenViewActivity.this);
			 */
			return null;
		}
	}

	@Override
	public void onLoadImage(String fileName) {
		// If we was showing an image not uncompresed
		if ((getTouchImageView() != null)
				&& (getTouchImageView().getCurrentImagePath().equals(fileName))) {
			// Reload current image by regenerating adapter
			mHandler.post(regenerateAdapterPageRunnable);
		}
	}

}
