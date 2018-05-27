package com.example.volume;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Controller
public class FileUploadController {

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@RequestMapping("/path")
	@ResponseBody
	public String info() {
		return storageService.config();
	}

	@RequestMapping("/files")
	@ResponseBody
	public List<String> listUploadedFiles() throws IOException {
		return storageService.loadAll()
				.map(path -> MvcUriComponentsBuilder
						.fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString()).build()
						.toString())
				.collect(Collectors.toList());
	}

	@RequestMapping("/file")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@RequestParam String filename) {

		Resource file = storageService.loadAsResource(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@RequestMapping(value = "/upload")
	@ResponseBody
	public Map<String, List<Map<String, String>>> handleFileUpload(MultipartHttpServletRequest request) {
		Map<String, List<Map<String, String>>> result = new HashMap<>();
		Iterator<Entry<String, MultipartFile>> i = request.getFileMap().entrySet().iterator();
		List<Map<String, String>> files = new ArrayList<>();
		while (i.hasNext()) {
			Map.Entry<String, MultipartFile> me = i.next();
			String fileName = me.getKey();
			MultipartFile multipartFile = me.getValue();
			storageService.store(multipartFile);

			Map<String, String> f = new HashMap<>();
			f.put("name", fileName);
			f.put("size", String.valueOf(multipartFile.getSize()));
			f.put("url", "");
			f.put("thumbnailUrl", "");
			f.put("deleteUrl", "");
			f.put("deletetype", "DELETE");
			files.add(f);
		}
		result.put("files", files);
		return result;
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

	@Autowired
	private RestTemplate _template;

	@RequestMapping(value = "/sleuth")
	@ResponseBody
	public List<?> sleuth(String url) {
		return _template.getForObject(url, List.class);
	}

}