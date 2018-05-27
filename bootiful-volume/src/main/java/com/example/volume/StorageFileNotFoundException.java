package com.example.volume;

public class StorageFileNotFoundException extends StorageException {
	private static final long serialVersionUID = 5797562888036908434L;

	public StorageFileNotFoundException(String message) {
		super(message);
	}

	public StorageFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}