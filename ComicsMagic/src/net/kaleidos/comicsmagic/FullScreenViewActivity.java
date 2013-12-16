package net.kaleidos.comicsmagic;

import java.util.ArrayList;

import net.kaleidos.comicsmagic.adapter.FullScreenImageAdapter;
import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

public class FullScreenViewActivity extends Activity{

	private Utils utils;
	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;
	SharedPreferences preferences;
	SharedPreferences.Editor editPreferences;
	float middleX;

	int fitStyle = AppConstant.FIT_WIDTH;
	ArrayList<String> fileNames;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		middleX = utils.getScreenWidth() / 2;


	}

	private void regenerateAdapterPage(int number){
		adapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
				fileNames, fitStyle);

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
		item.setChecked(true);
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.fit_width:
			fitStyle = AppConstant.FIT_WIDTH;
			break;
		case R.id.fit_height:
			fitStyle = AppConstant.FIT_HEIGHT;
			break;
		case R.id.fit_image:
			fitStyle = AppConstant.FIT_IMAGE;
			break;
		case R.id.fit_magic:
			fitStyle = AppConstant.FIT_MAGIC;
		}
		editPreferences.putInt("fitStyle", fitStyle);
		editPreferences.commit();

		regenerateAdapterPage(viewPager.getCurrentItem());
		return true;
	}


	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		int fitStyle = preferences.getInt("fitStyle", AppConstant.FIT_WIDTH);
		if (super.onMenuOpened(featureId, menu)){
			int id = -1;
			switch (fitStyle) {
			case AppConstant.FIT_WIDTH:
				id = R.id.fit_width;
				break;
			case AppConstant.FIT_HEIGHT:
				id = R.id.fit_height;
				break;
			case AppConstant.FIT_IMAGE:
				id = R.id.fit_image;
				break;
			case AppConstant.FIT_MAGIC:
				id = R.id.fit_magic;
				break;
			}

			if (id != -1) {
				menu.findItem(id).setChecked(true);
			}
			return true;
		}
		return false;
	}

	public void onClick(MotionEvent e){

		int number = viewPager.getCurrentItem();

		if ((e.getX()<middleX) && (number>0)) {
			viewPager.setCurrentItem(number - 1);
		}
		if ((e.getX()>middleX) && (number<fileNames.size()-1)) {
			viewPager.setCurrentItem(number + 1);
		}

	}

}
