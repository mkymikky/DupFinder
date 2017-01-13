package de.b0n.dir.processor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Kapselt File und zugehörigen Stream in einem Objekt.
 * Dient zum effizienten Teilen der Files bei unterschieden in den Streams und gleichzeitigem Halten des Stream-Zustands.
 * Die Datei wird erstmalig beim ersten Zugriff auf dessen Inhalt geöffnet. *
 */
class FileStream {

	private final File file;
	private BufferedInputStream stream;

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
				throw new IllegalStateException("Could not find designated File.", e);
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
				stream = null;
			} catch (IOException e) {
				stream = null;
				throw new IllegalStateException("Could not close Stream. Nothing to do about that, resetting Stream.");
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
			throw new IllegalStateException("Stream had Exception, closed.", e);
		}
	}
}