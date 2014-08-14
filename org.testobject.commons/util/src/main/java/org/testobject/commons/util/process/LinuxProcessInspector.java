package org.testobject.commons.util.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.thread.ThreadUtil;

/**
 * 
 * @author enijkamp
 * 
 * https://github.com/jezhumble/javasysmon
 */
public class LinuxProcessInspector implements ProcessInspector
{

	private static final Log log = LogFactory.getLog(LinuxProcessInspector.class);

	private static class LinuxProcess implements Process
	{
		private final int pid;
		private final String name;
		private final Process[] children;

		public LinuxProcess(int pid, String name, Process[] children)
		{
			this.pid = pid;
			this.name = name;
			this.children = children;
		}

		@Override
		public int getPid()
		{
			return pid;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public Process[] getChildren()
		{
			return children;
		}

		@Override
		public int getVirtualMemoryConsumption() {
			try {
				java.lang.Process p = Runtime.getRuntime().exec("ps -p " + this.pid + " -o vsz --no-heading");
				p.waitFor();
				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String pid = reader.readLine();
				return Integer.parseInt(pid);
			} catch (IOException | InterruptedException e) {
				log.warn("Could not get process '" + this.pid + "' virtual memory consumption");
				throw new RuntimeException(e);
			}
		}

		@Override
		public float getCpuUsage() {
			try(RandomAccessFile reader = new RandomAccessFile("/proc/" + pid + "/stat", "r")) {
				
				// sample 0
				String load0 = reader.readLine();
		        String[] toks0 = load0.split(" ");
		        
		        long utime0 = Long.parseLong(toks0[13]);
		        long stime0 = Long.parseLong(toks0[14]);
		        
		        // wait and reset
		        ThreadUtil.sleep(1000);
		        reader.seek(0);
		        
		        // sample 1
				String load1 = reader.readLine();
		        String[] toks1 = load1.split(" ");
		        
		        long utime1 = Long.parseLong(toks1[13]);
		        long stime1 = Long.parseLong(toks1[14]);

				long delta = (utime1 + stime1) - (utime0 + stime0);
                float percentage = (delta / 100f);

		        return percentage;

			} catch (Throwable e) {
				log.warn("Could not get cpu usage for process '" + pid + "'");
				throw new RuntimeException(e);
			}
		}

		@Override
		public void suspend() {
			try {
				Runtime.getRuntime().exec("kill -STOP " + this.pid);
			} catch (IOException e) {
				log.error("Could not suspend process '" + pid + "'");
				throw new RuntimeException(e);				
			}
		}

		@Override
		public void resume() {
			try {
				Runtime.getRuntime().exec("kill -CONT " + this.pid);
			} catch (IOException e) {
				log.error("Could not resume process '" + pid + "'");
				throw new RuntimeException(e);				
			}			
		}
	}

	@Override
	public Process getProcess(int pid)
	{
		ProcessBuilder.ProcessInfo[] processes = ProcessBuilder.processTable();
		return ProcessBuilder.createTree(processes, pid);
	}

	@Override
	public Process[] getProcesses()
	{
		ProcessBuilder.ProcessInfo[] processInfos = ProcessBuilder.processTable();
		Process[] processes = new Process[processInfos.length];
		for (int i = 0; i < processes.length; i++)
		{
			processes[i] = getProcess(processInfos[i].pid);
		}
		return processes;
	}

	@Override
	public Process getProcessTree()
	{
		return getProcess(1);
	}

	/**
	 * http://www.golesny.de/p/code/javagetpid
	 */
	@Override
	public Process getProcess(java.lang.Process process)
	{
		return getProcess(getProcessPid(process));
	}

	@Override
	public int getProcessPid(java.lang.Process process) {
		if (process.getClass().getName().equals("java.lang.UNIXProcess"))
		{
			/* get the PID on unix/linux systems */
			try
			{
				Field pid = process.getClass().getDeclaredField("pid");
				pid.setAccessible(true);
				return pid.getInt(process);
				
			} catch (Throwable t)
			{
				throw new RuntimeException(t);
			}
		}
		
		throw new IllegalArgumentException(process.getClass().getName());
	}
	
	private static class ProcessBuilder
	{
		public static Process createTree(ProcessInfo[] processes, int pid)
		{
			ProcessInfo process = getProcess(processes, pid);
			return new LinuxProcess(process.pid, process.name, getChildren(processes, process));
		}

		public static ProcessInfo getProcess(ProcessInfo[] processes, int pid)
		{
			for (ProcessInfo process : processes)
			{
				if (process.pid == pid)
				{
					return process;
				}
			}
			throw new IllegalArgumentException("Cannot find process with pid '" + pid + "'");
		}

		public static Process[] getChildren(ProcessInfo[] processes, ProcessInfo parent)
		{
			List<Process> children = new LinkedList<>();
			for (ProcessInfo child : processes)
			{
				if (child.parentPid == parent.pid)
				{
					children.add(new LinuxProcess(child.pid, child.name, getChildren(processes, child)));
				}
			}
			return children.toArray(new Process[] {});
		}

		public static ProcessInfo[] processTable()
		{
			final String[] pids = FileUtils.pidsFromProcFilesystem();
			List<ProcessInfo> processTable = new ArrayList<>();
			for (int i = 0; i < pids.length; i++)
			{
				try
				{
					String stat = FileUtils.slurp("/proc/" + pids[i] + "/stat");
					String status = FileUtils.slurp("/proc/" + pids[i] + "/status");
					LinuxProcessInfoParser parser = new LinuxProcessInfoParser(stat, status);
					processTable.add(parser.parse());
				} catch (Throwable e)
				{
					// process probably died since we got the process list
				}
			}
			return processTable.toArray(new ProcessInfo[] {});
		}

		static class ProcessInfo
		{
			private int pid;
			private int parentPid;
			private String name;

			public ProcessInfo(int pid, int parentPid, String name)
			{
				this.pid = pid;
				this.parentPid = parentPid;
				this.name = name;
			}
		}

		static class LinuxProcessInfoParser
		{
			private final String stat;
			private final String status;

			private static final Pattern STATUS_NAME_MATCHER = Pattern.compile("Name:\\s+(\\w+)", Pattern.MULTILINE);

			public LinuxProcessInfoParser(String stat, String status)
			{
				this.stat = stat;
				this.status = status;
			}

			public ProcessInfo parse()
			{
				String[] statElements = stat.split(" ");
				return new ProcessInfo(Integer.parseInt(statElements[0]), Integer.parseInt(statElements[3]), getFirstMatch(
						STATUS_NAME_MATCHER, status));
			}

			public String getFirstMatch(Pattern pattern, String string)
			{
				try
				{
					Matcher matcher = pattern.matcher(string);
					matcher.find();
					return matcher.group(1);
				} catch (Exception e)
				{
					return "0";
				}
			}
		}
	}

	/**
	 * Convenience methods for interacting with the filesystem.
	 */
	private static class FileUtils
	{
		private static final Pattern PROC_DIR_PATTERN = Pattern.compile("([\\d]*)");

		private final static FilenameFilter PROCESS_DIRECTORY_FILTER = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				File fileToTest = new File(dir, name);
				return fileToTest.isDirectory() && PROC_DIR_PATTERN.matcher(name).matches();
			}
		};

		/**
		 * If you're using an operating system that supports the proc filesystem,
		 * this returns a list of all processes by reading the directories under
		 * /proc
		 *
		 * @return An array of the ids of all processes running on the OS.
		 */
		public static String[] pidsFromProcFilesystem()
		{
			return new File("/proc").list(FileUtils.PROCESS_DIRECTORY_FILTER);
		}

		/**
		 * Given a filename, reads the entire file into a string.
		 *
		 * @param fileName The path of the filename to read. Should be absolute.
		 * @return A string containing the entire contents of the file
		 * @throws IOException If there's an IO exception while trying to read the file
		 */
		public static String slurp(String fileName) throws IOException
		{
			return slurpFromInputStream(new FileInputStream(fileName));
		}

		/**
		 * Given an InputStream, reads the entire file into a string.
		 *
		 * @param stream The InputStream representing the file to read
		 * @return A string containing the entire contents of the input stream
		 * @throws IOException If there's an IO exception while trying to read the input stream
		 */
		public static String slurpFromInputStream(InputStream stream) throws IOException
		{
			if (stream == null)
			{
				return null;
			}
			StringWriter sw = new StringWriter();
			String line;
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
				while ((line = reader.readLine()) != null)
				{
					sw.write(line);
					sw.write('\n');
				}
			} finally
			{
				stream.close();
			}
			return sw.toString();
		}
	}

	@Override
	public float getCpuUsage() {
		try(RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r")) {
			
			final int samples = 2;
			
			long[] cpu = new long[samples];
			long[] idle = new long[samples];
			
			for(int i = 0; i < samples; i++) {
				
				// sample
				String load = reader.readLine();
		        String[] toks = load.split(" ");

		        idle[i] = Long.parseLong(toks[5]);
		        cpu[i] = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
		              + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
		        
		        // wait and reset
		        ThreadUtil.sleep(200);
		        reader.seek(0);
			}

			float sum = 0;
			for(int i = 0; i < samples - 1; i++) {
				sum += (float)(cpu[i+1] - cpu[i]) / ((cpu[i+1] + idle[i+1]) - (cpu[i] + idle[i]));
			}

	        return sum / samples;

		} catch (Throwable e) {
			log.warn("Could not get cpu usage");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void killHard(java.lang.Process process) {
		try {
			int pid = getProcessPid(process);
			Runtime.getRuntime().exec("kill -9 " + pid);
		} catch(Throwable e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Override
	public int getCurrentProcess() {
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		String[] ids = pid.split("@");
		return Integer.valueOf(ids[0]);
	}
	
	public static void kill(int pid) {
		try {
			Runtime.getRuntime().exec("kill -9 " + pid);
		} catch(Throwable e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	public static int getPID(java.lang.Process process){
		if (process.getClass().getName().equals("java.lang.UNIXProcess"))
		{
			/* get the PID on unix/linux systems */
			try
			{
				Field pid = process.getClass().getDeclaredField("pid");
				pid.setAccessible(true);
				return pid.getInt(process);
				
			} catch (Throwable t)
			{
				throw new RuntimeException(t);
			}
		}
		
		throw new IllegalArgumentException(process.getClass().getName());
	}
}
