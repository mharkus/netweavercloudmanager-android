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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.nwcloudmanager.api.AppDetails;
import com.sap.nwcloudmanager.api.DeployAPI;
import com.sap.nwcloudmanager.api.StartStopAppResponseHandler;

public class AppDetailsActivity extends GATrackingActivity {
	private TextView applianceId;
	private TextView status;
	private TextView domain;
	private TextView accessPoints;
	private Button action;
	private Button undeploy;
	private TextView domainLabel;
	private TextView accessPointLabel;
	private Button logs;
	private ImageView refreshButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_appdetails);
		bindLogout();

		final AppDetails details = (AppDetails) getIntent().getSerializableExtra("details");

		applianceId = (TextView) findViewById(R.id.applianceId);
		status = (TextView) findViewById(R.id.status);
		domain = (TextView) findViewById(R.id.domain);
		domainLabel = (TextView) findViewById(R.id.domainLabel);
		accessPoints = (TextView) findViewById(R.id.accessPoints);
		accessPointLabel = (TextView) findViewById(R.id.accessPointLabel);
		action = (Button) findViewById(R.id.action);
		undeploy = (Button) findViewById(R.id.undeploy);
		logs = (Button) findViewById(R.id.getLogs);
		refreshButton = (ImageView) findViewById(R.id.refresh);

		logs.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AppDetailsActivity.this, LogsActivity.class);
				intent.putExtra("appId", details.getApplianceId());
				startActivity(intent);

				overridePendingTransition(R.anim.activityincoming, R.anim.activityoutgoing);
			}
		});

		undeploy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				new AlertDialog.Builder(new ContextThemeWrapper(AppDetailsActivity.this, R.style.ModifiedDialog)).setTitle("Confirmation").setMessage("Are you sure you want to do this?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						DeployAPI.deleteApp(details.getApplianceId(), new StartStopAppResponseHandler() {

							@Override
							public void onFailure(Throwable arg0, String message) {
								new AlertDialog.Builder(new ContextThemeWrapper(AppDetailsActivity.this, R.style.ModifiedDialog)).setInverseBackgroundForced(true).setTitle("Error").setMessage(arg0.getLocalizedMessage()).setPositiveButton("Ok", null).create().show();
							}

							@Override
							public void onSuccess(String status, String message) {
							}
						});

						Intent data = new Intent();
						data.putExtra("appId", details.getApplianceId());
						setResult(MainActivity.REFRESH, data);
						overridePendingTransition(R.anim.activityincoming, R.anim.activityoutgoing);

						finish();

					}
				}).setNegativeButton("No", null).create().show();

			}
		});

		action.setVisibility(View.VISIBLE);
		undeploy.setVisibility(View.VISIBLE);

		applianceId.setText(details.getApplianceId());
		status.setText(details.getStatus());

		if (!details.getStatus().equalsIgnoreCase("stopped")) {

			domain.setVisibility(View.VISIBLE);
			domainLabel.setVisibility(View.VISIBLE);

			accessPoints.setVisibility(View.VISIBLE);
			accessPointLabel.setVisibility(View.VISIBLE);

			domain.setText(details.getDomain());
			if (details.getAccessPoints() != null) {
				accessPoints.setText(details.getAccessPoints()[0]);
				Linkify.addLinks(accessPoints, Linkify.WEB_URLS);
			}
		} else {
			domain.setVisibility(View.GONE);
			domainLabel.setVisibility(View.GONE);
			accessPoints.setVisibility(View.GONE);
			accessPointLabel.setVisibility(View.GONE);
		}

		if (details.getStatus().equalsIgnoreCase("started") || details.getStatus().equalsIgnoreCase("starting") || details.getStatus().equalsIgnoreCase("error")) {
			action.setText("Stop");
			action.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showProgress();
					action.setEnabled(false);
					DeployAPI.stopApp(details.getApplianceId(), new StartStopAppResponseHandler() {

						@Override
						public void onFailure(Throwable arg0, String message) {
							hideProgress();
							new AlertDialog.Builder(new ContextThemeWrapper(AppDetailsActivity.this, R.style.ModifiedDialog)).setTitle("Error").setMessage(arg0.getLocalizedMessage()).setPositiveButton("OK", null).create().show();

						}

						@Override
						public void onSuccess(String status, String message) {
							hideProgress();
							new AlertDialog.Builder(new ContextThemeWrapper(AppDetailsActivity.this, R.style.ModifiedDialog)).setTitle("Status").setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent data = new Intent();
									data.putExtra("appId", details.getApplianceId());
									setResult(MainActivity.REFRESH, data);
									finish();

									overridePendingTransition(R.anim.activityincoming, R.anim.activityoutgoing);
								}
							}).create().show();

						}
					});

				}
			});

			undeploy.setVisibility(View.GONE);
		} else if (details.getStatus().equalsIgnoreCase("stopped")) {
			action.setText("Start");
			undeploy.setVisibility(View.VISIBLE);

			action.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showProgress();
					action.setEnabled(false);
					DeployAPI.startApp(details.getApplianceId(), new StartStopAppResponseHandler() {

						@Override
						public void onFailure(Throwable arg0, String message) {
							hideProgress();
							new AlertDialog.Builder(new ContextThemeWrapper(AppDetailsActivity.this, R.style.ModifiedDialog)).setTitle("Error").setMessage(arg0.getLocalizedMessage()).setPositiveButton("OK", null).create().show();

						}

						@Override
						public void onSuccess(String status, String message) {
							hideProgress();
							new AlertDialog.Builder(new ContextThemeWrapper(AppDetailsActivity.this, R.style.ModifiedDialog)).setTitle("Status").setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent data = new Intent();
									data.putExtra("appId", details.getApplianceId());
									setResult(MainActivity.REFRESH, data);
									finish();

									overridePendingTransition(R.anim.activityincoming, R.anim.activityoutgoing);
								}
							}).create().show();

						}
					});

				}
			});
		} else if (details.getStatus().equalsIgnoreCase("stopping")) {
			action.setVisibility(View.GONE);
			undeploy.setVisibility(View.GONE);
		}
	}

	protected void showProgress() {
		refreshButton.startAnimation(loadAnimation);
	}

	private void hideProgress() {
		loadAnimation.cancel();
	}

}
