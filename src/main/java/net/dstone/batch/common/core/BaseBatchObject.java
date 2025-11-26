package net.dstone.batch.common.core;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.dstone.batch.common.consts.Constants;
import net.dstone.common.utils.LogUtil;

@Component
public class BaseBatchObject{
	
	private LogUtil myLogger = null;
	protected Map<String,Object> baseParam = new HashMap<String,Object>();
	
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

	protected void log(Object msg) {
    	//this.info(msg);
    	this.debug(msg);
    }

	protected void callLog(Object obj, String method) {
		String logStr = obj.getClass().getName() + "."+method+"() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" ;
    	//this.info(logStr);
    	this.debug(logStr);
    }

	protected void callLog(Object obj, String method, String paramMsg) {
		String logStr = obj.getClass().getName() + "."+method+"("+paramMsg+") has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" ;
    	//this.info(logStr);
    	this.debug(logStr);
    }

	protected void setBaseParam(String key, Object val) {
		this.baseParam.put(key, val);
	}

	protected Object getBaseParam(String key) {
		return this.baseParam.get(key);
	}
	
	protected Map<String,Object> getBaseParamMap() {
		return this.baseParam;
	}
	
}
