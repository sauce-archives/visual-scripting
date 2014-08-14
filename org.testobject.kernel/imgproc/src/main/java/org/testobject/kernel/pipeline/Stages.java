package org.testobject.kernel.pipeline;

/**
 * 
 * @author nijkamp
 *
 */
public interface Stages
{
    void start();

    void end();

    void done(String stage);

    class Builder
    {
        public static Stages mock()
        {
            return new Stages()
            {

                @Override
                public void start()
                {

                }

                @Override
                public void end()
                {

                }

                @Override
                public void done(String stage)
                {

                }
            };
        }
    }
}
