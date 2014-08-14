package org.testobject.commons.util.rest;

import java.io.Closeable;
import java.io.InputStream;

import javax.ws.rs.core.MultivaluedMap;

import org.codehaus.jackson.type.TypeReference;

public interface RestClient extends Closeable {

	interface Factory {

		RestClient open(String url);

	}

	class Endpoint {
		
		public final String url;
		
		public Endpoint(String host, int port) {
			this.url = host + ":" + port;
		}

		public Endpoint(String url) {
			this.url = url;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Endpoint other = (Endpoint) obj;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return url;
		}
	}

	@SuppressWarnings("serial")
	class RestClientException extends RuntimeException {
		
		private final int status;
		
		public RestClientException(String message, Throwable cause, int status) {
			super(message, cause);
			this.status = status;
		}
		
		public int getStatus() {
			return status;
		}
	}
	
	<R, A1> R get(String path, Class<R> returnType);

	<R, A1> R get(String path, TypeReference<R> returnType);

	<R, A1> R post(String path, Class<R> returnType);

	<R, A1> R post(String path, TypeReference<R> returnType);

	<R, A1> R post(String path, A1 requestArg1, Class<R> returnType);

	<R, A1> R post(String path, A1 requestArg1, TypeReference<R> returnType);

	<A1> void post(String path, A1 requestArg1);

	void post(String path);
	
	void postAsync(String path);

	RestClient params(MultivaluedMap<String, String> queryParams);

	RestClient param(String key, String value);

	<R> R upload(String path, String fileName, InputStream file, Class<R> responseType);
	
	void upload(String path, String fileName, InputStream file);

	void close();

}
