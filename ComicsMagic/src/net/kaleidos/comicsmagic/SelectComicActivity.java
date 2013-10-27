package net.kaleidos.comicsmagic;

import java.io.File;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.adapter.ComicAdapter;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class SelectComicActivity extends Activity {
	 
		GridView gridView;
		Utils utils;
		ArrayList<File> files;
		
	 
	 
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_select_comic);
			utils = new Utils(this);
	 
			gridView = (GridView) findViewById(R.id.gridView1);
			
			files = utils.getFiles();
			
			gridView.setAdapter(new ComicAdapter(this, files, utils));
	 
			gridView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					openComic(position);
	 
				}
			});
	 
		}
		
		@Override
		protected void onDestroy() {
			super.onDestroy();
			utils.deleteCache();
		}

		private void openComic(int number){
			Intent i = new Intent(this, PageActivity.class);
            i.putExtra("file", files.get(number).getAbsolutePath());
            this.startActivity(i);
		}
	 
	}
