package de.b0n.dir.processor;

import java.io.File;

public class FileSize {
	private final File file;
	private final long size;

	public FileSize(final File file, long size) {
		this.file = file;
		this.size = size;
	}

	public long getSize() {
		return size;
	}

	public File getFile() {
		return file;
	}
}
