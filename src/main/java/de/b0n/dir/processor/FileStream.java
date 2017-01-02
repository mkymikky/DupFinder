package de.b0n.dir.processor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class FileStream {

	private final File file;
	private BufferedInputStream stream;

	public FileStream(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public BufferedInputStream getStream() {
		if (stream == null) {
			try {
				stream = new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				this.close();
				throw new IllegalStateException(e);
			}
		}
		return stream;
	}
	
	public void close() {
		if (stream != null) {
			try {
				stream.close();
				stream = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}