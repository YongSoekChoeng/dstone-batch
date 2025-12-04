package net.dstone.batch.common.config;

import java.util.Collection;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseBatchObject;
import net.dstone.batch.common.core.BaseJobConfig;

@Configuration
public class ConfigAutoReg extends BaseBatchObject {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

	@PostConstruct
	public void registerJobs() throws Exception {
		this.info(this.getClass().getName() + ".registerJobs() has been called !!!");
		try {
			// @AutoRegisteredJob 애노테이션이 붙은 모든 빈 검색
			Map<String, Object> jobs = applicationContext.getBeansWithAnnotation(AutoRegJob.class);
			
			for(Object jobObj : jobs.values()) {
				
				if (jobObj instanceof BaseJobConfig) {
					BaseJobConfig abstractJob = (BaseJobConfig)jobObj;
					String jobName = jobObj.getClass().getAnnotation(AutoRegJob.class).name();
					abstractJob.setName(jobName);
					Job job = abstractJob.buildAutoRegJob();
					jobRegistry.register(new ReferenceJobFactory(job));
				}
			}
			
			if( "true".equals(configProperty.getProperty("spring.cloud.dataflow.job-auto-register")) ) {
				this.registerToDataflow();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void registerToDataflow() {
		RestTemplate restTemplate = null;
		try {
			restTemplate = net.dstone.common.utils.RestFulUtil.getInstance().getRestTemplate();
	        // 등록된 모든 Job 이름 가져오기
	        Collection<String> jobNames = jobRegistry.getJobNames();
	        for (String jobName : jobNames) {
	        	
	            // SCDF에 Task 정의 생성
	            String taskDefUrl = configProperty.getProperty("spring.cloud.dataflow.client.server-uri") + "/tasks/definitions";
	            
	            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	            params.add("name", jobName);
	            params.add("definition", configProperty.getProperty("spring.application.name") + " --spring.batch.job.names=" + jobName);
	            
	            try {
	                restTemplate.postForEntity(taskDefUrl, params, String.class);
	                this.info("Registered job: " + jobName);
	            } catch (Exception e) {
	            	this.error("Failed to register job: " + jobName);
	            }
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
