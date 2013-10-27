package net.kaleidos.comicsmagic;

import java.io.File;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;

public class PageActivity extends Activity {
		ArrayList<File> files;
		ImageView imageView;
		int number = 0;
	 
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Utils utils = new Utils(this);
			Intent i = getIntent();
			String fileName = i.getStringExtra("file");
			this.files = utils.getAllImagesFile(fileName);
			
			setContentView(R.layout.page_view);
			imageView = (ImageView) findViewById(R.id.imageView1);
			
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					number++;
					showPage(number);
				}
			});
			
			
			
			showPage(number);	 
		}
		
		private void showPage(int number){
			Bitmap bmImg = BitmapFactory.decodeFile(files.get(number).getAbsolutePath());
	        imageView.setImageBitmap(bmImg);
		}
		
		
	 
	}
