package org.testobject.commons.util.process;

import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author enijkamp
 *
 */
public class LinuxProcessInspectorTest {
	
	@Test
	public void cpuUsage() {
		
		LinuxProcessInspector inspector = new LinuxProcessInspector();
		
		System.out.println(inspector.getProcess(inspector.getCurrentProcess()).getCpuUsage());
	}
	
	@Ignore @Test
	public void cpuUsageSingle() {
		LinuxProcessInspector inspector = new LinuxProcessInspector();
		ProcessInspector.Process process = inspector.getProcess(3849);
		System.out.println(process.getCpuUsage());
	}

}
