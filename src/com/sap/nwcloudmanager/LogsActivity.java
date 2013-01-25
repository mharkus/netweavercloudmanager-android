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

import java.text.SimpleDateFormat;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.nwcloudmanager.api.DownloadLogResponseHandler;
import com.sap.nwcloudmanager.api.ListLogsResponseHandler;
import com.sap.nwcloudmanager.api.LogAPI;
import com.sap.nwcloudmanager.api.LogFile;

public class LogsActivity extends GATrackingActivity {
	private ListView names;
	private ImageView refreshButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logs);
		bindLogout();
		final String appId = getIntent().getStringExtra("appId");
		
		refreshButton = (ImageView) findViewById(R.id.refresh);
		names = (ListView) findViewById(R.id.names);
		names.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			/* (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				showProgress();
				LogFile file = (LogFile)((BaseAdapter)arg0.getAdapter()).getItem(arg2);
				LogAPI.downloadLog(LogsActivity.this, appId, file.getName(), new DownloadLogResponseHandler() {
					
					@Override
					public void onFailure(Throwable arg0, String message) {
						hideProgress();
						new AlertDialog.Builder(new ContextThemeWrapper(LogsActivity.this, R.style.ModifiedDialog)).setTitle("Error")
						.setMessage(arg0.getMessage())
						.setPositiveButton("Ok", null)
						.create().show();
					}
					
					@Override
					public void onSuccess(String path) {
						hideProgress();
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse("file:///"+path), "text/plain");
						startActivity(intent);
						
						overridePendingTransition(R.anim.activityincoming, R.anim.activityoutgoing);
					}
				});
			}
		});
		
		showProgress();
		
		LogAPI.getLogs(appId, new ListLogsResponseHandler() {
			
			@Override
			public void onFailure(Throwable arg0, String message) {
				hideProgress();
				new AlertDialog.Builder(new ContextThemeWrapper(LogsActivity.this, R.style.ModifiedDialog))
				.setTitle("Error")
				.setMessage(arg0.getLocalizedMessage())
				.setPositiveButton("Ok", null)
				.create()
				.show();
			}
			
			@Override
			public void onSuccess(List<LogFile> files) {
				hideProgress();
				names.setAdapter(new ListLogsAdapter(LogsActivity.this, files));
			}
		});
	}
	
	class ListLogsAdapter extends BaseAdapter{
		
		private Context context;
		private List<LogFile> files;
		
		public ListLogsAdapter(Context context, List<LogFile> files) {
			this.context = context;
			this.files = files;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return files.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return files.get(position);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(R.layout.logs_list_item, null);
			TextView size = (TextView)view.findViewById(R.id.size);
			TextView lastModified = (TextView)view.findViewById(R.id.lastModified);
			TextView description = (TextView)view.findViewById(R.id.description);
			
			LogFile log = (LogFile) getItem(position);
			size.setText(log.getSize()/1000 + " kb");
			lastModified.setText(new SimpleDateFormat("HH:mm:ss MMM dd, yyyy").format(log.getLastModified()));
			description.setText(log.getDescription());
			
			return view;
		}
		
	}
	
	protected void showProgress() {
		refreshButton.startAnimation(loadAnimation);
	}

	private void hideProgress(){
		loadAnimation.cancel();
	}
}
