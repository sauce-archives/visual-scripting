package org.testobject.kernel.api.classification.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.collections.Lists;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public interface Variable<T> {
	
	@JsonProperty("name")
	String getName();
	
	Class<T> getType();
	
	@JsonProperty("value")
	T getValue();
	
	void bind(T value);
	
	// FIXME align resp. use proper names (en)
	interface Names {
		
		interface Depiction {
			
			@As(Mask.class) 
			String masks = "depiction.masks";
			
			@As(org.testobject.kernel.imaging.procedural.Node.class) 
			String graph = "depiction.graph";
			
			@As(Contour.class) 
			String contours = "depiction.contours";
			
			@As(Color.class) 
			String fill = "depiction.color.fill";
			
			@As(ImageFingerprint.class) 
			String fingerprint = "depiction.fingerprint";
		}
		
		interface Geometric {
			
			@As(Size.Int.class) 
			String size = "geometric.size";
			
			@As(Point.Int.class) 
			String position = "geometric.position";
		}
		
		@Retention(RetentionPolicy.RUNTIME)
		@Target({ ElementType.FIELD })
		public @interface As {
			Class<?> value();
		}
		
		static final class Registry {
			private static final Map<String, Class<?>> types = new HashMap<>();
			static {
				inspect(Depiction.class);
				inspect(Geometric.class);
			}
			
			private static void inspect(Class<?> cls) {
                try {
                    for(Field field : cls.getFields()) {
                        types.put(field.get(null).toString(), field.getAnnotation(As.class).value());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
			}
			
			public static Class<?> type(String name) {
				return types.get(name);
			}
		}
		
	}
	
	interface Text extends Variable<java.lang.String> {
		
	}
	
	interface Integer extends Variable<java.lang.Integer> {
		
	}
	
	interface Float extends Variable<java.lang.Float> {
		
	}
	
	interface Double extends Variable<java.lang.Double> {
		
	}
	
	class Builder {
		
		public static class Variables {
		
			private List<Variable<?>> variables = Lists.newLinkedList();
			
			public Variables point(String name, org.testobject.commons.math.algebra.Point.Int point) {
				variables.add(new Impl.Generic<org.testobject.commons.math.algebra.Point.Int>(name, point));
				return this;
			}
			
			public Variables size(String name, org.testobject.commons.math.algebra.Size.Int size) {
				variables.add(new Impl.Generic<org.testobject.commons.math.algebra.Size.Int>(name, size));
				return this;
			}
			
			public Variables value(String name, double value) {
				variables.add(new Impl.Double(name, value));
				return this;
			}
			
			public Variables color(String name, Color color) {
				variables.add(new Impl.Generic<Color>(name, color));
				return this;
			}
			
			public List<Variable<?>> build() {
				return variables;
			}
		}
		
		public static Builder.Variables variables() {
			return new Builder.Variables();
		}
		
		public static Variable<org.testobject.commons.math.algebra.Point.Int> point(String name, org.testobject.commons.math.algebra.Point.Int point) {
			return new Impl.Generic<org.testobject.commons.math.algebra.Point.Int>(name, point);
		}
		
		public static Variable<org.testobject.commons.math.algebra.Rectangle.Int> rect(String name, org.testobject.commons.math.algebra.Rectangle.Int rect) {
			return new Impl.Generic<org.testobject.commons.math.algebra.Rectangle.Int>(name, rect);
		}
		
		public static Variable<org.testobject.commons.math.algebra.Size.Int> size(String name, org.testobject.commons.math.algebra.Size.Int size) {
			return new Impl.Generic<org.testobject.commons.math.algebra.Size.Int>(name, size);
		}
		
		public static <T> Variable<T> value(String name, T value) {
			return new Impl.Generic<T>(name, value);
		}
		
		public static Variable<Color> color(String name, Color color) {
			return new Impl.Generic<Color>(name, color);
		}
		
		interface Impl {
		
			class Integer implements Variable.Integer {
				
				private java.lang.String name = null;
				private java.lang.Integer value = null;
				
				public Integer(java.lang.String name, java.lang.Integer value) {
					this.name = name;
					this.value = value;
				}
	
				@Override
				public java.lang.Integer getValue() {
					return value;
				}
				
				@Override
				public void bind(java.lang.Integer value) {
					this.value = value;
				}
	
				@Override
				public String getName() {
					if(name == null) {
						throw new IllegalStateException();
					}
					return name;
				}

				@Override
				public Class<java.lang.Integer> getType() {
					return java.lang.Integer.class;
				}
			}
			
			class Float implements Variable.Float {
				
				private java.lang.String name = null;
				private java.lang.Float value = null;
				
				public Float(java.lang.String name, java.lang.Float value) {
					this.name = name;
					this.value = value;
				}
	
				@Override
				public java.lang.Float getValue() {
					return value;
				}
				
				@Override
				public void bind(java.lang.Float value) {
					this.value = value;
				}
				
				@Override
				public String getName() {
					if(name == null) {
						throw new IllegalStateException();
					}
					return name;
				}
				
				@Override
				public Class<java.lang.Float> getType() {
					return java.lang.Float.class;
				}
			}
			
			class Double implements Variable.Double {
				
				private java.lang.String name = null;
				private java.lang.Double value = null;
				
				public Double(java.lang.String name, java.lang.Double value) {
					this.name = name;
					this.value = value;
				}
	
				@Override
				public java.lang.Double getValue() {
					return value;
				}
				
				@Override
				public void bind(java.lang.Double value) {
					this.value = value;
				}
				
				@Override
				public String getName() {
					if(name == null) {
						throw new IllegalStateException();
					}
					return name;
				}
				
				@Override
				public Class<java.lang.Double> getType() {
					return java.lang.Double.class;
				}
			}
			
			class Generic<T> implements Variable<T> {
				
				private java.lang.String name = null;
				private T value = null;
				
				public Generic(java.lang.String name, T value) {
					this.name = name;
					this.value = value;
				}
	
				@Override
				public T getValue() {
					return value;
				}
				
				@Override
				public void bind(T value) {
					this.value = value;
				}
				
				@Override
				public String getName() {
					if(name == null) {
						throw new IllegalStateException();
					}
					return name;
				}
				
				@SuppressWarnings("unchecked")
				@Override
                @JsonIgnore
				public Class<T> getType() {
					if(value == null) {
						throw new IllegalStateException();
					}
					return (Class<T>) value.getClass();
				}

                public String toString(){
                    return name + "=" + value;
                }
			}
		
		}
	}
}