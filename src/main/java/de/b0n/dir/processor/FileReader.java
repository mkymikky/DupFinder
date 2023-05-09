package de.b0n.dir.processor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kapselt ein File und stellt darauf eine read()-Operation zur Verfügung. Dient
 * zum effizienten Teilen der Files bei unterschieden in den Streams und
 * gleichzeitigem Halten des Stream-Zustands. Die Datei wird lazy geöffnet.
 */
class FileReader {
	/**
	 * Datei wurde zuende gelesen
	 */
	public static final int FINISHED = -1;
	/**
	 * Beim Lesen der Datei ist ein Fehler aufgetreten
	 */
	public static final int FAILING = -2;

	private final File file;
	private BufferedInputStream stream;

	/**
	 * Packt die Collection von Dateien in jeweils in einen FileStream,
	 * zusammengefasst in einer Queue.
	 * 
	 * @param files
	 *            In FileStreams zu kapselnde Files
	 * @return Queue mit FileStreams
	 */
	public static List<FileReader> pack(Collection<File> files) {
		return files.parallelStream().map(FileReader::new).collect(Collectors.toList());
	}

	/**
	 * Erzeugt das Objekt. Der Stream zum Auslesen wird lazy erst bei Bedarf
	 * geöffnet.
	 * 
	 * @param file File, dessen Stream bearbeitet werden soll
	 */
	public FileReader(File file) {
		if (file == null) {
			throw new IllegalArgumentException("File may not be null.");
		}
		this.file = file;
	}

	/**
	 * Erzeugt das Objekt. Der Stream zum Auslesen wird lazy erst bei Bedarf
	 * geöffnet. Die Methode steht nur Tests zur Verfügung
	 *
	 * @param file File, dessen Stream bearbeitet werden soll
	 */
	protected FileReader(File file, BufferedInputStream stream) {
		if (file == null) {
			throw new IllegalArgumentException("File may not be null.");
		}

		if (stream == null) {
			throw new IllegalArgumentException("Stream may not be null.");
		}
		this.file = file;
		this.stream = stream;
	}

	/**
	 * Schließt den Stream im Adapter und liefert das File.
	 * 
	 * @return zum Stream-Initialisieren genutzes File
	 */
	public File clear() {
		close();
		return file;
	}

	/**
	 * Schließt den Dateistream nach der Analyse oder im Fehlerfall.
	 * 
	 */
	private void close() {
		if (stream == null) {
			return;
		}

		try {
			stream.close();
		} catch (IOException e) {
			throw new IllegalStateException("Could not close Stream. Nothing to do about that, resetting FileStream.");
		} finally {
			stream = null;
		}
	}

	/**
	 * Liefert ein Byte aus dem geöffneten Dateistream zur Inhaltsanalyse.
	 * 
	 * @return Wert gemäß InputStream.read()
	 */
	public int read() {
		int data = FAILING;
		try {
			if (stream == null) {
				stream = new BufferedInputStream(new FileInputStream(file));
			}
			data = stream.read();
		} catch (IOException | IllegalStateException e) {
			close();
		}
		return data;
	}
}