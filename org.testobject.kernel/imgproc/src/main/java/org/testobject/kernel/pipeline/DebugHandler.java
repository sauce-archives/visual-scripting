package org.testobject.kernel.pipeline;

/**
 * 
 * @author enijkamp
 *
 */
public interface DebugHandler {
	
	class Builder {
		public static DebugHandler mock() {
			return new DebugHandler() {
				@Override
				public void handle(Pipeline.Intermediate info) {

				}
			};
		}
		
		public static DebugHandler sysout()	{
			return new DebugHandler() {
				@Override
				public void handle(Pipeline.Intermediate info) {
					if (info instanceof Pipeline.Locators) {
						Pipeline.Locators locators = (Pipeline.Locators) info;
						System.out.println();
						System.out.println(">> locators (before) <<");
						Locators.Util.print(System.out, locators.before);
						System.out.println();
						System.out.println(">> locators (after) <<");
						Locators.Util.print(System.out, locators.after);
						System.out.println();
					}
				}
			};
		}
	}

	void handle(Pipeline.Intermediate info);
}