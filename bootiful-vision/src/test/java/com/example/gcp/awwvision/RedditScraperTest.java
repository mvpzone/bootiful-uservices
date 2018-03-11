/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.gcp.awwvision;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.gcp.awwvision.RedditResponse.Data;
import com.example.gcp.awwvision.RedditResponse.Image;
import com.example.gcp.awwvision.RedditResponse.Listing;
import com.example.gcp.awwvision.RedditResponse.ListingData;
import com.example.gcp.awwvision.RedditResponse.Preview;
import com.google.auth.Credentials;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.common.collect.ImmutableMap;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = { "gcp-storage-bucket=fake-bucket" })
public class RedditScraperTest {
	@MockBean
	VisionAPI visionAPI;

	@MockBean
	StorageAPI storageAPI;

	// Even though this is not used directly in the test, mock it out so the
	// application doesn't try
	// to read environment variables to set the credential.
	@MockBean
	Credentials credentials;

	@Spy
	@Autowired
	RedditScraper scraper;

	@MockBean
	VisionConfig config;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		when(storageAPI.getBlob(Matchers.anyString())).thenReturn(null);
		doNothing().when(storageAPI).uploadJpeg(Matchers.anyString(), Matchers.anyString(), Matchers.anyMap());

		// Have the Vision API return "dog" for any request.
		final EntityAnnotation dogAnnotation = EntityAnnotation.newBuilder().setDescription("dog").setScore(1.0f)
				.build();
		final AnnotateImageResponse annotateResponse = AnnotateImageResponse.newBuilder()
				.addAllLabelAnnotations(Collections.singleton(dogAnnotation)).build();
		when(visionAPI.annotateImage(Matchers.anyString())).thenReturn(Optional.of(annotateResponse));

		doReturn(new byte[0]).when(config).loadResource(Matchers.any(String.class));
	}

	@Test
	public void testScrape() throws Exception {
		final Image img1 = new Image(new RedditResponse.Source("http://url1"), "img1");
		final Image img2 = new Image(new RedditResponse.Source("http://url2"), "img2");
		final RedditResponse redditResponse = new RedditResponse(
				new Data(new Listing[] { new Listing(new ListingData(new Preview(new Image[] { img1, img2 }))) }));

		scraper.storeAndLabel(redditResponse);

		verify(storageAPI).uploadJpeg("img1.jpg", "http://url1", ImmutableMap.of("label", "[dog:1.0],"));
		verify(storageAPI).uploadJpeg("img2.jpg", "http://url2", ImmutableMap.of("label", "[dog:1.0],"));
	}

	@Test
	public void testSelfPosts() throws Exception {
		final Image img1 = new Image(new RedditResponse.Source("http://url1"), "img1");
		final RedditResponse redditResponse = new RedditResponse(new Data(new Listing[] {
				new Listing(new ListingData(new Preview(new Image[] { img1 }))), new Listing(new ListingData()), }));

		scraper.storeAndLabel(redditResponse);
		verify(storageAPI).uploadJpeg("img1.jpg", "http://url1", ImmutableMap.of("label", "[dog:1.0],"));
	}
}
