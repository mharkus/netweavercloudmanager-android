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

package com.sap.nwcloudmanager.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;

import com.google.analytics.tracking.android.Log;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

public class LogAPI extends BaseAPI {

	public static void getLogs(final String appId, final ListLogsResponseHandler handler) {

		NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
		getHttpClient().setBasicAuth(config.getUsername(), config.getPassword());
		getHttpClient().get("https://monitoring." + config.getHost() + "/log/api_basic/logs/" + config.getAccount() + "/" + appId + "/web", null, new JsonHttpResponseHandler() {

			@Override
			protected void handleFailureMessage(Throwable arg0, String arg1) {
				handler.onFailure(arg0, arg1);
			}

			@Override
			public void onSuccess(JSONObject json) {
				
				if (json.has("logFiles")) {
					try {
						JSONArray logFiles = json.getJSONArray("logFiles");
						List<LogFile> list = new ArrayList<LogFile>();
						for (int i = 0; i < logFiles.length(); i++) {
							JSONObject logFile = logFiles.getJSONObject(i);
							LogFile obj = new LogFile();
							obj.setComponent(logFile.getString("component"));
							obj.setName(logFile.getString("name"));
							obj.setSize(logFile.getLong("size"));
							obj.setLastModified(new Date(logFile.getLong("lastModified")));
							obj.setDescription(logFile.getString("description"));

							list.add(obj);
						}

						handler.onSuccess(list);

					} catch (JSONException e) {
						Log.e(e.getMessage());
					}
				}
			}

			@Override
			public void onFailure(Throwable arg0) {
				handler.onFailure(arg0, arg0.getMessage());
			}

		});
	}

	public static void downloadLog(final Context context, final String appId, final String logName, final DownloadLogResponseHandler downloadLogResponseHandler) {

		NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
		getHttpClient().setBasicAuth(config.getUsername(), config.getPassword());
		String[] contentTypes = { "application/x-gzip" };
		getHttpClient().get("https://monitoring." + config.getHost() + "/log/api_basic/logs/" + config.getAccount() + "/" + appId + "/web/" + logName, null, new BinaryHttpResponseHandler(contentTypes) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.loopj.android.http.BinaryHttpResponseHandler#onSuccess(byte
			 * [])
			 */
			@Override
			public void onSuccess(byte[] arg0) {

				GZIPInputStream gzis = null;
				
				try {
					gzis = new GZIPInputStream(new ByteArrayInputStream(arg0));
					
					StringBuilder string = new StringBuilder();
				    byte[] data = new byte[4028];
				    int bytesRead;
				    while ((bytesRead = gzis.read(data)) != -1) {
				        string.append(new String(data, 0, bytesRead));
				    }
				    
				    File downloadsFile = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
				    if(downloadsFile.exists()){
				    	downloadsFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + logName);
				    	FileOutputStream fos = new FileOutputStream(downloadsFile);
				    	fos.write(string.toString().getBytes());
				    	fos.close();
				    }
				    
				    downloadLogResponseHandler.onSuccess(downloadsFile.getAbsolutePath());
					
				} catch (IOException e) {
					Log.e(e.getMessage());
				}finally{
					try {
						if(gzis != null){
							gzis.close();
						}
					} catch (IOException e) {
					}
				}
			}

			@Override
			public void onFailure(Throwable arg0) {
				downloadLogResponseHandler.onFailure(arg0, arg0.getMessage());
			}

		});
	}
}
