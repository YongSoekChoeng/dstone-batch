package net.dstone.batch.common.config;

import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.AbstractJob;
import net.dstone.batch.common.core.BatchBaseObject;

@Configuration
public class ConfigAutoReg extends BatchBaseObject {
	
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private JobRegistry jobRegistry;

	@PostConstruct
	public void registerJobs() throws Exception {
		this.info(this.getClass().getName() + ".registerJobs() has been called !!!");
		try {
			// @AutoRegisteredJob 애노테이션이 붙은 모든 빈 검색
			Map<String, Object> jobs = applicationContext.getBeansWithAnnotation(AutoRegJob.class);
			
			for(Object jobObj : jobs.values()) {
				
				if (jobObj instanceof AbstractJob) {
					AbstractJob abstractJob = (AbstractJob)jobObj;
					String jobName = jobObj.getClass().getAnnotation(AutoRegJob.class).name();
					abstractJob.setName(jobName);
					Job job = abstractJob.buildAutoRegJob();
					jobRegistry.register(new ReferenceJobFactory(job));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
