package net.dstone.batch.common.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.dstone.common.utils.LogUtil;
import net.dstone.common.utils.StringUtil;

@Component
public class BaseBatchObject{
	
	private LogUtil myLogger = null;
	private Map<String,Object> property = new HashMap<String,Object>();
	
	protected LogUtil getLogger() {
		if(myLogger == null) {
			myLogger = new LogUtil(this);
		}
		return myLogger;
	}

	protected LogUtil getLogger(Object o) {
		if(myLogger == null) {
			myLogger = new LogUtil(o.getClass());
		}
		return myLogger;
	}
	
	protected void trace(Object o) {
		getLogger().trace(o);
	}

	protected void debug(Object o) {
		getLogger().debug(o);
	}
	
	protected void info(Object o) {
		getLogger().info(o);
	}
	
	protected void warn(Object o) {
		getLogger().warn(o);
	}

	protected void error(Object o) {
		getLogger().error(o);
	}

	protected void sysout(Object o) {
		LogUtil.sysout(o);
	}

	protected void setProperty(String key, Object val) {
		this.property.put(key, val);
	}

	protected String getStrProperty(String key) {
		if( this.property.containsKey(key) ) {
			return StringUtil.nullCheck(this.property.get(key), "").toString();
		}else {
			return "";
		}
	}
	protected String getStrProperty(String key, String defaultVal) {
		if( this.property.containsKey(key) ) {
			return StringUtil.nullCheck(this.property.get(key), defaultVal).toString();
		}else {
			return defaultVal;
		}
	}
	protected Object getObjProperty(String key) {
		if( this.property.containsKey(key) ) {
			return this.property.get(key);
		}else {
			return null;
		}
	}
	
}
