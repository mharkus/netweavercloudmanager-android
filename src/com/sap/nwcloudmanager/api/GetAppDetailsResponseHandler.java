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

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public abstract class GetAppDetailsResponseHandler implements BaseHandler {

	private ImageView status;
	private ProgressBar progress;
	private View view;
	
	public GetAppDetailsResponseHandler(){
		
	}
	
	public GetAppDetailsResponseHandler(View view, ImageView status, ProgressBar progress) {
		this.setStatus(status);
		this.setProgress(progress);
		this.setView(view);
	}
	public abstract void onSuccess(AppDetails status);
	public ImageView getStatus() {
		return status;
	}
	public void setStatus(ImageView status) {
		this.status = status;
	}
	public ProgressBar getProgress() {
		return progress;
	}
	public void setProgress(ProgressBar progress) {
		this.progress = progress;
	}
	public View getView() {
		return view;
	}
	public void setView(View view) {
		this.view = view;
	}
}
