/*
 * Copyright (C) 2013 Marc Lester Tan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sap.nwcloudmanager;

import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.nwcloudmanager.api.AppDetails;
import com.sap.nwcloudmanager.api.DeployAPI;
import com.sap.nwcloudmanager.api.GetAppDetailsResponseHandler;
import com.sap.nwcloudmanager.api.ListAppsResponseHandler;

public class MainActivity extends GATrackingActivity {

	public static final int REFRESH = 0;
	private ListView apps;
	private String[] appIds;
	private ImageView refreshButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bindLogout();

		refreshButton = (ImageView) findViewById(R.id.refresh);
		refreshButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showProgress();
				loadData();
			}
		});
		apps = (ListView) findViewById(R.id.apps);
		apps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
			 * .widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				if (arg1.getTag() == null) {
					Toast.makeText(MainActivity.this, "Please wait for the appliance status", Toast.LENGTH_LONG).show();
					return;
				}

				AppDetails details = (AppDetails) arg1.getTag();
				Intent intent = new Intent(MainActivity.this, AppDetailsActivity.class);
				intent.putExtra("details", details);
				startActivityForResult(intent, 0);

				overridePendingTransition(R.anim.activityincoming, R.anim.activityoutgoing);

			}
		});

		loadData();
	}

	/**
	 * 
	 */
	protected void showProgress() {
		refreshButton.startAnimation(loadAnimation);
	}

	private void hideProgress() {
		loadAnimation.cancel();
	}

	private void loadData() {
		final SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
		final Editor editor = pref.edit();

		showProgress();
		DeployAPI.listApps(new ListAppsResponseHandler() {

			@Override
			public void onFailure(Throwable t, String message) {

				editor.putBoolean("valid", false);
				editor.commit();

				hideProgress();
				new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.ModifiedDialog)).setMessage(t.getLocalizedMessage()).setTitle("Error").setPositiveButton("Ok", null).create().show();

			}

			@Override
			public void onSuccess(final String[] strings) {
				editor.putBoolean("valid", true);
				editor.commit();

				hideProgress();
				MainActivity.this.appIds = strings;
				apps.setAdapter(new ApplicationListAdapter(MainActivity.this, strings));

			}
		});
	}

	class ApplicationListAdapter extends BaseAdapter {

		private Context context;
		private String[] items;

		/**
		 * 
		 */
		public ApplicationListAdapter(Context context, String[] items) {
			this.context = context;
			this.items = items;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return items.length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return items[position];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(R.layout.application_list_item, null);
			TextView name = (TextView) view.findViewById(R.id.name);
			ImageView status = (ImageView) view.findViewById(R.id.status);
			ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);

			String item = (String) getItem(position);
			name.setText(item);

			DeployAPI.getAppDetails(item, new GetAppDetailsResponseHandler(view, status, progress) {

				@Override
				public void onFailure(Throwable t, String message) {
					new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.ModifiedDialog)).setMessage(t.getLocalizedMessage()).setTitle("Error").setPositiveButton("Ok", null).create().show();
				}

				@Override
				public void onSuccess(AppDetails status) {
					if (status.getStatus().equalsIgnoreCase("started")) {
						getStatus().setImageResource(R.drawable.started);
					} else if (status.getStatus().equalsIgnoreCase("stopped")) {
						getStatus().setImageResource(R.drawable.stopped);
					} else if (status.getStatus().equalsIgnoreCase("failed")) {
						getStatus().setImageResource(R.drawable.stopped);
					} else {
						getStatus().setImageResource(R.drawable.starting);
					}

					getView().setTag(status);
					getStatus().setVisibility(View.VISIBLE);
					getProgress().setVisibility(View.GONE);

				}
			});

			return view;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data == null || data.getExtras() == null) {
			return;
		}

		if (resultCode == REFRESH) {
			showProgress();
			String appId = data.getExtras().getString("appId");

			DeployAPI.getAppDetails(appId, new GetAppDetailsResponseHandler() {

				@Override
				public void onFailure(Throwable arg0, String message) {
					hideProgress();
					new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.ModifiedDialog)).setTitle("Error").setMessage(arg0.getLocalizedMessage()).setPositiveButton("Ok", null).create().show();
				}

				@Override
				public void onSuccess(AppDetails status) {
					hideProgress();
					List<String> ids = Arrays.asList(appIds);
					int index = ids.indexOf(status.getApplianceId());
					View parent = apps.getChildAt(index);
					parent.setTag(status);
					ImageView statusImage = (ImageView) parent.findViewById(R.id.status);
					if (status.getStatus().equalsIgnoreCase("started")) {
						statusImage.setImageResource(R.drawable.started);
					} else if (status.getStatus().equalsIgnoreCase("stopped")) {
						statusImage.setImageResource(R.drawable.stopped);
					} else {
						statusImage.setImageResource(R.drawable.starting);
					}
				}
			});

		}

	}

}
