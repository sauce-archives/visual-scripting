package org.testobject.commons.bus;

public interface Progress {

	class Builder {
		public static Progress mock() {
			return new Progress() {
				public void onProgress(Event<?> event) {
				}
			};
		}
	}

	void onProgress(Event<?> event);

}
