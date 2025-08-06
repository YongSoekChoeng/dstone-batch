package net.dstone.batch.common.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import net.dstone.batch.common.annotation.AutoRegisteredJob;

@Configuration
public class ConfigAutoReg {
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private JobRegistry jobRegistry;

	@PostConstruct
	public void registerJobs() throws Exception {
		try {
			// @AutoRegisteredJob 애노테이션이 붙은 모든 빈 검색
			Map<String, Object> beans = applicationContext.getBeansWithAnnotation(AutoRegisteredJob.class);
			for (Object bean : beans.values()) {
System.out.println( "bean.getClass().getName()=================>>>>" + bean.getClass().getName());				
				// Tasklet 구현체만 처리
				if (bean instanceof Tasklet) {
					String className = bean.getClass().getSimpleName();
					// Step 생성 (클래스명 + "Step")
					Step step = stepBuilderFactory.get(className + "Step").tasklet((Tasklet) bean).build();
					// Job 생성 (클래스명 + "Job") 및 Step 연결
					Job job = jobBuilderFactory.get(className + "Job").start(step).build();
System.out.println( "job=================>>>>" + job);	
					// JobRegistry에 Job 등록
					jobRegistry.register(new ReferenceJobFactory(job));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
