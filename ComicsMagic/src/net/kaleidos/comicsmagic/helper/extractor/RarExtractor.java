package net.kaleidos.comicsmagic.helper.extractor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import net.kaleidos.comicsmagic.helper.AppConstant;
import net.kaleidos.comicsmagic.helper.Utils;
import net.kaleidos.comicsmagic.listener.LoadImageListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

public class RarExtractor {

	public static File getFirstImageFile(File file, Context context) {
		File outputFile = null;
		try {
			File outputDir = context.getCacheDir(); // temp dir
			File thumbnailDir = new File(outputDir.getAbsolutePath()
					+ File.separator + "thumbnail");
			thumbnailDir.mkdir();

			outputFile = new File(thumbnailDir + File.separator
					+ file.getName() + ".jpg");
			File outputFileTmp = new File(thumbnailDir + File.separator
					+ file.getName() + ".tmp");
			if (!outputFile.exists()) {

				Archive arch = new Archive(file);
				FileHeader fh = null;
				String firstImage = "";

				while (true) {
					fh = arch.nextFileHeader();
					if (fh == null) {
						break;
					}
					if ((fh != null)
							&& (!fh.isDirectory())
							&& (Utils.isSupportedFile(fh.getFileNameString(),
									AppConstant.IMAGE_EXTN))) {
						if (firstImage == ""
								|| firstImage.compareToIgnoreCase(fh
										.getFileNameString()) > 0) {
							firstImage = fh.getFileNameString();
						}
					}
				}
				arch.close();

				arch = new Archive(file);
				fh = null;

				while (true) {
					fh = arch.nextFileHeader();
					if (fh == null) {
						break;
					}
					if ((fh != null)
							&& (!fh.isDirectory())
							&& (Utils.isSupportedFile(fh.getFileNameString(),
									AppConstant.IMAGE_EXTN))) {
						if (firstImage.equalsIgnoreCase(fh.getFileNameString())) {
							OutputStream stream = new FileOutputStream(
									outputFileTmp);
							arch.extractFile(fh, stream);
							stream.close();

							FileOutputStream fout = new FileOutputStream(
									outputFile);
							InputStream in = new FileInputStream(outputFileTmp);

							Bitmap imageBitmap = Utils.readBitmapFromStream(in);

							imageBitmap = Bitmap.createScaledBitmap(
									imageBitmap, 71, 100, false);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							imageBitmap.compress(Bitmap.CompressFormat.JPEG,
									100, baos);
							fout.write(baos.toByteArray());
							fout.close();
							in.close();
							imageBitmap.recycle();
							outputFileTmp.delete();
							break;
						}
					}
				}

				arch.close();
			}
		} catch (Exception e) {
			Log.e("Decompress", "unrar", e);
		}
		return outputFile;
	}

	public static void decompressImageFile(File file, File outputDir,
			String fileName, LoadImageListener loadImageListener) {

		Log.d("Decompress", "DEBUG RAR Filename searched: " + fileName);

		try {
			Archive arch = new Archive(file);
			FileHeader fh = null;
			boolean ok = false;
			while (!ok) {
				fh = arch.nextFileHeader();
				if (fh == null) {
					ok = true;
					Log.d("Decompress", "DEBUG RAR Filename NOT FOUND: "
							+ fileName);
				}
				if (fh != null) {
					String fileNameString = Utils.absolutePathToString(fh
							.getFileNameString());
					File outputFile = new File(outputDir.getAbsolutePath()
							+ File.separator + fileNameString);

					if (fileName.equals(outputFile.getAbsolutePath())) {
						if (outputFile.exists()) {
							ok = true;
							Log.d("Decompress", "DEBUG RAR Filename FOUND");
						} else {
							ok = true;
							Log.d("Decompress", "DEBUG RAR Filename CREATED");
							OutputStream stream;
							try {
								stream = new FileOutputStream(outputFile);
								arch.extractFile(fh, stream);
								stream.close();
								if (loadImageListener != null) {
									loadImageListener.onLoadImage(outputFile
											.getAbsolutePath());
								}

							} catch (Exception e) {
								Log.e("Decompress", "Fail on unrar file: "
										+ fileNameString, e);
							}
						}
					}
				}
			}

			arch.close();
		} catch (Exception e) {
			Log.e("Decompress", "unrar", e);
			Log.d("Decompress", "DEBUG RAR Filename ERROR");
		}
	}

	public static ArrayList<String> getAllImagesNamesFromFile(File file,
			File outputDir) {
		ArrayList<String> fileNames = new ArrayList<String>();
		try {
			Archive arch = new Archive(file);
			FileHeader fh = null;

			while (true) {
				fh = arch.nextFileHeader();
				if (fh == null) {
					break;
				}
				if ((fh != null)
						&& (!fh.isDirectory())
						&& (Utils.isSupportedFile(fh.getFileNameString(),
								AppConstant.IMAGE_EXTN))) {
					fileNames
							.add(outputDir.getAbsolutePath()
									+ File.separator
									+ Utils.absolutePathToString(fh
											.getFileNameString()));
				}
			}
			arch.close();
		} catch (Exception e) {
			Log.e("Decompress", "unrar", e);
		}
		Collections.sort(fileNames);
		return fileNames;
	}

}
