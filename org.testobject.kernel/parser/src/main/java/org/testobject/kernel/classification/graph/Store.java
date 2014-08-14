package org.testobject.kernel.classification.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;
import org.testobject.commons.util.collections.Lists;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.classification.graph.Locator.Node;
import org.testobject.kernel.api.util.VariableUtil;
import org.testobject.kernel.classification.classifiers.Compression;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 * 
 */
public interface Store {

	interface Serializer {
		
		interface Put {
			void put(Locator.Descriptor descriptor, List<Image.Int> masks);
		}

		void serialize(Image.Int raw, Locator.Node locator, OutputStream output, Put put);
		
		class Factory {
			public static Serializer create(final List<Compression.Zip> zips) {
				final Locators.Serializer json = new Locators.Serializer();
				return new Serializer() {
					@Override
					public void serialize(Image.Int raw, Locator.Node locators, OutputStream output, Put put) {
						// masks
						Masks.Serializer.serialize(raw, locators, put);
						
						// compress
						Locator.Node zipped = Features.Zip.zip(locators, zips);
						
						// json
						json.serialize(zipped, output);
					}
				};
			}
		}
	}

	interface Deserializer {
		
		interface Get {
			Image.Int get(int descriptorId, int maskId);
		}

		Locator.Node deserialize(InputStream input);
		
		Locator.Node deserialize(InputStream input, Get get);
		
		class Factory {
			public static Deserializer create(final List<Compression.Unzip> unzips) {
				return new Deserializer() {
					@Override
					public Node deserialize(InputStream input) {
						
						Locators.Deserializer json = new Locators.Deserializer();
						Locator.Node unzipped = json.deserialize(input);
						
						return unzipped;
					}

					@Override
					public Node deserialize(InputStream input, Get get) {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}
		}
	}
	
	interface Features {
		
		class Zip {
			
			public static Locator.Node zip(Locator.Node source, List<Compression.Zip> zips) {
				if(source.getChildren().isEmpty()) {
					List<Locator.Node> childs = Lists.empty();
					return Locator.Node.Factory.create(zip(source.getDescriptor(), zips), childs);
				} else {
					List<Locator.Node> childs = Lists.newArrayList(source.getChildren().size());
					for(Locator.Node child : source.getChildren()) {
						childs.add(zip(child, zips));
					}
					return Locator.Node.Factory.create(zip(source.getDescriptor(), zips), childs);
				}
			}
			
			public static Locator.Descriptor zip(Locator.Descriptor source, List<Compression.Zip> zips) {
				
				Compression.Zip zip = getZip(zips, source.getLabel());
				
				List<Variable<?>> features = zip.zip(source.getLabel(), source.getFeatures());
				
				return Locator.Descriptor.Factory.create(source.getId(), source.getLabel(), features);
			}

			private static Compression.Zip getZip(List<Compression.Zip> zips, Qualifier qualifier) {
				for(Compression.Zip zip : zips) {
					if(zip.supports(qualifier)) {
						return zip;
					}
				}
				
				throw new IllegalArgumentException("no zip registered for qualifier '" + qualifier.toString() + "'");
			}
		}
		
		class Unzip {
			
			public static Locator.Node unzip(Locator.Node source, List<Compression.Unzip> unzips, Classifier.Images images) {
				if(source.getChildren().isEmpty()) {
					List<Locator.Node> childs = Lists.empty();
					return Locator.Node.Factory.create(unzip(source.getDescriptor(), unzips, images), childs);
				} else {
					List<Locator.Node> childs = Lists.newArrayList(source.getChildren().size());
					for(Locator.Node child : source.getChildren()) {
						childs.add(unzip(child, unzips, images));
					}
					return Locator.Node.Factory.create(unzip(source.getDescriptor(), unzips, images), childs);
				}
			}

			private static Locator.Descriptor unzip(Locator.Descriptor source, List<Compression.Unzip> unzips, Classifier.Images images) {
				Compression.Unzip unzip = getUnzip(unzips, source.getLabel());
				
				List<Variable<?>> features = unzip.unzip(source.getLabel(), source.getFeatures(), images);
				
				return Locator.Descriptor.Factory.create(source.getId(), source.getLabel(), features);
			}

			private static Compression.Unzip getUnzip(List<Compression.Unzip> unzips, Qualifier qualifier) {
				for(Compression.Unzip unzip : unzips) {
					if(unzip.supports(qualifier)) {
						return unzip;
					}
				}
				
				throw new IllegalArgumentException("no unzip registered for qualifier '" + qualifier.toString() + "'");
			}
			
		}
	}
	
	interface Masks {
		
		class Serializer {
			
			private static void serialize(Image.Int raw, Locator.Node node, Store.Serializer.Put put) {
				
				List<Mask> masks = VariableUtil.getMasks(node.getDescriptor().getFeatures());
				List<Image.Int> raws = Lists.newArrayList(masks.size());
				
				for(Mask mask : masks) {
					Image.Int cutByMask = BlobUtils.Cut.cutByMask(raw, mask);
					raws.add(cutByMask);
				}
				
				put.put(node.getDescriptor(), raws);
				
				for(Locator.Node child : node.getChildren()) {
					serialize(raw, child, put);
				}
			}
		}
	}
	
	interface Locators {
		
		class Util {
			
			private static final Map<JsonMethod, JsonAutoDetect.Visibility> VISIBILITIES = new HashMap<>();
			static {
				VISIBILITIES.put(JsonMethod.FIELD, Visibility.NONE);
				VISIBILITIES.put(JsonMethod.CREATOR, Visibility.NONE);
				VISIBILITIES.put(JsonMethod.GETTER, Visibility.NONE);
				VISIBILITIES.put(JsonMethod.IS_GETTER, Visibility.NONE);
			}
			
			public static ObjectMapper createMapper() {
				
				ObjectMapper mapper = new ObjectMapper();
				
				for (Map.Entry<JsonMethod, JsonAutoDetect.Visibility> entry : VISIBILITIES.entrySet()) {
					mapper.setVisibility(entry.getKey(), entry.getValue());
				}
				
				return mapper;
			}
			
		}
		
		class Serializer {
			
			private static interface DescriptorMixIn {
				@JsonSerialize(using = MasksSerializer.class)
				List<Mask> getMasks();
			}
			
			private final ObjectMapper mapper = Util.createMapper();
			
			public Serializer() {
				SimpleModule module = new SimpleModule("VariableModule", new Version(1, 0, 0, null));
				module.addSerializer(Variable.class, new VariableSerializer());
				mapper.registerModule(module);
				mapper.getSerializationConfig().addMixInAnnotations(Locator.Descriptor.class, DescriptorMixIn.class);
			}

			public void serialize(Node locator, OutputStream output) {
				try {
					mapper.writerWithDefaultPrettyPrinter().writeValue(output, locator);
				} catch (IOException e) {
					throw new RuntimeException("unable to serialize locator: " + locator.getDescriptor().getLabel(), e);
				}
			}
			
			@SuppressWarnings("rawtypes")
			private static final class VariableSerializer extends JsonSerializer<Variable> {
				@Override
				public void serialize(Variable value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
						JsonProcessingException {
					jgen.writeStartObject();
					jgen.writeFieldName(value.getName());
					jgen.writeObject(value.getValue());
					jgen.writeEndObject();
				}
			}
			
			private static final class MasksSerializer extends JsonSerializer<List<Mask>> {
				@Override
				public void serialize(List<Mask> masks, JsonGenerator jgen, SerializerProvider provider) throws IOException,
						JsonProcessingException {
					jgen.writeStartObject();
					for(int i = 0; i < masks.size(); i++) {
						jgen.writeFieldName(Integer.toString(i));
						jgen.writeObject(masks.get(i).getBoundingBox());
					}
					jgen.writeEndObject();
				}
			}
		}
		
		class Deserializer {
			
			private final ObjectMapper mapper = Util.createMapper();
			
			public Deserializer() {
				SimpleModule module = new SimpleModule("VariableModule", new Version(1, 0, 0, null));
				module.addDeserializer(Variable.class, new VariableDeserializer());
				mapper.registerModule(module);
			}

			public Node deserialize(InputStream input) {
				try {
					return mapper.readValue(input, Locator.Node.class);
				} catch (IOException e) {
					throw new RuntimeException("unable to deserialize locator", e);
				}
			}

			@SuppressWarnings("rawtypes")
			private static final class VariableDeserializer extends JsonDeserializer<Variable> {
				@Override
				public Variable<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
					String fieldName;
					Object value;

					// parser
					{
						jp.nextToken();
						fieldName = jp.getCurrentName();
						jp.nextToken();
						Class<?> type = Variable.Names.Registry.type(fieldName);
						value = jp.readValueAs(type);
						jp.nextToken();
					}

					return Variable.Builder.value(fieldName, value);
				}
			}
		}
	}
}
