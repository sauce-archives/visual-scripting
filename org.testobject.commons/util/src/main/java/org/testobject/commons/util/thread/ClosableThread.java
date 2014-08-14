package org.testobject.commons.util.thread;

import java.lang.Thread.UncaughtExceptionHandler;

import org.testobject.commons.util.io.Closable;


/**
 * 
 * @author nijkamp
 *
 */
public class ClosableThread implements Closable
{	
    private final java.lang.Thread thread;
    private final Runnable runnable;
    private boolean closed = false;
    private final int timeout;
    
    public ClosableThread(Runnable runnable)
    {
        this(runnable, 0);
    }
    
    public ClosableThread(Runnable runnable, int timeout, String name)
    {
    	this(runnable, timeout);
    	this.thread.setName(name);
    }
    
    public ClosableThread(Runnable runnable, int timeout)
    {
        this.runnable = runnable;
        this.thread = new java.lang.Thread(runnable);
        this.timeout = timeout;
    }
    
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler)
    {
        thread.setUncaughtExceptionHandler(handler);
    }
    
    public void start()
    {
        if (closed)
        {
            throw new IllegalStateException();
        }
        thread.start();
    }
    
    @Override
    public void close()
    {
        if (closed)
        {
            return;
        }
        try
        {
            if(runnable instanceof Closable)
            {
                Closable closable = (Closable) runnable;
                closable.close();
                thread.join(timeout);
            }
        }
        catch (InterruptedException e)
        {
        }
        finally
        {
            closed = true;
        }
    }
}
