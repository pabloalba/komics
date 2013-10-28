package net.kaleidos.comicsmagic;

import java.util.ArrayList;
import java.util.Collections;

import net.kaleidos.comicsmagic.components.TouchImageView;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class PageActivity extends Activity {
	ArrayList<String> fileNames;
	TouchImageView touchImageView;
	int number = 0;
	Utils utils;
	ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		touchImageView = new TouchImageView(this);
		setContentView(touchImageView);
		utils = new Utils(this);
		
		
		 new LoadComic().execute();
		

		/*
		 * imageView.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { number++;
		 * showPage(number); } });
		 */

	}

	

	private void showPage(int number) {
		Bitmap bmImg = BitmapFactory.decodeFile(fileNames.get(number));
		touchImageView.setImageBitmap(bmImg);
		touchImageView.setMaxZoom(4f);
	}
	
	private class LoadComic extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			Log.e("LoadComic", "1");
			Intent i = getIntent();
			String fileName = i.getStringExtra("file");
			fileNames = utils.getAllImagesFile(fileName);
			Collections.sort(fileNames);
			
			Log.e("LoadComic", "2 - "+fileNames.size());
			return null;
		}
		
		protected void 	onPreExecute(){
			Log.e("LoadComic", "0");
			progressDialog = ProgressDialog.show(PageActivity.this, "", "Loading comic", true);
		}
		
		protected void onPostExecute(Object result){
			Log.e("LoadComic", "3");
			showPage(number);
			progressDialog.dismiss();
		}
		
		
	
	}

}
