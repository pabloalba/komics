package net.kaleidos.comicsmagic.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class Utils {
	private Context _context;

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
			File thumbnailDir = new File (outputDir.getAbsolutePath() + File.separator + "thumbnail");
			thumbnailDir.mkdir();
			
			outputFile = new File(thumbnailDir + File.separator + file.getName() + ".jpg");
			if (!outputFile.exists()) {
				ZipFile zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
				String firstImage = "ZZ";
				ZipEntry firstImageEntry = null;
				while (zipEntries.hasMoreElements()) {
					ZipEntry ze = ((ZipEntry)zipEntries.nextElement());
					if (!ze.isDirectory()
							&& isSupportedFile(ze.getName(), AppConstant.IMAGE_EXTN)) {
						if (firstImage.compareTo(ze.getName()) > 0){
							firstImage = ze.getName();
							firstImageEntry = ze;
						}
					}
				}
				
				if (firstImageEntry != null) {
					InputStream in = zipFile.getInputStream(firstImageEntry);					
					FileOutputStream fout = new FileOutputStream(outputFile);
					Bitmap imageBitmap = BitmapFactory.decodeStream(in);
					imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 142,
							200, false);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					fout.write(baos.toByteArray());
					fout.close();
					in.close();
				}
			}
		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}
		return outputFile;

	}
	
	

	public ArrayList<String> getAllImagesFile(String fileName) {
		ArrayList<String> fileNames = new ArrayList<String>();
		try {
			File cacheDir = _context.getCacheDir(); // temp dir
			
			File comicsDir = new File (cacheDir.getAbsolutePath() + File.separator + "comics");
			comicsDir.mkdir();
			
			//Delete old comics
			deleteOldComics(comicsDir);
			
			File file = new File(fileName);
			File outputDir = new File (comicsDir.getAbsolutePath() + File.separator + file.getName());
			if (!outputDir.exists()) {
				outputDir.mkdir();
				FileInputStream fin = new FileInputStream(file);
				ZipInputStream zin = new ZipInputStream(fin);
				ZipEntry ze = null;
				while ((ze = zin.getNextEntry()) != null) {
					if (!ze.isDirectory()
							&& isSupportedFile(ze.getName(), AppConstant.IMAGE_EXTN)) {
						File outputFile = new File (outputDir.getAbsolutePath() + File.separator + ze.getName());
						FileOutputStream fout = new FileOutputStream(outputFile);
	
						byte[] buffer = new byte[4096];
						for (int c = zin.read(buffer); c != -1; c = zin
								.read(buffer)) {
							fout.write(buffer, 0, c);
						}
	
						zin.closeEntry();
						fout.close();
					
						fileNames.add(outputFile.getAbsolutePath());
					}
				}
				zin.close();
			} else {
				File[] files = outputDir.listFiles();
				for (int i=0; i<files.length;i++){
					fileNames.add(files[i].getAbsolutePath());
				}
			}
		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}
		return fileNames;

	}
	
	/**
	 * Delete comics cache. If there are more than 3 comics stored, delete
	 * the oldests
	 * @param comicsDir
	 */
	public void deleteOldComics(File comicsDir){
		File[] files = comicsDir.listFiles();
		if (files.length > 3) {
			Arrays.sort(files, new Comparator<File>(){
			    public int compare(File f1, File f2)
			    {
			        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			    } });
			for (int i =0; i<files.length-3;i++){
				deleteDir(files[i]);
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
