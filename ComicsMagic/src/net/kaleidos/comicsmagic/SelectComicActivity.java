package net.kaleidos.comicsmagic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import net.kaleidos.comicsmagic.adapter.ComicAdapter;
import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class SelectComicActivity extends Activity {

	GridView gridView;
	Utils utils;
	ArrayList<File> files = null;
	File currentComic;
	ProgressDialog progressDialog;
	SharedPreferences preferences;
	SharedPreferences.Editor editPreferences;
	File currentDirectory;
	ArrayList<String> fileNames;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_comic);

		gridView = (GridView) findViewById(R.id.gridView1);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				iconClick(position);

			}
		});


		utils = new Utils(this);

		preferences = getSharedPreferences("comicsMagic", MODE_PRIVATE);
		editPreferences = preferences.edit();
		String currentPath = preferences.getString("currentPath", android.os.Environment.getExternalStorageDirectory().getAbsolutePath());

		currentDirectory = new File(currentPath);
		if (!currentDirectory.exists() || !currentDirectory.isDirectory()){
			currentDirectory = android.os.Environment.getExternalStorageDirectory();
		}
		openDirectory(currentDirectory);

	}

	private void iconClick(int number){
		File f = files.get(number);
		if (f.isDirectory()){
			openDirectory(f);
		} else {
			currentComic = f;
			openComic();
		}


	}

	private void openDirectory(File file) {
		currentDirectory = file;
		editPreferences.putString("currentPath", file.getAbsolutePath());
		editPreferences.commit();
		new LoadComics().execute();
	}


	private void openComic() {
		new LoadComic().execute();
	}

	private void comicFilesLoaded(){
		String md5Name = Utils.md5(currentComic.getAbsolutePath());
		int lastPage = preferences.getInt(md5Name, 0);

		editPreferences.putInt("pageNumber", lastPage);
		editPreferences.commit();

		//Intent i = new Intent(this, PageActivity.class);
		Intent i = new Intent(this, FullScreenViewActivity.class);
		i.putExtra("fileName", currentComic.getAbsolutePath());
		i.putExtra("md5Name", md5Name);
		this.startActivity(i);
	}



	private class LoadComic extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			fileNames = utils.getAllImagesFile(currentComic.getAbsolutePath());
			Collections.sort(fileNames);
			comicFilesLoaded();
			return null;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(SelectComicActivity.this, "",
					"Loading comic", true);
		}

		@Override
		protected void onPostExecute(Object result) {
			progressDialog.dismiss();
		}

	}

	private class LoadComics extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			try {
				files = utils.getFiles(currentDirectory);
			} catch (Exception e) {
				currentDirectory = new File("/");
				files = utils.getFiles(currentDirectory);
			}
			for (File f : files) {
				if ((f != null) && (!f.isDirectory())){
					utils.getFirstImageFile(f); //Preload
				}
			}

			return null;
		}

		@Override
		protected void 	onPreExecute(){
			progressDialog = ProgressDialog.show(SelectComicActivity.this, "", "Loading comics", true);
		}

		@Override
		protected void onPostExecute(Object result){

			if (gridView.getAdapter() instanceof ComicAdapter) {
				((ComicAdapter) gridView.getAdapter()).changeModelList (files);
			} else {
				gridView.setAdapter(new ComicAdapter(SelectComicActivity.this, files, utils));
			}
			progressDialog.dismiss();
		}



	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.page, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int fitStyle = AppConstant.FIT_WIDTH;
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

}
