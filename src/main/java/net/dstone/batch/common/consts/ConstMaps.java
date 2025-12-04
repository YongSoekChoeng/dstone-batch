package net.dstone.batch.common.consts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.JobParameter;

import net.dstone.common.utils.StringUtil;

/**
 * Static Map들을 관리하는 클래스
 */
public class ConstMaps {

	/**
	 * JOB실행ID(executionId)를 KEY값으로 JOB실행쓰레드를 저장하는 맵.
	 */
	private static ConcurrentHashMap<Long, Thread> JOB_THREAD_MAP = new ConcurrentHashMap<>();
	
	public static class JobThreadRegistry {
	    public static synchronized void register(Long executionId, Thread thread) {
	    	JOB_THREAD_MAP.put(executionId, thread);
	    }
	    public static synchronized void unregister(Long executionId) {
	    	JOB_THREAD_MAP.remove(executionId);
	    }
	}

	/**
	 * 실행ID를 KEY값으로 JOB실행파라메터를 저장하는 맵.
	 */
	private static ConcurrentHashMap<String, Map<String,String>> JOB_PARAM_MAP = new ConcurrentHashMap<String, Map<String,String>>();

	public static class JobParamRegistry {
		public static final String EXE_PREFIX 		= "Execution-";
		public static final String THREAD_PREFIX 	= "Thread-";
		public static synchronized void registerByExecution(Long executionId, Map<String, JobParameter<?>> jParamMap) {
			register(EXE_PREFIX+executionId, jParamMap);
		}
		public static synchronized void registerByThread(Long threadId, Map<String, JobParameter<?>> jParamMap) {
			register(THREAD_PREFIX+threadId, jParamMap);
		}
	    @SuppressWarnings("rawtypes")
		protected static synchronized void register(String id, Map<String, JobParameter<?>> jParamMap) {
	        Map<String,String> jobParameters = new HashMap<String,String>();
	    	if( jParamMap != null ) {
	    		Iterator<String> keys = jParamMap.keySet().iterator();
	    		while(keys.hasNext()) {
	    			String key = keys.next();
	    			JobParameter jobParameterVal = jParamMap.get(key);
	    			if(  jobParameterVal != null) {
	    				String val = StringUtil.nullCheck(jobParameterVal.getValue(), "");
	        			jobParameters.put(key, val);
	    			}
	    		}
	    	}
	    	StringBuffer buff = new StringBuffer();
	    	buff.append("||============== " + " JobParamRegistry.register(id["+id+"], ["+jobParameters+"])" + " ==============||");
	    	//LogUtil.sysout(buff.toString());
	    	JOB_PARAM_MAP.put(id, jobParameters);
	    }
	    
		public static synchronized void unregisterByExecution(Long executionId) {
			unregister(EXE_PREFIX+executionId);
		}
		public static synchronized void unregisterByThread(Long threadId) {
			unregister(THREAD_PREFIX+threadId);
		}
		protected static synchronized void unregister(String id) {
	    	StringBuffer buff = new StringBuffer();
	    	buff.append("||============== " + "JobParamRegistry.unregister(id["+id+"])" + " ==============||");
	    	//LogUtil.sysout(buff.toString());
	    	JOB_PARAM_MAP.remove(id);
	    }
		public static String getInitJobParamByExecutionId(Long executionId, String key) {
			String val = "";
			String id = ConstMaps.JobParamRegistry.EXE_PREFIX + executionId;
			if(ConstMaps.JOB_PARAM_MAP.containsKey(id)) {
				Map map = ConstMaps.JOB_PARAM_MAP.get(id);
				if(map.containsKey(key)) {
					val = map.get(key).toString();
				}
			}
			return val;
		}
		public static String getInitJobParamByThreadId(Long threadId, String key) {
			String val = "";
			String id = ConstMaps.JobParamRegistry.THREAD_PREFIX + threadId;
			if(ConstMaps.JOB_PARAM_MAP.containsKey(id)) {
				Map map = ConstMaps.JOB_PARAM_MAP.get(id);
				if(map.containsKey(key)) {
					val = map.get(key).toString();
				}
			}
			return val;
		}
	}

}
