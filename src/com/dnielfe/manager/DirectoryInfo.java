/*
 * Copyright (C) 2013 Simple Explorer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package com.dnielfe.manager;

import java.io.File;
import java.text.DateFormat;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.content.Intent;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DirectoryInfo extends Activity {
	private static final int KB = 1024;
	private static final int MG = KB * KB;
	private static final int GB = MG * KB;
	private String mPathName;
	private TextView mPathLabel, mDirLabel, mFileLabel, mTimeLabel, mUsedLabel,
			mAvaibleLabel, mFreeLabel;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folderinfo);

		ActionBar actionBar = getActionBar();
		actionBar.show();
		actionBar.setDisplayHomeAsUpEnabled(true);

		Intent i = getIntent();
		if (i != null) {
			if (i.getAction() != null
					&& i.getAction().equals(Intent.ACTION_VIEW)) {
				mPathName = i.getData().getPath();

				if (mPathName == null)
					mPathName = "";
			} else {
				mPathName = i.getExtras().getString("PATH_NAME");
			}
		}

		mPathLabel = (TextView) findViewById(R.id.path_label);
		mDirLabel = (TextView) findViewById(R.id.dirs_label);
		mFileLabel = (TextView) findViewById(R.id.files_label);
		mTimeLabel = (TextView) findViewById(R.id.time_stamp);
		mUsedLabel = (TextView) findViewById(R.id.permission1);
		mFreeLabel = (TextView) findViewById(R.id.freespace);
		mAvaibleLabel = (TextView) findViewById(R.id.avaible_size);

		new BackgroundWork().execute(mPathName);
	}

	@Override
	public void onBackPressed() {
		finish();
		return;
	}

	private class BackgroundWork extends AsyncTask<String, Void, Long> {
		File dir = new File(mPathName);

		private ProgressDialog dialog;
		private String mDisplaySize;
		private String mAvaibleSize;
		private String mFreeSpace;
		private int mFileCount = 0;
		private int mDirCount = 0;
		long test = dir.getTotalSpace();
		long freespace = dir.getFreeSpace();

		protected void onPreExecute() {
			dialog = ProgressDialog.show(DirectoryInfo.this, "",
					getString(R.string.calcinfo));
			dialog.setCancelable(true);
		}

		protected Long doInBackground(String... vals) {
			File dir = new File(vals[0]);
			long size = 0;
			int len = 0;

			File[] list = dir.listFiles();
			if (list != null)
				len = list.length;

			for (int i = 0; i < len; i++) {
				if (list[i].isFile())
					mFileCount++;
				else if (list[i].isDirectory())
					mDirCount++;
			}

			if (vals[0].equals("/")) {
				StatFs fss = new StatFs(Environment.getRootDirectory()
						.getPath());
				size = fss.getAvailableBlocksLong() * fss.getBlockSizeLong();

			} else {
				size = FileUtils.getDirSize(vals[0]);
			}

			// get used space of FileSystem
			if (size > GB)
				mDisplaySize = String.format("%.2f GB", (double) size / GB);
			else if (size < GB && size > MG)
				mDisplaySize = String.format("%.2f MB", (double) size / MG);
			else if (size < MG && size > KB)
				mDisplaySize = String.format("%.2f KB", (double) size / KB);
			else
				mDisplaySize = String.format("%.2f B", (double) size);

			// get free space of FileSystem
			if (freespace > GB)
				mFreeSpace = String.format("%.2f GB", (double) freespace / GB);
			else if (freespace < GB && freespace > MG)
				mFreeSpace = String.format("%.2f MB", (double) freespace / MG);
			else if (freespace < MG && freespace > KB)
				mFreeSpace = String.format("%.2f KB", (double) freespace / KB);
			else
				mFreeSpace = String.format("%.2f B", (double) freespace);

			// get total size of FileSystem
			if (test > GB)
				mAvaibleSize = String.format("%.2f GB", (double) test / GB);
			else if (test < GB && test > MG)
				mAvaibleSize = String.format("%.2f MB", (double) test / MG);
			else if (test < MG && test > KB)
				mAvaibleSize = String.format("%.2f KB", (double) test / KB);
			else
				mAvaibleSize = String.format("%.2f B", (double) test);

			return size;
		}

		protected void onPostExecute(Long result) {
			File dir = new File(mPathName);

			String avaible = String.valueOf(mAvaibleSize);

			DateFormat dateFormat = android.text.format.DateFormat
					.getDateFormat(getApplicationContext());
			DateFormat timeFormat = android.text.format.DateFormat
					.getTimeFormat(getApplicationContext());

			mPathLabel.setText(dir.getAbsolutePath());
			mDirLabel.setText(mDirCount + getString(R.string.folders));
			mFileLabel.setText(mFileCount + getString(R.string.files));
			mTimeLabel.setText(dateFormat.format(dir.lastModified()) + " "
					+ timeFormat.format(dir.lastModified()));
			mUsedLabel.setText(mDisplaySize);

			if (dir.getAbsolutePath().equals("/")) {
				mFreeLabel.setText("---");
				mAvaibleLabel.setText("---");
			} else if (!dir.canRead() || !dir.canWrite()) {
				mFreeLabel.setText("---");
				mAvaibleLabel.setText("---");
			} else {
				mFreeLabel.setText(mFreeSpace);
				mAvaibleLabel.setText(avaible);
			}

			dialog.cancel();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menu) {
		switch (menu.getItemId()) {

		case android.R.id.home:
			this.finish();
			return true;
		}
		return false;
	}
}