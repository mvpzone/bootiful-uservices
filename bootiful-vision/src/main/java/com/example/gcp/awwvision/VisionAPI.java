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
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class VisionAPI {
	@Autowired
	private VisionConfig config;

	@Autowired
	private ImageAnnotatorClient imageAnnotator;

	private final Feature labelDetection = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();

	public Optional<AnnotateImageResponse> annotateImage(final String imageUrl) throws IOException {
		log.debug("Annotating Image : {}", imageUrl);
		// Copies the content of the image to memory.
		final ByteString imgBytes = ByteString.copyFrom(config.loadResource(imageUrl));
		log.debug("Downloaded Image bytes : {}", imgBytes.size());

		// Builds the image annotation request
		final Image img = Image.newBuilder().setContent(imgBytes).build();
		final AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(labelDetection).setImage(img)
				.build();

		// Performs label detection on the image file
		final BatchAnnotateImagesResponse batchResponse = imageAnnotator
				.batchAnnotateImages(Collections.singletonList(request));
		final Optional<AnnotateImageResponse> response;
		if (batchResponse.getResponsesCount() > 0) {
			response = Optional.of(batchResponse.getResponsesList().iterator().next());
		} else {
			response = Optional.empty();
		}

		log.info("Annotated Image[{}]: {}", imageUrl, response);
		return response;
	}
}
