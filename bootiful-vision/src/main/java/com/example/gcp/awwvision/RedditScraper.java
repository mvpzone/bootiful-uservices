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

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.example.gcp.awwvision.RedditResponse.Listing;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.common.collect.ImmutableMap;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides a request mapping for scraping images from reddit, labeling them
 * with the Vision API, and storing them in Cloud Storage.
 */
@Controller
@Slf4j
public class RedditScraper {
	@Autowired
	private VisionConfig config;

	@Autowired
	private VisionAPI visionAPI;

	@Autowired
	private StorageAPI storageAPI;

	@Value("${reddit-user-agent}")
	private String redditUserAgent;

	@RequestMapping("/reddit")
	public String getRedditUrls(final Model model, final RestTemplate restTemplate) throws IOException {
		final HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.USER_AGENT, redditUserAgent);
		final RedditResponse response = restTemplate
				.exchange(config.redditURL(), HttpMethod.GET, new HttpEntity<String>(headers), RedditResponse.class)
				.getBody();
		storeAndLabel(response);
		log.info("Reddit Reponse Items: {}", response.data.children.length);
		log.debug("Reddit Response: {}", response);

		return "reddit";
	}

	void storeAndLabel(final RedditResponse response) throws IOException {
		for (Listing listing : response.data.children) {
			// Ignore invalid listings and filter out self posts
			if (listing.data == null || listing.data.preview == null) {
				continue;
			}

			for (RedditResponse.Image img : listing.data.preview.images) {
				final String url = img.source.url;
				try {
					final Optional<AnnotateImageResponse> annotateResponse = visionAPI.annotateImage(url);
					annotateResponse.ifPresent(r -> {
						final ImmutableMap<String, String> metaData;
						if (r.hasError()) {
							final int code = r.getError().getCode();
							final String message = r.getError().getMessage();
							metaData = ImmutableMap.of("label", code + "/" + message);
							log.warn("Annotation Error, Image: {}, Code: {}, Message: {}", url, code, message);
						} else {
							final StringBuilder label = new StringBuilder();
							for (EntityAnnotation annotation : r.getLabelAnnotationsList()) {
								label.append('[').append(annotation.getDescription()).append(":")
										.append(annotation.getScore()).append("],");
							}
							metaData = ImmutableMap.of("label", label.toString());
							log.info("Annotated Image: {}, Code: {}, Message: {}", url, label);
						}

						try {
							storageAPI.uploadJpeg(img.id + ".jpg", url, metaData);
						} catch (IOException e) {
							log.warn("Issue in uploading image " + url, e);
						}
					});
				} catch (Exception e1) {
					log.warn("Issue in streaming image " + url, e1);
				}
			}
		}
	}
}
