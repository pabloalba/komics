package net.kaleidos.comicsmagic;

import java.io.File;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.adapter.ComicAdapter;
import net.kaleidos.comicsmagic.helper.Utils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.Toast;

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

		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parentView,
					View childView, int position, long id) {
				deleteComicDialog(position);
				return true;
			}
		});

		utils = new Utils(this);

		preferences = getSharedPreferences("comicsMagic", MODE_PRIVATE);
		editPreferences = preferences.edit();

		String currentPath = preferences.getString("currentPath",
				android.os.Environment.getExternalStorageDirectory()
						.getAbsolutePath());

		currentDirectory = new File(currentPath);
		if (!currentDirectory.exists() || !currentDirectory.isDirectory()) {
			currentDirectory = android.os.Environment
					.getExternalStorageDirectory();
		}
		openDirectory(currentDirectory);

	}

	public void deleteComicDialog(final int position) {
		File f = files.get(position);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Delete");
		alert.setMessage("Do you want to delete " + f.getName() + "?");
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				deleteComic(position);
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});
		alert.show();
	}

	private void deleteComic(int number) {
		File file = files.get(number);
		String name = file.getName();
		if (deleteFile(file)) {
			files.remove(file);
			reloadFilesList();
			Toast.makeText(this, name + " has been deleted", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, "There was a problem deleting " + name,
					Toast.LENGTH_SHORT).show();
		}
	}

	public static boolean deleteFile(File file) {
		if (file != null) {
			if (file.isDirectory()) {
				String[] children = file.list();
				for (int i = 0; i < children.length; i++) {
					boolean success = deleteFile(new File(file, children[i]));
					if (!success) {
						return false;
					}
				}
			}
			return file.delete();
		}
		return false;
	}

	private void iconClick(int number) {
		File f = files.get(number);
		if (f.isDirectory()) {
			openDirectory(f);
		} else {
			currentComic = f;
			openComic();
		}

	}

	private void openDirectory(File file) {
		currentDirectory = file;

		LoadComics loadComics = new LoadComics();
		loadComics.setAbsolutePath(file.getAbsolutePath());
		loadComics.execute();
	}

	private void openComic() {
		new LoadComic().execute();
	}

	private void comicFilesLoaded() {
		String md5Name = Utils.md5(currentComic.getAbsolutePath());
		int lastPage = preferences.getInt(md5Name, 0);

		editPreferences.putInt("pageNumber", lastPage);
		editPreferences.commit();

		// Intent i = new Intent(this, PageActivity.class);
		Intent i = new Intent(this, FullScreenViewActivity.class);
		// Intent i = new Intent(this, GalleryFileActivity.class);
		i.putExtra("fileName", currentComic.getAbsolutePath());
		i.putExtra("md5Name", md5Name);
		this.startActivity(i);
	}

	private class LoadComic extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
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
		String path;

		public void setAbsolutePath(String path) {
			this.path = path;
		}

		@Override
		protected Object doInBackground(Object... params) {

			try {
				files = utils.getFiles(currentDirectory);
			} catch (Exception e) {
				currentDirectory = new File("/");
				files = utils.getFiles(currentDirectory);
			}

			// Do no preload
			/*
			 * for (File f : files) { if ((f != null) && (!f.isDirectory())) {
			 * utils.getFirstImageFile(f); // Preload } }
			 */

			return null;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(SelectComicActivity.this, "",
					"Loading comics", true);
		}

		@Override
		protected void onPostExecute(Object result) {

			if (gridView.getAdapter() instanceof ComicAdapter) {
				((ComicAdapter) gridView.getAdapter()).changeModelList(files);
			} else {
				gridView.setAdapter(new ComicAdapter(SelectComicActivity.this,
						files, utils, preferences));
			}

			// Save path after the load has been made
			editPreferences.putString("currentPath", path);
			editPreferences.commit();

			progressDialog.dismiss();
		}

	}

	public void reloadFilesList() {
		if ((gridView.getAdapter() instanceof ComicAdapter) && (files != null)) {
			((ComicAdapter) gridView.getAdapter()).changeModelList(files);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		reloadFilesList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_select_comic, menu);

		boolean showFrontPages = preferences.getBoolean("showFrontPages", true);
		menu.findItem(R.id.show_front_pages).setChecked(showFrontPages);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.show_front_pages) {
			boolean showFrontPages = !item.isChecked();
			item.setChecked(showFrontPages);

			editPreferences.putBoolean("showFrontPages", showFrontPages);
			editPreferences.commit();

			reloadFilesList();

		} else {
			Utils.saveFitPreferenceFromMenu(item, editPreferences);
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		Utils.markSelectedItemAsChecked(menu, preferences);
		return true;
	}

}
