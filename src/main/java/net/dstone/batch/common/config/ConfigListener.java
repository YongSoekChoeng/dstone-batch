package net.dstone.batch.common.config;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Configuration
public class ConfigListener extends BatchBaseObject{

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

    private void log(Object msg) {
    	this.info(msg);
    }

    @Bean("jobRegisterListener")
    public JobExecutionListener jobRegisterListener() {
    	return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
            	this.executeLog(jobExecution);
                JobThreadRegistry.register(jobExecution.getId(), Thread.currentThread());
            }
            @Override
            public void afterJob(JobExecution jobExecution) {
            	this.executeLog(jobExecution);
                JobThreadRegistry.unregister(jobExecution.getId());
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
    
    private static ConcurrentHashMap<Long, Thread> jobThreads = new ConcurrentHashMap<>();
    @Component
    private static class JobThreadRegistry {
        public static synchronized void register(Long executionId, Thread thread) {
            jobThreads.put(executionId, thread);
        }
        public static synchronized void unregister(Long executionId) {
            jobThreads.remove(executionId);
        }
    }
}
