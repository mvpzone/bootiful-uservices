package com.example.volume;

public class StorageException extends RuntimeException {
	private static final long serialVersionUID = -2423325050105844514L;

	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
