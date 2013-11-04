package net.kaleidos.comicsmagic;

import java.io.File;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.adapter.ComicAdapter;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class SelectComicActivity extends Activity {

	GridView gridView;
	Utils utils;
	ArrayList<File> files = null;
	ProgressDialog progressDialog;
	SharedPreferences preferences;
	SharedPreferences.Editor editPreferences;
	File currentDirectory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_comic);

		gridView = (GridView) findViewById(R.id.gridView1);

		gridView.setOnItemClickListener(new OnItemClickListener() {
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
			openComic(f);
		}
		
		
	}

	private void openDirectory(File file) {
		currentDirectory = file;
		editPreferences.putString("currentPath", file.getAbsolutePath());
		editPreferences.commit();
		new LoadComics().execute();
	}


	private void openComic(File file) {
		String md5Name = Utils.md5(file.getAbsolutePath());
		int lastPage = preferences.getInt(md5Name, 0);
		
		editPreferences.putInt("pageNumber", lastPage);				
		editPreferences.commit();
		Intent i = new Intent(this, PageActivity.class);
		i.putExtra("file", file.getAbsolutePath());
		i.putExtra("md5Name", md5Name);
		this.startActivity(i);
		
	}
	
	private class LoadComics extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			
			files = utils.getFiles(currentDirectory);
        	for (File f : files) {
        		if ((f != null) && (!f.isDirectory())){
        			utils.getFirstImageFile(f); //Preload
        		}
			}
			
			return null;
		}
		
		protected void 	onPreExecute(){
			progressDialog = ProgressDialog.show(SelectComicActivity.this, "", "Loading comics", true);
		}
		
		protected void onPostExecute(Object result){

			if (gridView.getAdapter() instanceof ComicAdapter) {
				((ComicAdapter) gridView.getAdapter()).changeModelList (files);
			} else {
				gridView.setAdapter(new ComicAdapter(SelectComicActivity.this, files, utils));
			}
			progressDialog.dismiss();
		}
		
		
	
	}

}
