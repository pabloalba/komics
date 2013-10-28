package net.kaleidos.comicsmagic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import net.kaleidos.comicsmagic.adapter.ComicAdapter;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class SelectComicActivity extends Activity {

	GridView gridView;
	Utils utils;
	ArrayList<File> files = null;
	ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_comic);

		gridView = (GridView) findViewById(R.id.gridView1);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				openComic(position);

			}
		});

		
		utils = new Utils(this);
		
		new LoadComics().execute();
		
	}



	private void openComic(int number) {
		Intent i = new Intent(this, PageActivity.class);
		i.putExtra("file", files.get(number).getAbsolutePath());
		this.startActivity(i);
	}
	
	private class LoadComics extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			files = utils.getFiles();
        	for (File f : files) {
				utils.getFirstImageFile(f); //Preload
			}
			
			return null;
		}
		
		protected void 	onPreExecute(){
			Log.e("LoadComic", "0");
			progressDialog = ProgressDialog.show(SelectComicActivity.this, "", "Loading comics", true);
		}
		
		protected void onPostExecute(Object result){
			Log.e("LoadComic", "3");
			gridView.setAdapter(new ComicAdapter(SelectComicActivity.this, files, utils));
			progressDialog.dismiss();
		}
		
		
	
	}

}
