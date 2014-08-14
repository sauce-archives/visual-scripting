package org.testobject.commons.util.platform;

/**
 * 
 * @author enijkamp
 *
 */
public final class OS
{
    private static final int UNSPECIFIED = -1;
    private static final int MAC = 0;
    private static final int LINUX = 1;
    private static final int WINDOWS = 2;
    private static final int SOLARIS = 3;
    private static final int osType;

    static
    {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux"))
        {
            osType = LINUX;
        }
        else if (osName.startsWith("Mac"))
        {
            osType = MAC;
        }
        else if (osName.startsWith("Windows"))
        {
            osType = WINDOWS;
        }
        else if (osName.startsWith("Solaris") || osName.startsWith("SunOS"))
        {
            osType = SOLARIS;
        }
        else
        {
            osType = UNSPECIFIED;
        }
    }

    private static final boolean isServerMode;

    static
    {
        isServerMode = System.getProperty("java.vm.name").toLowerCase().contains("server");
    }

    private OS()
    {
    }

    public static final boolean isServerMode()
    {
        return isServerMode;
    }

    public static final boolean isClientMode()
    {
        return !isServerMode;
    }

    public static final boolean isMac()
    {
        return osType == MAC;
    }

    public static final boolean isLinux()
    {
        return osType == LINUX;
    }

    public static final boolean isWindows()
    {
        return osType == WINDOWS;
    }

    public static final boolean isSolaris()
    {
        return osType == SOLARIS;
    }

    public static final boolean isX11()
    {
        return !OS.isWindows() && !OS.isMac();
    }

    public static final boolean isWebStart()
    {
        return System.getProperty("javawebstart.version") != null;
    }
}