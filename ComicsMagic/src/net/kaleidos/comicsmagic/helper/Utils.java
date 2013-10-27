package net.kaleidos.comicsmagic.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class Utils {
	private Context _context;

	// constructor
	public Utils(Context context) {
		this._context = context;
	}

	// Reading file paths from SDCard
	public ArrayList<File> getFiles() {
		ArrayList<File> files = new ArrayList<File>();

		File directory = new File(
				android.os.Environment.getExternalStorageDirectory()
						+ File.separator + AppConstant.PHOTO_ALBUM);

		// check for directory
		if (directory.isDirectory()) {
			// getting list of file paths
			File[] listFiles = directory.listFiles();

			// Check for count
			if (listFiles.length > 0) {

				// loop through all files
				for (int i = 0; i < listFiles.length; i++) {

					// get file path
					String filePath = listFiles[i].getAbsolutePath();

					// check for supported file extension
					if (isSupportedFile(filePath, AppConstant.COMIC_EXTN)) {
						// Add image path to array list
						files.add(listFiles[i]);
					}
				}
			} else {
				// image directory is empty
				Toast.makeText(
						_context,
						AppConstant.PHOTO_ALBUM
								+ " is empty. Please load some images in it !",
						Toast.LENGTH_LONG).show();
			}

		} else {
			AlertDialog.Builder alert = new AlertDialog.Builder(_context);
			alert.setTitle("Error!");
			alert.setMessage(AppConstant.PHOTO_ALBUM
					+ " directory path is not valid! Please set the image directory name AppConstant.java class");
			alert.setPositiveButton("OK", null);
			alert.show();
		}

		return files;
	}

	// Check supported file extensions
	private boolean isSupportedFile(String filePath,
			List<String> validExtensions) {
		String ext = filePath.substring((filePath.lastIndexOf(".") + 1),
				filePath.length());

		return validExtensions.contains(ext.toLowerCase(Locale.getDefault()));
	}

	/*
	 * getting screen width
	 */
	@SuppressLint("NewApi")
	public int getScreenWidth() {
		int columnWidth;
		WindowManager wm = (WindowManager) _context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) { // Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		columnWidth = point.x;
		return columnWidth;
	}

	public File getFirstImageFile(File file) {
		File outputFile = null;
		try {
			File outputDir = _context.getCacheDir(); // temp dir
			FileInputStream fin = new FileInputStream(file);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			boolean ok = false;
			while (((ze = zin.getNextEntry()) != null) && (!ok)) {
				if (!ze.isDirectory()
						&& isSupportedFile(ze.getName(), AppConstant.IMAGE_EXTN)) {
					outputFile = File.createTempFile("img_", "img", outputDir);
					FileOutputStream fout = new FileOutputStream(outputFile);

					Bitmap imageBitmap = BitmapFactory.decodeStream(zin);

					imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 142,
							200, false);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					fout.write(baos.toByteArray());

					zin.closeEntry();
					fout.close();
					ok = true;
				}
			}
			zin.close();
		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}
		return outputFile;

	}

	public ArrayList<File> getAllImagesFile(String fileName) {
		ArrayList<File> files = new ArrayList<File>();
		try {
			File outputDir = _context.getCacheDir(); // temp dir
			File file = new File(fileName);
			FileInputStream fin = new FileInputStream(file);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				if (!ze.isDirectory()
						&& isSupportedFile(ze.getName(), AppConstant.IMAGE_EXTN)) {
					File outputFile = File.createTempFile("img_", "img",
							outputDir);
					FileOutputStream fout = new FileOutputStream(outputFile);

					byte[] buffer = new byte[4096];
					for (int c = zin.read(buffer); c != -1; c = zin
							.read(buffer)) {
						fout.write(buffer, 0, c);
					}

					zin.closeEntry();
					fout.close();
					files.add(outputFile);
				}
			}
			zin.close();
		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}
		return files;

	}

	public void deleteCache() {
		try {
			File dir = _context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				deleteDir(dir);
			}
		} catch (Exception e) {
		}
	}

	public boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
}
