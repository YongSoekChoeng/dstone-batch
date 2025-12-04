package net.dstone.batch.common.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.dstone.batch.common.consts.ConstMaps;
import net.dstone.batch.common.core.BaseBatchObject;

@Configuration
public class ConfigListener extends BaseBatchObject {

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

    /**
     * Job 의 쓰레드를 JOB_THREAD_MAP 에 세팅하는 리스너.
     * @return
     */
    @Bean("jobRegisterListener")
    public JobExecutionListener jobRegisterListener() {
    	return new JobExecutionListener() {
            @SuppressWarnings({ })
			@Override
            public void beforeJob(JobExecution jobExecution) {
            	this.executeLog(jobExecution);
            	// Job Thread 등록.
                ConstMaps.JobThreadRegistry.register(jobExecution.getId(), Thread.currentThread());
                // Job Parameter 등록.
                ConstMaps.JobParamRegistry.registerByExecution(jobExecution.getId(), jobExecution.getJobParameters().getParameters());
            }
            @Override
            public void afterJob(JobExecution jobExecution) {
            	this.executeLog(jobExecution);
            	// Job Thread 등록해제.
                ConstMaps.JobThreadRegistry.unregister(jobExecution.getId());
                // Job Parameter 등록해제.
                ConstMaps.JobParamRegistry.unregisterByExecution(jobExecution.getId());
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

}
