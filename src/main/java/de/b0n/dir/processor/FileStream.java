package de.b0n.dir.processor;

import com.github.funthomas424242.unmodifiable.UnmodifiableQueue;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Kapselt File und zugehörigen Stream in einem Objekt.
 * Dient zum effizienten Teilen der Files bei unterschieden in den Streams und gleichzeitigem Halten des Stream-Zustands.
 * Die Datei wird erstmalig beim ersten Zugriff auf dessen Inhalt geöffnet. *
 */
class FileStream {

	private final File file;
	private BufferedInputStream stream;

	protected static Set<File> fileHandles=new HashSet<>();

//	public static File newFile(final String path){
//	    final int count=fileHandles.size();
//
//		final File file = new File(path);
//		fileHandles.add(file);
//	}

	/**
	 * Packt die Collection von Dateien in jeweils in einen FileStream, zusammengefasst in einer Queue.
	 * @param files In FileStreams zu kapselnde Files
	 * @return Queue mit FileStreams
	 */
	public static UnmodifiableQueue<FileStream> pack(Iterator<File> files) {
		UnmodifiableQueue<FileStream> fileStreams = new UnmodifiableQueue<>();
		while(files.hasNext()){
			fileStreams=fileStreams.addElement(new FileStream(files.next()));
		}
		return fileStreams;
	}

	public static Queue<File> extract(Collection<FileStream> fileStreams) {
		Queue<File> filesQueue = new ConcurrentLinkedQueue<File>();
		for (FileStream fileStream : fileStreams) {
			filesQueue.add(fileStream.getFile());
		}
		return filesQueue;
	}

	public static UnmodifiableQueue<File> convertListOfFileStreamToListOfFiles(Iterator<FileStream> fileStreams) {
		UnmodifiableQueue<File> filesQueue = new UnmodifiableQueue();
		while(fileStreams.hasNext()){
			filesQueue=filesQueue.addElement(fileStreams.next().getFile());
		}
		return filesQueue;
	}

	/**
	 * Schließt alle Streams der im FileStream hinterlegten Dateien.
	 * @param fileStreams zu schließende FileStreams
	 */
	public static void closeAll(Collection<FileStream> fileStreams) {
		for (FileStream fileStream : fileStreams) {
			fileStream.close();
		}
	}

	/**
	 * Schließt alle Streams der im FileStream hinterlegten Dateien.
	 * @param fileStreams zu schließende FileStreams
	 */
	public static void closeAll(Iterator<FileStream> fileStreams) {
		while(fileStreams.hasNext()) {
			fileStreams.next().close();
		}
	}

	/**
	 * Erzeugt das Objekt.
	 * Der Stream zum Auslesen wird lazy erst bei Bedarf geöffnet.
	 * @param file 
	 */
	public FileStream(File file) {
		if (file == null) {
			throw new IllegalArgumentException("File may not be null.");
		}
		this.file = file;
	}

	/**
	 * Nach der Stream-Verarbeitung soll das Ursprungsfile weiterverarbeitet werden können.
	 * @return zum Stream-Initialisieren genutzes File
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Öffnet den Dateistream beim ersten Zugriff.
	 * @return gepufferter InputStream des File
	 */
	private BufferedInputStream getStream() {
		if (stream == null) {
			try {
				stream = new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				this.close();
				throw new IllegalStateException("Could not open designated File: " + file.getAbsolutePath() + " Reason: " + e.getMessage(), e);
			}
		}
		return stream;
	}
	
	/**
	 * Schließt den Dateistream nach der Analyse oder im Fehlerfall.
	 */
	public void close() {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				throw new IllegalStateException("Could not close Stream. Nothing to do about that, resetting FileStream.");
			} finally {
				stream = null;
			}
		}
	}

	/**
	 * Liefert ein Byte aus dem geöffneten Dateistream zur Inhaltsanalyse.
	 * @see InputStream.read()
	 * @return Wert gemäß InputStream.read()
	 */
	public int read() {
		try {
			return getStream().read();
		} catch (IOException e) {
			this.close();
			throw new IllegalStateException("Stream of " + file.getAbsolutePath() + " could not be read: " + e.getMessage(), e);
		}
	}
}