package net.kaleidos.comicsmagic.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.kaleidos.comicsmagic.helper.extractor.RarExtractor;
import net.kaleidos.comicsmagic.helper.extractor.ZipExtractor;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class Utils {
	private final Context _context;

	// constructor
	public Utils(Context context) {
		this._context = context;
	}

	// Reading file paths from SDCard
	public ArrayList<File> getFiles(File directory) {
		ArrayList<File> files = new ArrayList<File>();

		// getting list of file paths
		File[] listFiles = directory.listFiles();

		// Check for count
		if (listFiles.length > 0) {


			Arrays.sort(listFiles, new Comparator<File>(){
				@Override
				public int compare(File f1, File f2)
				{
					//First folders
					if (f1.isDirectory() && !f2.isDirectory()){
						return -1;
					}
					if (!f1.isDirectory() && f2.isDirectory()){
						return 1;
					}

					return f1.getName().compareToIgnoreCase(f2.getName());
				} });


			// loop through all files
			for (int i = 0; i < listFiles.length; i++) {

				// get file path
				String filePath = listFiles[i].getAbsolutePath();

				// check for supported file extension
				if (listFiles[i].canRead() &&
						((isSupportedFile(filePath, AppConstant.COMIC_EXTN)) ||
								listFiles[i].isDirectory())){
					// Add image path to array list
					files.add(listFiles[i]);
				}
			}
		}

		//Add "go back" on first position
		if (directory.getParent() != null){
			files.add(0, new File(directory.getParent()));
		} else {
			files.add(0, directory);
		}


		return files;
	}

	// Check supported file extensions
	public static boolean isSupportedFile(String filePath,
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
		if (Utils.isSupportedFile(file.getName(), AppConstant.COMIC_EXTN_ZIP)){
			return ZipExtractor.getFirstImageFile(file, _context);
		} else if (Utils.isSupportedFile(file.getName(), AppConstant.COMIC_EXTN_RAR)){
			return RarExtractor.getFirstImageFile(file, _context);
		}
		return null;

	}



	public ArrayList<String> getAllImagesFile(String fileName) {
		ArrayList<String> fileNames = new ArrayList<String>();

		File file = new File(fileName);
		File cacheDir = _context.getCacheDir(); // temp dir

		File comicsDir = new File (cacheDir.getAbsolutePath() + File.separator + "comics");
		comicsDir.mkdir();

		//Delete old comics
		Utils.deleteOldComics(comicsDir);

		String md5Name = Utils.md5(file.getAbsolutePath());
		File outputDir = new File (comicsDir.getAbsolutePath() + File.separator + md5Name);
		if (!outputDir.exists()) {
			outputDir.mkdir();
			if (Utils.isSupportedFile(file.getName(), AppConstant.COMIC_EXTN_ZIP)){
				fileNames = ZipExtractor.getAllImagesFile(file, outputDir);
			} else if (Utils.isSupportedFile(file.getName(), AppConstant.COMIC_EXTN_RAR)){
				fileNames = RarExtractor.getAllImagesFile(file, outputDir);
			}
		} else {
			File[] files = outputDir.listFiles();
			for (int i=0; i<files.length;i++){
				fileNames.add(files[i].getAbsolutePath());
			}
		}
		return fileNames;
	}

	/**
	 * Delete comics cache. If there are more than 3 comics stored, delete
	 * the oldests
	 * @param comicsDir
	 */
	public static void deleteOldComics(File comicsDir){
		File[] files = comicsDir.listFiles();
		if (files.length > 3) {
			Arrays.sort(files, new Comparator<File>(){
				@Override
				public int compare(File f1, File f2)
				{
					return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
				} });
			for (int i =0; i<files.length-3;i++){
				Utils.deleteDir(files[i]);
			}
		}

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

	public static boolean deleteDir(File dir) {
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

	public static final String md5(final String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Bitmap readBitmapFromStream(InputStream in) throws IOException{
		//Read the file to memory
		byte[] byteArr = new byte[0];
		byte[] buffer = new byte[1024];
		int len;
		int count = 0;


		while ((len = in.read(buffer)) > -1) {
			if (len != 0) {
				if (count + len > byteArr.length) {
					byte[] newbuf = new byte[(count + len) * 2];
					System.arraycopy(byteArr, 0, newbuf, 0, count);
					byteArr = newbuf;
				}

				System.arraycopy(buffer, 0, byteArr, count, len);
				count += len;
			}
		}

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inInputShareable =true;

		return BitmapFactory.decodeByteArray(byteArr, 0, count, options);
	}

}
