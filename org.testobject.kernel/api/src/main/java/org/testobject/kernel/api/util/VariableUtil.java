package org.testobject.kernel.api.util;

import java.util.List;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Size;
import org.testobject.kernel.api.classification.graph.Contour;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.api.classification.graph.Variable.Names;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.procedural.Color;
import org.testobject.kernel.imaging.segmentation.Mask;

/**
 * 
 * @author enijkamp
 *
 */
public class VariableUtil {
	
	public static void addPosition(List<Variable<?>> variables, Point.Int size, int id) {
		variables.add(Variable.Builder.value(Names.Geometric.position + "." + id, size));
	}
	
	public static ImageFingerprint getFingerprint(List<Variable<?>> variables) {
		return VariableUtil.<ImageFingerprint>get(variables, Names.Depiction.fingerprint).getValue();
	}
	
	public static List<Mask> getMasks(List<Variable<?>> variables) {
		return VariableUtil.<List<Mask>>get(variables, Names.Depiction.masks).getValue();
	}
	
	public static List<Contour> getContours(List<Variable<?>> variables) {
		return VariableUtil.<List<Contour>>get(variables, Names.Depiction.contours).getValue();
	}
	
	public static Point.Int getPosition(List<Variable<?>> variables) {
		return VariableUtil.<Point.Int>get(variables, Names.Geometric.position).getValue();
	}

	public static Size.Int getSize(List<Variable<?>> variables) {
		return VariableUtil.<Size.Int>get(variables, Names.Geometric.size).getValue();
	}
	
	public static Color getFill(List<Variable<?>> variables) {
		return VariableUtil.<Color>get(variables, Names.Depiction.fill).getValue();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Variable<T> get(List<Variable<?>> variables, String name) {
		for(Variable<?> variable : variables) {
			if(variable.getName().equals(name)) {
				return (Variable<T> ) variable;
			}
		}
		
		throw new IllegalArgumentException("missing variable '" + name + "'");
	}

	public static boolean has(List<Variable<?>> variables, String name) {
		for(Variable<?> variable : variables) {
			if(variable.getName().equals(name)) {
				return true;
			}
		}
		
		return false;
	}

	public static Point.Int getPosition(List<Variable<?>> features, int id) {
		return VariableUtil.<Point.Int>get(features, Names.Geometric.position + "." + id).getValue();
	}
}