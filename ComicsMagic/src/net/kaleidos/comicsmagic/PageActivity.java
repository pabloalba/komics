package net.kaleidos.comicsmagic;

import java.io.File;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.components.TouchImageView;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

public class PageActivity extends Activity {
		ArrayList<File> files;
		TouchImageView touchImageView;
		int number = 0;
	 
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Utils utils = new Utils(this);
			Intent i = getIntent();
			String fileName = i.getStringExtra("file");
			this.files = utils.getAllImagesFile(fileName);
			
			
			touchImageView = new TouchImageView(this);
			setContentView(touchImageView);
			
		        
			
			
			
			/*
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					number++;
					showPage(number);
				}
			});
			*/
			
			
			showPage(number);	 
		}
		
		private void showPage(int number){
			Bitmap bmImg = BitmapFactory.decodeFile(files.get(number).getAbsolutePath());
			touchImageView.setImageBitmap(bmImg);
			touchImageView.setMaxZoom(4f);	        
		}
		
		
	 
	}
