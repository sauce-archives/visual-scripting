package org.testobject.commons.util.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;

public class RestClientImpl implements RestClient {
	
	public final static Log log = LogFactory.getLog(RestClientImpl.class);
	
	private final Client client;
	private final WebResource webResource;
	private final AsyncWebResource asyncWebResource;
	private final String url;
	private final MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

	private static Integer READ_TIMEOUT = 5 * 60 * 1000;

	@Inject
	public RestClientImpl(Client client, String url) {
		this.client = client;
		this.client.setReadTimeout(READ_TIMEOUT);
		this.url = url;
		this.webResource = client.resource(url);
		this.asyncWebResource = client.asyncResource(url);
	}

	public RestClientImpl(Client client, WebResource webResource, AsyncWebResource asyncWebResource, String url) {
		this.client = client;
		this.client.setReadTimeout(READ_TIMEOUT);
		this.webResource = webResource;
		this.asyncWebResource = asyncWebResource;
		this.url = url;
	}
	
	@Override
	public <R, A1> R get(String path, Class<R> returnType) {

		try {
			return webResource
					.path(path)
					.queryParams(queryParams)
					.type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.get(returnType);
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}

	@Override
	public <R, A1> R get(String path, TypeReference<R> returnType) {
		try {
			return webResource
					.path(path)
					.queryParams(queryParams)
					.type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.get(new GenericType<R>(returnType.getType()));
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}

	@Override
	public <R, A1> R post(String path, Class<R> returnType) {
		try {
			return webResource
					.path(path)
					.queryParams(queryParams)
					.type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.post(returnType);
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}

	@Override
	public <R, A1> R post(String path, TypeReference<R> returnType) {
		try {
			return webResource
					.path(path)
					.queryParams(queryParams)
					.type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.post(new GenericType<R>(returnType.getType()), returnType);
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}

	@Override
	public <R, A1> R post(String path, A1 requestArg1, Class<R> returnType) {
		try {
			return webResource
					.path(path)
					.queryParams(queryParams)
					.type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.post(returnType, requestArg1);
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}

	@Override
	public <R, A1> R post(String path, A1 requestArg1, TypeReference<R> returnType) {
		try {
			return webResource
					.path(path)
					.queryParams(queryParams)
					.type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.post(new GenericType<R>(returnType.getType()), requestArg1);
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}

	@Override
	public <A1> void post(String path, A1 requestArg1) {
		try {
			webResource
					.path(path)
					.queryParams(queryParams)
					.type(MediaType.APPLICATION_JSON)
					.post(requestArg1);
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}

	@Override
	public void post(String path) {
		try {
			webResource
					.path(path)
					.queryParams(queryParams)
					.post();
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}
	
	@Override
	public void postAsync(String path) {
		try {
			asyncWebResource
					.path(path)
					.queryParams(queryParams)
					.post();
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		}
	}

	@Override
	public <R> R upload(String path, String fileName, InputStream file, Class<R> responseType) {
		try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {

			formDataMultiPart.bodyPart(new StreamDataBodyPart("file", file, fileName));

			return webResource.path(path).type(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON)
					.post(responseType, formDataMultiPart);
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void upload(String path, String fileName, InputStream file) {
		try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {

			formDataMultiPart.bodyPart(new StreamDataBodyPart("file", file, fileName));

			webResource.path(path).type(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON)
					.post(formDataMultiPart);
		} catch (UniformInterfaceException ex) {
			throw logAndRethrowException(ex);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		client.destroy();
	}

	public void setParameter(String key, String value) {
		this.queryParams.putSingle(key, value);
	}

	public void setParameters(MultivaluedMap<String, String> queryParams) {
		this.queryParams.putAll(queryParams);
	}

	@Override
	public RestClient params(MultivaluedMap<String, String> queryParams) {
		RestClientImpl newClient = new RestClientImpl(this.client, this.webResource, this.asyncWebResource, this.url);
		newClient.setParameters(this.queryParams);
		newClient.setParameters(queryParams);
		
		return newClient;
	}

	@Override
	public RestClient param(String key, String value) {
		RestClientImpl newClient = new RestClientImpl(this.client, this.webResource, this.asyncWebResource, this.url);
		newClient.setParameters(this.queryParams);
		newClient.setParameter(key, value);

		return newClient;
	}
	
	private RestClientException logAndRethrowException(UniformInterfaceException ex) {
		try (InputStream in = ex.getResponse().getEntityInputStream()){
			int status = ex.getResponse().getStatus();
			String errorResponse = CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8));
			
			JSONObject jsonException = new JSONObject(errorResponse);
			
			if (jsonException.has("message")) {
				String message = jsonException.getString("message");
				return new RestClientException(message, ex, status);
			}
			
			return new RestClientException("received an error from backend: status " + status + " content:" + errorResponse, ex, status);
		} catch (IOException | JSONException e) {
			throw new RuntimeException("error while deserializing", e);
		}
	}
	
	@Override
	public String toString() {
		return "RestClient: " + url;
	}

}
