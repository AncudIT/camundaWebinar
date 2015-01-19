package de.ancud.camunda.simple.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple helper class providing a possibility to switch between Module and Class Logger. 
 *
 */
public class ModuleLogger {

    public static Log getLogger(Class<?> cls) {
	if (Boolean.getBoolean("hws.class.logger")) {
	    return LogFactory.getLog(cls);
	} else {
	    return LogFactory.getLog("hws");
	}
    }

}
