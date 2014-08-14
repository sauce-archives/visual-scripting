package org.testobject.kernel.inference.interpreter;

/**
 * 
 * @author enijkamp
 *
 */
public interface Stages {
	
    void start();

    void end();

    void done(String stage);

    class Builder {
        public static Stages stub() {
            return new Stages() {

                @Override
                public void start() {

                }

                @Override
                public void end() {

                }

                @Override
                public void done(String stage) {

                }
            };
        }
    }
}
