package net.kaleidos.comicsmagic;

import java.util.ArrayList;
import java.util.Collections;

import net.kaleidos.comicsmagic.components.TouchImageView;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class PageActivity extends Activity {
	ArrayList<String> fileNames;
	TouchImageView touchImageView;
	int number = 0;
	Utils utils;
	ProgressDialog progressDialog;
	SharedPreferences preferences;
	SharedPreferences.Editor editPreferences;
	float middleX;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		touchImageView = new TouchImageView(this);
		touchImageView.setMaxZoom(8f);
		setContentView(touchImageView);
		utils = new Utils(this);
		preferences = getSharedPreferences("comicsMagic", MODE_PRIVATE);
		editPreferences = preferences.edit();	
		number = preferences.getInt("pageNumber", 0);
		middleX = utils.getScreenWidth() / 2;

		new LoadComic().execute();

		touchImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {				
				int newNumber = number;
				if ((touchImageView.getLast().x<middleX) && (number>0)) {
					newNumber--;
				} 
				if ((touchImageView.getLast().x>middleX) && (number<fileNames.size())) {
					newNumber++;
				}
					
				if (newNumber != number) {
					number = newNumber;
					editPreferences.putInt("pageNumber", number);				
					editPreferences.commit();
					showPage(number);
				}
			}
		});

	}

	private void showPage(int number) {
		Bitmap bmImg = BitmapFactory.decodeFile(fileNames.get(number));
		touchImageView.setImageBitmap(bmImg);		
		touchImageView.fitToWidth();
	}

	private class LoadComic extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			Intent i = getIntent();
			String fileName = i.getStringExtra("file");
			fileNames = utils.getAllImagesFile(fileName);
			Collections.sort(fileNames);
			return null;
		}

		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(PageActivity.this, "",
					"Loading comic", true);
		}

		protected void onPostExecute(Object result) {
			showPage(number);
			progressDialog.dismiss();
		}

	}

}
