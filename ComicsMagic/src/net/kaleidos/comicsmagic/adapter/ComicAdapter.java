package net.kaleidos.comicsmagic.adapter;

import java.io.File;
import java.util.ArrayList;

import net.kaleidos.comicsmagic.R;
import net.kaleidos.comicsmagic.helper.Utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ComicAdapter extends BaseAdapter {
	private final Context context;
	private ArrayList<File> files;
	private final SharedPreferences preferences;

	public void setFiles(ArrayList<File> files) {
		this.files = files;
	}

	private final Utils utils;

	public ComicAdapter(Context context, ArrayList<File> files, Utils utils,
			SharedPreferences preferences) {
		this.context = context;
		this.files = files;
		this.utils = utils;
		this.preferences = preferences;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View gridView;

		if (convertView == null) {
			gridView = new View(context);
			// get layout from mobile.xml
			gridView = inflater.inflate(R.layout.comic_file, null);

		} else {
			gridView = convertView;
		}
		// set value into textview
		TextView textView = (TextView) gridView
				.findViewById(R.id.grid_item_label);
		// set image based on selected text
		ImageView imageView = (ImageView) gridView
				.findViewById(R.id.grid_item_image);

		File file = files.get(position);

		if (position == 0) {
			// Add a "go up" special folder
			textView.setText("..");
			imageView.setImageResource(R.drawable.back);

		} else {

			textView.setText(file.getName());
			String md5Name = Utils.md5(file.getAbsolutePath());
			if (preferences.getBoolean("readed_" + md5Name, false)) {
				textView.setTextColor(Color.GRAY);
			} else {
				textView.setTextColor(Color.WHITE);
			}
			imageView.setImageResource(R.drawable.comic);

			if (file.isDirectory()) {
				imageView.setImageResource(R.drawable.folder);
			} else {
				if (preferences.getBoolean("showFrontPages", true)) {
					File f = utils.getFirstImageFile(file);
					if (f != null) {
						Bitmap bmImg = BitmapFactory.decodeFile(f
								.getAbsolutePath());
						imageView.setImageBitmap(bmImg);
					} else {
						imageView.setImageResource(R.drawable.comic);
					}
				} else {
					imageView.setImageResource(R.drawable.comic);
				}
			}
		}

		return gridView;
	}

	@Override
	public int getCount() {
		return files.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void changeModelList(ArrayList<File> files) {
		this.files = files;
		notifyDataSetChanged();
	}

}
