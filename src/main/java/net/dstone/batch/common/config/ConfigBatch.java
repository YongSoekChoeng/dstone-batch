package net.dstone.batch.common.config;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import net.dstone.batch.common.annotation.AutoRegFlow;
import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.annotation.AutoRegStep;
import net.dstone.batch.common.core.BatchBaseObject;

@Configuration
@EnableBatchProcessing
public class ConfigBatch extends BatchBaseObject implements BatchConfigurer {

	/********************************** DB 관련설정 시작 **********************************/
	@Autowired
	@Qualifier("dataSourceSample")
	private DataSource dataSourceSample;
	
	@Bean(name = "txManagerBatch")
	public PlatformTransactionManager txManagerBatch() {
		PlatformTransactionManager txManagerBatch = new DataSourceTransactionManager(this.dataSourceSample);
		return txManagerBatch;
	}
    
	@Override
	public JobRepository getJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(this.dataSourceSample);
        factory.setTransactionManager(this.txManagerBatch());
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
        factory.afterPropertiesSet();
        return factory.getObject();
	}

	@Override
	public PlatformTransactionManager getTransactionManager() throws Exception {
		return this.txManagerBatch();
	}

	@Override
	public JobLauncher getJobLauncher() throws Exception {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(getJobRepository());
        launcher.afterPropertiesSet();
        return launcher;
	}

	@Override
	public JobExplorer getJobExplorer() throws Exception {
        JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
        factoryBean.setDataSource(this.dataSourceSample);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
	}
	/********************************** DB 관련설정 끝 **********************************/

	/********************************** JOB 관련설정 시작 **********************************/

	@PostConstruct
	public void registerJobs(){
		try{
			// AutoRegJob 정보를 담을 구조체
			Map<String, List<Object>> jobList = new HashMap<String, List<Object>>();
			Iterator<String> keys = null;
			
			// @AutoRegJob 애노테이션이 붙은 모든 빈 검색
			Map<String, Object> jobs = applicationContext.getBeansWithAnnotation(AutoRegJob.class);
			// @AutoRegStep 애노테이션이 붙은 모든 빈 검색
			Map<String, Object> steps = applicationContext.getBeansWithAnnotation(AutoRegStep.class);
			// @AutoRegFlow 애노테이션이 붙은 모든 빈 검색
			Map<String, Object> flows = applicationContext.getBeansWithAnnotation(AutoRegFlow.class);

			// 1. Job 별로 Items(Step, Flow) Grouping
			for(Object jobObj : jobs.values()) {
				String jobName = jobObj.getClass().getAnnotation(AutoRegJob.class).name();
				List<Object> jobItems = new LinkedList<Object>();
				for(Object stepObj : steps.values()) {
					if( stepObj.getClass().getAnnotation(AutoRegStep.class) != null) {
						jobItems.add(stepObj);
					}else if( stepObj.getClass().getAnnotation(AutoRegFlow.class) != null) {
						jobItems.add(stepObj);
					}
				}
				jobList.put(jobName, jobItems);
			}
			// 2. Job 별로 Items(Step, Flow) Sorting
			keys = jobList.keySet().iterator();
			while( keys.hasNext() ) {
				String jobName = keys.next();
				List<Object> jobItems = jobList.get(jobName);
				jobItems.sort(Comparator.comparingInt(bean -> {
				    if ( bean.getClass().getAnnotation(AutoRegStep.class) != null) {
				        return bean.getClass().getAnnotation(AutoRegStep.class).order();
				    } else if ( bean.getClass().getAnnotation(AutoRegFlow.class) != null) {
				    	return bean.getClass().getAnnotation(AutoRegFlow.class).order();
				    } else {
				    	return 0;
				    }
				}));
			}
			// 3. Job 자동 등록.
			keys = jobList.keySet().iterator();
			while( keys.hasNext() ) {

				String jobName = keys.next();
				JobBuilder jobBuilder = jobBuilderFactory.get(jobName);
				Job job = null;

				List<Object> jobItems = jobList.get(jobName);
				Object jobItem = null;
				
				FlowBuilder<Flow> flowBuilder = null;
				
				if(jobItems.size() > 0) {
					// start()는 반드시 첫 요소로 호출
					jobItem = jobItems.get(0);
					
				    if (jobItem instanceof Step) {
				    	flowBuilder = new FlowBuilder<Flow>(jobItem.getClass().getAnnotation(AutoRegStep.class).name()).start((Step) jobItem);
				    } else if (jobItem instanceof Flow) {
				    	flowBuilder = new FlowBuilder<Flow>(jobItem.getClass().getAnnotation(AutoRegFlow.class).name()).start((Flow) jobItem);
				    } else {
				        throw new IllegalArgumentException("Unsupported component type: " + jobItem.getClass());
				    }
					// 두번째 이후 요소는 next()호출
					for(int i=1; i<jobItems.size(); i++) {
						jobItem = jobItems.get(i);
					    if (jobItem instanceof Step) {
					    	flowBuilder = new FlowBuilder<Flow>(jobItem.getClass().getAnnotation(AutoRegStep.class).name()).next((Step) jobItem);
					    } else if (jobItem instanceof Flow) {
					    	flowBuilder = new FlowBuilder<Flow>(jobItem.getClass().getAnnotation(AutoRegFlow.class).name()).next((Flow) jobItem);
					    } else {
					        throw new IllegalArgumentException("Unsupported component type: " + jobItem.getClass());
					    }
					}

					Flow flow = flowBuilder.build();
					job = jobBuilder.start(flow).end().build();

					this.info("jobRegistry.register===============================>>> line 172 jobs:" + job);
					jobRegistry.register(new ReferenceJobFactory(job));
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/********************************** JOB 관련설정 끝 **********************************/

}
