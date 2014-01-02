package net.kaleidos.comicsmagic.helper.extractor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import net.kaleidos.comicsmagic.listener.LoadImageListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class ZipExtractor {

	public static File getFirstImageFile(File file, Context context) {
		File outputFile = null;
		try {
			File outputDir = context.getCacheDir(); // temp dir
			File thumbnailDir = new File(outputDir.getAbsolutePath()
					+ File.separator + "thumbnail");
			thumbnailDir.mkdir();

			outputFile = new File(thumbnailDir + File.separator
					+ file.getName() + ".jpg");
			if (!outputFile.exists()) {
				ZipFile zipFile = new ZipFile(file);
				Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
				String firstImage = "";
				ZipEntry firstImageEntry = null;
				while (zipEntries.hasMoreElements()) {
					ZipEntry ze = (zipEntries.nextElement());
					if (!ze.isDirectory()
							&& Utils.isSupportedFile(ze.getName(),
									AppConstant.IMAGE_EXTN)) {
						if (firstImage == ""
								|| firstImage.compareToIgnoreCase(ze.getName()) > 0) {
							firstImage = ze.getName();
							firstImageEntry = ze;
						}
					}
				}

				if (firstImageEntry != null) {
					InputStream in = zipFile.getInputStream(firstImageEntry);
					FileOutputStream fout = new FileOutputStream(outputFile);

					Bitmap imageBitmap = Utils.readBitmapFromStream(in);

					imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 71,
							100, false);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					fout.write(baos.toByteArray());
					fout.close();
					in.close();
					imageBitmap.recycle();
				}
			}
		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}
		return outputFile;
	}

	public static void decompressImagesFile(File file, File outputDir,
			Set<String> extractFilenames, LoadImageListener loadImageListener) {
		try {
			ZipFile zipFile = new ZipFile(file);
			String path = outputDir.getAbsolutePath() + File.separator;

			int num = 0;

			for (Iterator iterator = extractFilenames.iterator(); iterator
					.hasNext();) {
				String fullName = (String) iterator.next();
				String zipName = fullName.substring(path.length());
				ZipEntry ze = zipFile.getEntry(zipName);

				File outputFile = new File(outputDir.getAbsolutePath()
						+ File.separator + ze.getName());
				if (!outputFile.exists()) {
					Log.d("DEBUG", "Extract file: " + outputFile);
					FileOutputStream fout = new FileOutputStream(outputFile);
					InputStream zin = zipFile.getInputStream(ze);
					byte[] buffer = new byte[4096];
					for (int c = zin.read(buffer); c != -1; c = zin
							.read(buffer)) {
						fout.write(buffer, 0, c);
					}

					zin.close();
					fout.close();

					if (loadImageListener != null) {
						loadImageListener.onLoadImage(outputFile
								.getAbsolutePath());
					}

				} else {
					Log.d("DEBUG", "Do not extract file: " + outputFile);
				}

			}

		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}

	}

	public static ArrayList<String> getAllImagesNamesFromFile(File file,
			File outputDir) {
		ArrayList<String> fileNames = new ArrayList<String>();
		try {
			ZipFile zipFile = new ZipFile(file);
			Enumeration e = zipFile.entries();

			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				if (!ze.isDirectory()
						&& Utils.isSupportedFile(ze.getName(),
								AppConstant.IMAGE_EXTN)) {
					fileNames.add(outputDir.getAbsolutePath() + File.separator
							+ ze.getName());
				}
			}
			zipFile.close();
		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}
		Collections.sort(fileNames);
		return fileNames;

	}

}
