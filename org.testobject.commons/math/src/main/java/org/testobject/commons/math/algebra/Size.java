package org.testobject.commons.math.algebra;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author enijkamp
 */
public interface Size {

	class Int {

        public int w;
        public int h;

        public static final Int ZERO = new Int(0, 0);
        
        public static Int zero() {
        	return ZERO;
        }
        
        public static Int from(int w, int h) {
        	return new Int(w, h);
        }

        @JsonCreator
        public Int(@JsonProperty("w") int width, @JsonProperty("h") int height) {
            this.w = width;
            this.h = height;
        }

        public Int(Point.Int point) {
            this.w = point.x;
            this.h = point.y;
        }

        @JsonProperty("h")
        public final int getHeight() {
            return h;
        }

        @JsonProperty("w")
        public final int getWidth() {
            return w;
        }

        public String toString() {
            return "(" + w + ", " + h + ")";
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Int))
                return false;
            Int size = (Int) obj;
            return w == size.w && h == size.h;
        }

        public Int plus(int width, int height) {
            return new Int(this.w + width, this.h + height);
        }

        public Int plus(Int size) {
            return new Int(this.w + size.w, this.h + size.h);
        }

        public Int minus(Int size) {
            return new Int(this.w - size.w, this.h - size.h);
        }

        public Int max(Int size) {
            return new Int(Math.max(w, size.w), Math.max(h,
                    size.h));
        }

        public Int abs() {
            return new Int(Math.abs(w), Math.abs(h));
        }

        public static Int diff(Point.Int p1, Point.Int p2) {
            return new Int(p1.x - p2.x, p1.y - p2.y);
        }
    }

    class Double {

        public double w;
        public double h;

        public static final Double ZERO = new Double(0, 0);
        
        public static Double zero() {
        	return ZERO;
        }
        
        public static Double from(int w, int h) {
        	return new Double(w, h);
        }

        public Double(double width, double height) {
            this.w = width;
            this.h = height;
        }

        public Double(Point.Double point) {
            this.w = point.x;
            this.h = point.y;
        }

        public final double getH() {
            return h;
        }

        public final double getWidth() {
            return w;
        }

        public String toString() {
            return "(" + w + ", " + h + ")";
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Double))
                return false;
            Double size = (Double) obj;
            return w == size.w && h == size.h;
        }

        public Double plus(double width, double height) {
            return new Double(this.w + width, this.h + height);
        }

        public Double plus(Double size) {
            return new Double(this.w + size.w, this.h + size.h);
        }

        public Double minus(Double size) {
            return new Double(this.w - size.w, this.h - size.h);
        }

        public Double scale(double scale) {
            return new Double(w * scale, h * scale);
        }

        public Double scale(double scaleX, double scaleY) {
            return new Double(w * scaleX, h * scaleY);
        }

        public Double scale(Double scale) {
            return new Double(w * scale.w, h * scale.h);
        }

        public Double max(Double size) {
            return new Double(Math.max(w, size.w), Math.max(h,
                    size.h));
        }

        public Double abs() {
            return new Double(Math.abs(w), Math.abs(h));
        }

        public static Double diff(Point.Double p1, Point.Double p2) {
            return new Double(p1.x - p2.x, p1.y - p2.y);
        }
    }

}