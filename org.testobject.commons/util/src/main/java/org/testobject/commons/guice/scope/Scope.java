package org.testobject.commons.guice.scope;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public interface Scope {

    class DefaultScope implements Scope{

        private final Map<String, Object> data;
        
        public DefaultScope() {
        	this(new HashMap<String, Object>());
		}
        
        public DefaultScope(Map<String, Object> data) {
			this.data = data;
		}

        public Object getAttribute(String key){
            return data.get(key);
        }

        public void setAttribute(String key, Object value){
            data.put(key, value);
        }

        public void removeAttribute(String key){
            data.remove(key);
        }
    }

    class WebSession implements Scope {

        private HttpSession session;

        public WebSession(HttpSession session){
            this.session = session;
        }

        public Object getAttribute(String key){
            return session.getAttribute(key);
        }

        public void setAttribute(String key, Object value){
            session.setAttribute(key, value);
        }

        public void removeAttribute(String key){
            session.removeAttribute(key);
        }

    }

    public Object getAttribute(String key);

    public void setAttribute(String key, Object value);

    public void removeAttribute(String key);
}
