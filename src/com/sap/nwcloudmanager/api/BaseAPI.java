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

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;

import com.loopj.android.http.AsyncHttpClient;

public abstract class BaseAPI {
	private static AsyncHttpClient httpClient = new AsyncHttpClient();
	
	protected final static AsyncHttpClient getHttpClient() {
		//HttpHost proxy = new HttpHost("proxy_server", 8080);
		//httpClient.getHttpClient().getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		return httpClient;
	}
}
