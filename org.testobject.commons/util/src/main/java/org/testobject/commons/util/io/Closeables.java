package org.testobject.commons.util.io;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author enijkamp
 *
 */
public class Closeables {

	private static final Log log = LogFactory.getLog(Closeables.class);

	public static class Factory {
		public static Closeable stub() {
			return new Closeable() {
				@Override
				public void close() throws IOException {

				}
			};
		}
	}

	public static void close(Closeable closeable) {

		if (closeable == null) {
			return;
		}

		try {
			closeable.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void closeQuietly(Closeable closeable) {

		if (closeable == null) {
			return;
		}

		try {
			closeable.close();
		} catch (Throwable e) {
			log.warn(e.getMessage(), e);
		}
	}

	public static void close(Closable closeable) {

		if (closeable == null) {
			return;
		}

		closeable.close();
	}

	public static Closeable stub() {
		return new Closeable() {
			@Override
			public void close() throws IOException {}
		};
	}

}
