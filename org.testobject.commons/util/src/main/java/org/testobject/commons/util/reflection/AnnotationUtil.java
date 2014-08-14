package org.testobject.commons.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author enijkamp
 *
 */
public class AnnotationUtil {

	public static <A extends Annotation> Set<A> getInheritedAnnotations(Class<?> type, Class<A> annotation) {
		Set<A> annotations = new HashSet<>();

		getInheritedAnnotations(type, annotation, annotations);

		return annotations;
	}

	public static <A extends Annotation> void getInheritedAnnotations(Class<?> type, Class<A> annotation, Set<A> annotations) {
		A annotaionValue = type.getAnnotation(annotation);
		if (annotaionValue != null) {
			annotations.add(annotaionValue);
		}

		if (type.getSuperclass() != Object.class && type.getSuperclass() != null) {
			getInheritedAnnotations(type.getSuperclass(), annotation, annotations);
		}

		if (type.getInterfaces() != null) {
			for (Class<?> clazz : type.getInterfaces()) {
				getInheritedAnnotations(clazz, annotation, annotations);
			}
		}
	}
	
	public static <A extends Annotation> boolean isInheritedAnnotationPresent(Method method, Class<A> annotation) {
		return getInheritedAnnotations(method, annotation).isEmpty() == false;
	}

	public static <A extends Annotation> Set<A> getInheritedAnnotations(Method method, Class<A> annotation) {
		Set<A> annotations = new HashSet<>();

		getInheritedAnnotations(method, annotation, annotations);

		return annotations;
	}

	public static <A extends Annotation> void getInheritedAnnotations(Method method, Class<A> annotation, Set<A> annotations) {
		A annotationValue = method.getAnnotation(annotation);
		if (annotationValue != null) {
			annotations.add(annotationValue);
		}

		try {
			Class<?> declaringClass = method.getDeclaringClass();
			if (declaringClass.getSuperclass() != Object.class && declaringClass.getSuperclass() != null) {
				getInheritedAnnotations(declaringClass.getSuperclass(), annotation, annotations);
			}

			if (declaringClass.getInterfaces() != null) {
				for (Class<?> clazz : declaringClass.getInterfaces()) {
					Method superMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
					if (superMethod != null) {
						getInheritedAnnotations(superMethod, annotation, annotations);
					}
				}
			}
		} catch (NoSuchMethodException e) {
		}
	}
}
