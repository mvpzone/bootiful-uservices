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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobField;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Storage.BlobTargetOption;
import com.google.cloud.storage.Storage.PredefinedAcl;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper methods for interacting with the Cloud Storage API.
 * 
 * Uses the Cloud Storage Bucket configured in the application properties.
 */
@Component
@Slf4j
public class StorageAPI {
	@Autowired
	private VisionConfig config;

	@Autowired
	private Storage storageService;

	public void uploadJpeg(final String name, final String url, final Map<String, String> metadata) throws IOException {
		final BlobInfo blobInfo = BlobInfo.newBuilder(config.bucketName(), name).setContentType("image/jpeg").build();
		final Blob blob = storageService.create(blobInfo, config.loadResource(url),
				BlobTargetOption.predefinedAcl(PredefinedAcl.PUBLIC_READ));
		log.debug("Uploaded ImageLink: [{}] to Bucket: [{}], Name: [{}]", blob.getMediaLink(), blob.getBucket(),
				blob.getName());
	}

	public List<Blob> listAll() {
		final Page<Blob> blobPage = storageService.list(config.bucketName(),
				BlobListOption.pageSize(config.getPageSize()), BlobListOption.fields(BlobField.values()));
		final Iterable<Blob> bucketIterator = blobPage.iterateAll();
		if (bucketIterator.iterator().hasNext()) {
			final List<Blob> blobList = StreamSupport.stream(bucketIterator.spliterator(), false)
					.filter(b -> !b.isDirectory()).collect(Collectors.toList());
			log.info("Bucket [{}] list : {}", config.bucketName(), blobList.size());
			return Collections.unmodifiableList(blobList);
		} else {
			log.info("Bucket [{}] list : {}", config.bucketName(), 0);
			return Collections.emptyList();
		}
	}

	public Blob getBlob(final String name) {
		final Blob blob = storageService.get(BlobId.of(config.bucketName(), name));
		log.info("Got blob : {}", blob);
		return blob;
	}
}
