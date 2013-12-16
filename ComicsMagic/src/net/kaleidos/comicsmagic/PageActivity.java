package net.kaleidos.comicsmagic;

import android.app.Activity;

public class PageActivity extends Activity {
	/*
	ArrayList<String> fileNames;
	TouchImageView touchImageView;
	int number = 0;
	Utils utils;
	ProgressDialog progressDialog;
	SharedPreferences preferences;
	SharedPreferences.Editor editPreferences;
	float middleX;
	int fitStyle = AppConstant.FIT_WIDTH;
	String md5Name;
	Bitmap bmImg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = getSharedPreferences("comicsMagic", MODE_PRIVATE);
		editPreferences = preferences.edit();
		number = preferences.getInt("pageNumber", 0);
		fitStyle = preferences.getInt("fitStyle", AppConstant.FIT_WIDTH);

		touchImageView = new TouchImageView(this);
		touchImageView.setBackgroundColor(Color.BLACK);
		touchImageView.setFitStyle(fitStyle);
		touchImageView.setMaxZoom(8f);
		setContentView(touchImageView);
		utils = new Utils(this);

		middleX = utils.getScreenWidth() / 2;

		new LoadComic().execute();

		touchImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int newNumber = number;
				if ((touchImageView.getLast().x<middleX) && (number>0)) {
					newNumber--;
				}
				if ((touchImageView.getLast().x>middleX) && (number<fileNames.size()-1)) {
					newNumber++;
				}

				if (newNumber != number) {
					number = newNumber;
					editPreferences.putInt("pageNumber", number);
					editPreferences.putInt(md5Name, number);
					editPreferences.commit();
					showPage(number);
				}
			}
		});

	}

	private void showPage(int number) {
		if (bmImg != null) {
			bmImg.recycle();
		}

		try {
			File file = new File(fileNames.get(number));
			InputStream in = new FileInputStream(file);
			bmImg = Utils.readBitmapFromStream(in);
			in.close();
			touchImageView.loadNewPage(bmImg);
		} catch (Exception e){
			Log.e("PageActivity", "Error on showPage", e);
		}
	}

	private class LoadComic extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			Intent i = getIntent();
			String fileName = i.getStringExtra("file");
			fileNames = utils.getAllImagesFile(fileName);
			Collections.sort(fileNames);
			md5Name = i.getStringExtra("md5Name");
			return null;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(PageActivity.this, "",
					"Loading comic", true);
		}

		@Override
		protected void onPostExecute(Object result) {
			showPage(number);
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
		touchImageView.setFitStyle(fitStyle);
		touchImageView.fit();
		showPage(number);
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
	 */
}
