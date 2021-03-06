/*
 * Copyright (C) 2014 Andrei Varabyeu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.restendpoint.http;

import com.epam.reportportal.restendpoint.http.proxy.RestEndpointInvocationHandler;
import com.epam.reportportal.restendpoint.serializer.ByteArraySerializer;
import com.epam.reportportal.restendpoint.serializer.Serializer;
import com.epam.reportportal.restendpoint.serializer.TextSerializer;
import com.epam.reportportal.restendpoint.serializer.json.GsonSerializer;
import com.google.common.collect.Lists;
import com.google.common.reflect.Reflection;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.util.List;

/**
 * Builders for {@link RestEndpoint}
 *
 * @author avarabyeu
 */
public final class RestEndpoints {

	/**
	 * No need to create instance
	 */
	private RestEndpoints() {
	}

	/**
	 * Creates default {@link RestEndpoint} for provided endpoint URL.
	 * Adds {@link DefaultErrorHandler} and all possible serializers
	 *
	 * @return created RestEndpoint
	 */
	public static RestEndpoint createDefault() {
		return new HttpClientRestEndpoint(
				HttpClients.createDefault(),
				Lists.newArrayList(new TextSerializer(), new ByteArraySerializer(), new GsonSerializer()),
				new DefaultErrorHandler()
		);
	}

	/**
	 * Creates default {@link RestEndpoint} for provided endpoint URL.
	 * Adds {@link DefaultErrorHandler} and all possible serializers
	 *
	 * @param endpointUrl Base endpoint URL
	 * @return created RestEndpoint
	 */
	public static RestEndpoint createDefault(String endpointUrl) {
		return new HttpClientRestEndpoint(
				HttpClients.createDefault(),
				Lists.newArrayList(new TextSerializer(), new ByteArraySerializer(), new GsonSerializer()),
				new DefaultErrorHandler(),
				endpointUrl
		);
	}

	/**
	 * Creates interface implementation (via proxy) of provided class using RestEndpoint as rest client
	 * <b>Only interfaces are supported!</b>
	 *
	 * @param clazz    - interface to be proxied
	 * @param endpoint - RestEndpoint to be used as rest client
	 * @param <T>      - Type of interface to be proxied
	 * @return interface implementation (e.g.) just proxy
	 */
	public static <T> T forInterface(Class<T> clazz, RestEndpoint endpoint) {
		return Reflection.newProxy(clazz, new RestEndpointInvocationHandler(clazz, endpoint));
	}

	/**
	 * Creates default builder which uses Apache Http Commons client as endpoint implementation
	 *
	 * @return New Builder instance
	 */
	public static Builder create() {
		return new Builder();
	}

	/**
	 * Builder for {@link RestEndpoint}
	 */
	public static class Builder {

		private final List<Serializer> serializers;

		private final HttpClientBuilder httpClientBuilder;

		private HttpClient httpClient;

		private ErrorHandler errorHandler;

		private String endpointUrl;

		/**
		 * Default RestEndpoints builder
		 */
		Builder() {
			this.serializers = Lists.newArrayList();
			this.httpClientBuilder = HttpClientBuilder.create();
		}

		/**
		 * Build {@link RestEndpoint}
		 *
		 * @return Built RestEndpoint
		 */
		public final RestEndpoint build() {
			HttpClient closeableHttpClient;
			if (null == httpClient) {
				closeableHttpClient = httpClientBuilder.build();
			} else {
				closeableHttpClient = httpClient;
			}

			return new HttpClientRestEndpoint(closeableHttpClient, serializers, errorHandler, endpointUrl);
		}

		public final Builder withBaseUrl(String url) {
			this.endpointUrl = url;
			return this;
		}

		public final Builder withErrorHandler(ErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
			return this;
		}

		public final Builder withSerializer(Serializer serializer) {
			this.serializers.add(serializer);
			return this;
		}

		/**
		 * Uses provided {@link HttpClient}
		 * <b>May override some configuration methods like {@link #withBasicAuth(String, String)}</b>
		 *
		 * @param httpClient Apache HTTP client
		 * @return this Builder
		 */
		public final Builder withHttpClient(HttpClient httpClient) {
			this.httpClient = httpClient;
			return this;
		}

		/**
		 * Adds Preemptive Basic authentication to the client
		 *
		 * @param username Username
		 * @param password Password
		 * @return this Builder
		 */
		public final Builder withBasicAuth(String username, String password) {
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
			httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			httpClientBuilder.addInterceptorFirst(new PreemptiveAuthInterceptor());
			return this;
		}

		/**
		 * Adds Keystore for SSL
		 *
		 * @param keyStore     KeyStore input stream
		 * @param keyStorePass KeyStore password
		 * @return This builder
		 */
		public final Builder withSsl(InputStream keyStore, String keyStorePass) {
			SSLContext sslcontext;
			try {
				sslcontext = org.apache.http.ssl.SSLContexts.custom()
						.loadTrustMaterial(IOUtils.loadKeyStore(keyStore, keyStorePass), null)
						.build();
			} catch (Exception e) {
				throw new IllegalArgumentException("Unable to load trust store", e);
			}

            /*
			 * Unreal magic, but we can't use
			 * org.apache.http.conn.ssl.SSLConnectionSocketFactory
			 * .BROWSER_COMPATIBLE_HOSTNAME_VERIFIER here due to some problems
			 * related to classloaders. Initialize host name verifier explicitly
			 */
			httpClientBuilder.setSSLContext(sslcontext).setSSLHostnameVerifier(new DefaultHostnameVerifier());

			return this;
		}

		/**
		 * Builds RestEndpoints and created proxy implementation for provided class
		 * <b>Only interfaces are supported!</b>
		 *
		 * @param clazz - interface to be proxied
		 * @param <T>   - type of interface to be proxied
		 * @return - interface implementation based on proxy
		 */
		public final <T> T forInterface(Class<T> clazz) {
			return RestEndpoints.forInterface(clazz, build());
		}

	}

}
