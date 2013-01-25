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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.Log;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sap.nwcloudmanager.api.AppDetails.ApplicationProcess;


public class DeployAPI extends BaseAPI {

	public static synchronized void getAppDetails(final String applianceId, final GetAppDetailsResponseHandler handler) {
		NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
		getHttpClient().setBasicAuth(config.getUsername(), config.getPassword());
		getHttpClient().get("https://" + config.getHost() + "/domain_basic/spaces/" + config.getAccount() + "/appliances/" + applianceId + "/components/web/", null, new JsonHttpResponseHandler() {

			@Override
			protected void handleFailureMessage(Throwable arg0, String arg1) {
				handler.onFailure(arg0, arg1);
			}

			@Override
			public void onSuccess(JSONObject json) {
				try {
					AppDetails details = new AppDetails();
					details.setApplianceId(applianceId);
					details.setStatus(json.getString("status"));
					details.setUrl(json.optString("url"));
					details.setDomain(json.optString("domain"));
					details.setUrls((String) json.opt("urls"));

					JSONArray accessPoints = json.optJSONArray("accessPoints");
					if (accessPoints != null) {
						String[] temp = new String[accessPoints.length()];
						for (int i = 0; i < accessPoints.length(); i++) {
							String accessPoint = (String) accessPoints.get(i);
							temp[i] = accessPoint;
						}

						details.setAccessPoints(temp);
					}

					JSONArray process = json.optJSONArray("applicationProcesses");
					if (process != null) {
						for (int i = 0; i < process.length(); i++) {
							JSONObject obj = (JSONObject) process.get(i);
							ApplicationProcess proc = new ApplicationProcess();
							proc.setProcessId(obj.getString("processId"));
							proc.setStatus(obj.getString("status"));
							details.getProcesses().add(proc);
						}

					}

					Map<String, String> keyVal = new HashMap<String, String>();
					JSONObject detailsObj = json.optJSONObject("details");
					if (detailsObj != null) {
						JSONArray names = detailsObj.names();
						for (int i = 0; i < names.length(); i++) {
							String name = (String) names.get(i);
							String val = (String) detailsObj.get(name);
							keyVal.put(name, val);
						}

						details.getDetails().putAll(keyVal);

					}

					handler.onSuccess(details);

				} catch (Exception e) {
					Log.e(e.getMessage());
				}

			}

			@Override
			public void onFailure(Throwable arg0) {
				handler.onFailure(arg0, arg0.getMessage());
			}

		});
	}

	public static void listApps(final ListAppsResponseHandler handler) {

		NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
		getHttpClient().setBasicAuth(config.getUsername(), config.getPassword());
		getHttpClient().get("https://" + config.getHost() + "/deploy/" + config.getAccount() + "/appliances", null, new JsonHttpResponseHandler() {

			@Override
			protected void handleFailureMessage(Throwable arg0, String arg1) {
				handler.onFailure(arg0, arg1);
			}

			@Override
			public void onSuccess(JSONObject json) {
				if (json.has("appliances")) {
					String appliances;
					try {
						appliances = (String) json.getString("appliances");
						handler.onSuccess(appliances.split(","));
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

	public static void stopApp(String appId, final StartStopAppResponseHandler handler) {

		NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
		getHttpClient().setBasicAuth(config.getUsername(), config.getPassword());
		getHttpClient().delete("https://" + config.getHost() + "/domain_basic/spaces/" + config.getAccount() + "/appliances/" + appId + "/components/web", new JsonHttpResponseHandler() {

			@Override
			protected void handleFailureMessage(Throwable arg0, String arg1) {
				handler.onFailure(arg0, arg1);
			}

			@Override
			public void onSuccess(JSONObject json) {
				try {

					handler.onSuccess(json.getString("status"), json.getString("message"));
				} catch (JSONException e) {
					Log.e(e.getMessage());
				}
			}

			@Override
			public void onFailure(Throwable arg0) {
				handler.onFailure(arg0, arg0.getMessage());
			}

		});
	}
	
	public static void startApp(String appId, final StartStopAppResponseHandler handler) {

		NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
		getHttpClient().setBasicAuth(config.getUsername(), config.getPassword());
		getHttpClient().put("https://" + config.getHost() + "/domain_basic/spaces/" + config.getAccount() + "/appliances/" + appId + "/components/web", new JsonHttpResponseHandler() {

			@Override
			protected void handleFailureMessage(Throwable arg0, String arg1) {
				handler.onFailure(arg0, arg1);
			}

			@Override
			public void onSuccess(JSONObject json) {
				try {

					handler.onSuccess(json.getString("status"), json.getString("message"));
				} catch (JSONException e) {
					Log.e(e.getMessage());
				}
			}

			@Override
			public void onFailure(Throwable arg0) {
				handler.onFailure(arg0, arg0.getMessage());
			}
		});
	}
	
	public static void deleteApp(String appId, final StartStopAppResponseHandler handler) {

		NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
		getHttpClient().setBasicAuth(config.getUsername(), config.getPassword());
		getHttpClient().delete("https://" + config.getHost() + "/deploy/"+config.getAccount()+"/appliances/" + appId, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String arg0) {
				
			}


			@Override
			public void onFailure(Throwable arg0) {
				handler.onFailure(arg0, arg0.getMessage());
			}
		});
	}
	
	
}
