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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.rpc.Status;

/**
 * Provides request mappings for reading images from Cloud Storage.
 */
@Controller
public class VisionController {

	@Autowired
	private VisionAPI visionAPI;

	@Autowired
	private StorageAPI storageAPI;

	@RequestMapping("/")
	public String view(final Model model) throws IOException {
		final List<ImageModel> images = storageAPI.listAll().stream()
				.map(b -> new ImageModel(b.getMediaLink(), b.getMetadata().get("label"))).collect(Collectors.toList());
		model.addAttribute("images", images);
		return "index";
	}

	@RequestMapping("/label/{label}")
	public String viewLabel(@PathVariable("label") final String label, final Model model) throws IOException {
		final List<ImageModel> images = storageAPI.listAll().stream()
				.filter(b -> label.contains(b.getMetadata().get("label")))
				.map(b -> new ImageModel(b.getMediaLink(), b.getMetadata().get("label"))).collect(Collectors.toList());
		model.addAttribute("images", images);
		return "index";
	}

	@GetMapping("/image")
	public String image(final Model model) {
		return "image";
	}

	@RequestMapping("/vision")
	public String annotate(final Model model, final String imageUrl) throws Exception {
		final Optional<AnnotateImageResponse> annotateResponse = visionAPI.annotateImage(imageUrl);
		annotateResponse.ifPresent(response -> {
			if (response.hasError()) {
				final Status error = response.getError();
				model.addAttribute("annotationError", error.getMessage() + "/" + error.getCode());
			} else {
				model.addAttribute("annotationList", response.getLabelAnnotationsList());
			}
		});
		model.addAttribute("imageUrl", imageUrl);

		return "annotate";
	}

	static class ImageModel {
		private String URL;
		private String label;

		public ImageModel(@Nonnull final String URL, @Nonnull final String label) {
			Objects.requireNonNull(URL, "URL cannot be null.");
			Objects.requireNonNull(label, "Label cannot be null.");
			this.URL = URL;
			this.label = label;
		}

		public String getURL() {
			return URL;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public int hashCode() {
			return URL.hashCode() + label.hashCode();
		}

		@Override
		public boolean equals(final Object object) {
			if (this == object) {
				return true;
			}

			if (object == null) {
				return false;
			}

			if (getClass() != object.getClass()) {
				return false;
			}

			if (!(object instanceof ImageModel)) {
				return false;
			}

			final ImageModel other = (ImageModel) object;
			return other.getURL().equals(getURL()) && other.getLabel().equals(getLabel());
		}

		@Override
		public String toString() {
			return "ImageModel [URL=" + URL + ", label=" + label + "]";
		}
	}
}
