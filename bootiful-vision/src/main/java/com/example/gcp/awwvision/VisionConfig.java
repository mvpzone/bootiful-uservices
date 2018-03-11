/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.example.gcp.awwvision;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.common.base.Strings;


/**
 * Sets up connections to client libraries and other injectable beans.
 */
@Configuration
public class VisionConfig {
	@Value("${gcp-application-name}")
	private String applicationName;

	@Value("${gcp-reddit-url:https://www.reddit.com/r/aww/hot.json}")
	private String redditURL = "https://www.reddit.com/r/aww/hot.json";

	@Value("${gcp-bucket-name}")
	private String bucketName;

	@Value("gs://${gcp-bucket-name}")
	private Resource gcsBucket;

	@Value("${gcp-bucket-pagesize:64}")
	private int pageSize;

	@Autowired
	private CredentialsProvider credentialsProvider;

	@Autowired
	private ResourceLoader resourceLoader;

	VisionConfig() {
		final String env = System.getenv("VCAP_SERVICES");
		if (env != null && this.bucketName == null) {
			this.bucketName = new JSONObject(env).getJSONArray("google-storage").getJSONObject(0)
					.getJSONObject("credentials").getString("bucket_name");
		}
	}

	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	public String redditURL() {
		return redditURL;
	}

	public String bucketName() {
		return this.bucketName;
	}

	public Resource bucketResource() {
		return this.gcsBucket;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public byte[] loadResource(final String url) throws IOException {
		// Copies the content of the image to memory.
		return StreamUtils.copyToByteArray(this.resourceLoader.getResource(url).getInputStream());
	}

	@Bean
	JsonFactory jsonFactory() {
		return JacksonFactory.getDefaultInstance();
	}

	@Bean
	Credentials credentials() throws IOException {
		final String env = System.getenv("VCAP_SERVICES");
		if (Strings.isNullOrEmpty(env)) {
			return credentialsProvider.getCredentials();
		}

		final String privateKeyData = new JSONObject(env).getJSONArray("google-storage").getJSONObject(0)
				.getJSONObject("credentials").getString("PrivateKeyData");
		if (Strings.isNullOrEmpty(privateKeyData)) {
			return credentialsProvider.getCredentials();
		}

		final InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(privateKeyData));
		return GoogleCredentials.fromStream(stream);
	}

	@Bean
	@Scope("singleton")
	ImageAnnotatorClient imageAnnotator(final Credentials credential) throws IOException {
		final ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
				.setCredentialsProvider(credentialsProvider).build();
		return ImageAnnotatorClient.create(settings);
	}

	@Bean
	@Scope("singleton")
	Storage storage(final Credentials credential) {
		return StorageOptions.newBuilder().setCredentials(credential).build().getService();
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
