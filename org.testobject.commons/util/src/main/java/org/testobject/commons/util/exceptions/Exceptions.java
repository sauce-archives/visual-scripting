package org.testobject.commons.util.exceptions;

/**
 * 
 * @author enijkamp
 *
 */
public class Exceptions {
	
	@SuppressWarnings("serial")
	public static class UIException extends RuntimeException {
		private boolean critical;

		public UIException(String userFriendlyText, boolean critical) {
			super(userFriendlyText);
			this.critical = critical;
		}
		
		public UIException(String userFriendlyText, Throwable t, boolean critical) {
			super(userFriendlyText, t);
			this.critical = critical;
		}
		
		public boolean isCritical() {
			return critical;
		}
	}
	
	public static IllegalArgumentException newUnsupportedTypeException(String entity, Class<?> type) {
		return new IllegalArgumentException("Unsupported " + entity + " type '" +  type.getName() + "'");
	}
	
	public static IllegalArgumentException newUnsupportedTypeException(String entity, String type) {
		return new IllegalArgumentException("Unsupported " + entity + " type '" +  type + "'");
	}
	
	public static UIException newUIException(String userFriendlyText, boolean critical){
		return new UIException(userFriendlyText, critical);
	}
	
	public static UIException newUIException(String userFriendlyText, Throwable t, boolean critical){
		return new UIException(userFriendlyText, t, critical);
	}

}
