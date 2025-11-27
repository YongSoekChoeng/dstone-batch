package net.dstone.batch.common.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseBatchObject;
import net.dstone.common.utils.LogUtil;
import net.dstone.common.utils.StringUtil;

@Configuration
public class ConfigListener extends BaseBatchObject{

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

    /**
     * Job 의 쓰레드를 JOB_THREAD_MAP 에 세팅하는 리스너.
     * @return
     */
    @Bean("jobRegisterListener")
    public JobExecutionListener jobRegisterListener() {
    	return new JobExecutionListener() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
            public void beforeJob(JobExecution jobExecution) {
            	this.executeLog(jobExecution);
            	// Thread 등록.
                JobThreadRegistry.register(jobExecution.getId(), Thread.currentThread());
                // JobParameter 등록.
                JobParamRegistry.registerByExecution(jobExecution.getId(), jobExecution.getJobParameters().getParameters());
            }
            @Override
            public void afterJob(JobExecution jobExecution) {
            	this.executeLog(jobExecution);
                JobThreadRegistry.unregister(jobExecution.getId());
                JobParamRegistry.unregisterByExecution(jobExecution.getId());
            }
            private void executeLog(JobExecution jobExecution) {
            	String jobName = jobExecution.getJobInstance().getJobName();
            	String jobStatus = jobExecution.getStatus().toString();
            	StringBuffer buff = new StringBuffer();
            	buff.setLength(0);
            	buff.append("\n");
            	buff.append("||======================================= Job["+jobName+"] "+ jobStatus +" =======================================||");
            	buff.append("\n");
            	info(buff.toString());
            }
    	};
    }
    
    /**
     * JOB실행ID(executionId)를 KEY값으로 JOB실행쓰레드를 저장하는 맵.
     */
    private static ConcurrentHashMap<Long, Thread> JOB_THREAD_MAP = new ConcurrentHashMap<>();
    @Component
    private static class JobThreadRegistry {
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
    public static ConcurrentHashMap<String, Map<String,String>> JOB_PARAM_MAP = new ConcurrentHashMap<String, Map<String,String>>();
    @Component
    public static class JobParamRegistry {
    	public static final String EXE_PREFIX 		= "Execution-";
    	public static final String THREAD_PREFIX 	= "Thread-";
    	public static synchronized void registerByExecution(Long executionId, Map<String, JobParameter<?>> jParamMap) {
    		register(EXE_PREFIX+executionId, jParamMap);
    	}
    	public static synchronized void registerByThread(Long threadId, Map<String, JobParameter<?>> jParamMap) {
    		register(THREAD_PREFIX+threadId, jParamMap);
    	}
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
        	LogUtil.sysout(buff.toString());
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
        	LogUtil.sysout(buff.toString());
        	JOB_PARAM_MAP.remove(id);
        }
    }

}
