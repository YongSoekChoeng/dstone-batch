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
import net.dstone.common.utils.StringUtil;

@Configuration
public class ConfigAutoReg extends BaseBatchObject {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private JobRegistry jobRegistry;

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean
	
	@PostConstruct
	public void autoRegJob() throws Exception {
        boolean autoRegisterJobs = Boolean.valueOf(StringUtil.ifEmpty(configProperty.getProperty("spring.application.auto-register-jobs"), "true"));
        if(autoRegisterJobs) {
        	registerAllJobs();
        }
	}

	/**
	 * AutoRegJob 어노테이션의 모든 Job들을 Job Registry에 등록하는 메소드.
	 * @throws Exception
	 */
	public void registerAllJobs() throws Exception {
		this.info(this.getClass().getName() + ".registerAllJobs() has been called !!!");
		try {
			// @AutoRegisteredJob 애노테이션이 붙은 모든 빈 검색
			Map<String, Object> jobs = applicationContext.getBeansWithAnnotation(AutoRegJob.class);
			for(Object jobObj : jobs.values()) {
				if (jobObj instanceof BaseJobConfig) {
					BaseJobConfig abstractJob = (BaseJobConfig)jobObj;
					String jobName = jobObj.getClass().getAnnotation(AutoRegJob.class).name();
					abstractJob.setName(jobName);
					Job job = abstractJob.buildAutoRegJob();
					ReferenceJobFactory factory = new ReferenceJobFactory(job);
					jobRegistry.register(factory);
				}
			}
			if( "true".equals(configProperty.getProperty("spring.cloud.dataflow.job-auto-register")) ) {
				this.registerAllJobsToDataflow();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * JobName을 파라메터로 받아서 AutoRegJob 어노테이션으로 등록되어 있을 경우 그 Job을 Job Registry에 등록하는 메소드.
	 * @throws Exception
	 */
	public void registerJob(String jobName) throws Exception {
		this.info(this.getClass().getName() + ".registerJob("+jobName+") has been called !!!");
		try {
			// @AutoRegisteredJob 애노테이션이 붙은 모든 빈 검색
			Map<String, Object> jobs = applicationContext.getBeansWithAnnotation(AutoRegJob.class);
			for(Object jobObj : jobs.values()) {
				if (jobObj instanceof BaseJobConfig) {
					BaseJobConfig abstractJob = (BaseJobConfig)jobObj;
					String autoRegJobName = jobObj.getClass().getAnnotation(AutoRegJob.class).name();
					if( autoRegJobName.equals(jobName) ) {
						abstractJob.setName(jobName);
						Job job = abstractJob.buildAutoRegJob();
						ReferenceJobFactory factory = new ReferenceJobFactory(job);
						jobRegistry.register(factory);
						if( "true".equals(configProperty.getProperty("spring.cloud.dataflow.job-auto-register")) ) {
							this.registerJobToDataflow(jobName);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * SCDF에 Job Registry 의 모든 Task를 등록하는 메소드
	 */
	private void registerAllJobsToDataflow() {
		try {
	        // 등록된 모든 Job 이름 가져오기
	        Collection<String> jobNames = jobRegistry.getJobNames();
	        for (String jobName : jobNames) {
	        	this.registerJobToDataflow(jobName);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * SCDF에 Job 의 Task를 등록하는 메소드
	 */
	private void registerJobToDataflow(String jobName) {
		RestTemplate restTemplate = null;
		try {
			restTemplate = net.dstone.common.utils.RestFulUtil.getInstance().getRestTemplate();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
