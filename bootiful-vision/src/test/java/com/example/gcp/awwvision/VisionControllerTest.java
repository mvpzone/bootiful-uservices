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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.ModelResultMatchers;

import com.example.gcp.awwvision.VisionController.ImageModel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.common.collect.ImmutableMap;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = { "gcp-bucket-name=bootiful-vision-storage" })
public class VisionControllerTest {
	@MockBean
	private StorageAPI storageAPI;

	@Autowired
	private MockMvc mvc;

	@Value("${gcp-bucket-name}")
	private String bucketName;

	@Before
	public void setUp() throws Exception {
		final String img1 = "obj1";
		final Blob blob1 = Mockito.mock(Blob.class);
		when(blob1.getMediaLink()).thenReturn(getPublicUrl(bucketName, img1));
		when(blob1.getBlobId()).thenReturn(BlobId.of(bucketName, img1));
		when(blob1.getMetadata()).thenReturn(ImmutableMap.of("label", "dog"));

		final String img2 = "obj2";
		final Blob blob2 = Mockito.mock(Blob.class);
		when(blob2.getMediaLink()).thenReturn(getPublicUrl(bucketName, img2));
		when(blob2.getBlobId()).thenReturn(BlobId.of(bucketName, img2));
		when(blob2.getMetadata()).thenReturn(ImmutableMap.of("label", "cat"));

		when(storageAPI.listAll()).thenReturn(Arrays.asList(blob1, blob2));
	}

	@Test
	public void testView() throws Exception {
		final ImageModel img1 = new ImageModel(getPublicUrl(bucketName, "obj1"), "dog");
		final ImageModel img2 = new ImageModel(getPublicUrl(bucketName, "obj2"), "cat");
		final ResultActions getResult = mvc.perform(get("/"));
		final ModelResultMatchers model = model();

		getResult.andExpect(model.attributeExists("images"));
		getResult.andExpect(model.attribute("images", isA(List.class)));
		getResult.andExpect(model.attribute("images", containsInAnyOrder(img1, img2)));
		getResult.andExpect(model.attribute("images", hasItems(img1, img2)));
		model.hasNoErrors();
		model.size(2);
	}

	@Test
	public void testViewLabel() throws Exception {
		final ImageModel dog = new ImageModel(getPublicUrl(bucketName, "obj1"), "dog");
		mvc.perform(get("/label/dog")).andExpect(model().attribute("images", contains(dog)));
	}

	@Test
	public void testViewLabelEmpty() throws Exception {
		mvc.perform(get("/label/octopus")).andExpect(model().attribute("images", empty()));
	}

	static String getPublicUrl(final String bucket, final String object) {
		return String.format("http://storage.googleapis.com/%s/%s", bucket, object);
	}
}
