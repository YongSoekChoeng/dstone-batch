package net.dstone.batch.common.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class ConfigTransaction extends BatchBaseObject{

	/********************************************************************************
	1. TransactionManager 관련 설정(Spring 내부적으로 필수로 transactionManager를 사용하는 경우가 있으므로 두개의 이름을 지정)
	********************************************************************************/
	@Bean(name = {"transactionManager", "txManagerCommon"})
	public PlatformTransactionManager txManagerCommon(@Qualifier("dataSourceCommon") DataSource dataSourceCommon) {
		PlatformTransactionManager txManagerCommon = new DataSourceTransactionManager(dataSourceCommon);
		return txManagerCommon;
	}

	/********************************************************************************
	2. Sample DataSource TransactionManager 관련 설정
	********************************************************************************/
	@Bean(name = "txManagerSample")
	public PlatformTransactionManager txManagerSample(@Qualifier("dataSourceSample") DataSource dataSourceSample) {
		PlatformTransactionManager txManagerSample = new DataSourceTransactionManager(dataSourceSample);
		return txManagerSample;
	}


	/********************************************************************************
	3. TaskExecutor 관련 설정
	********************************************************************************/
    // 기본 executor (대부분의 Job이 사용)	
    @Bean
    @Primary
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setThreadNamePrefix("batch-default-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    // 대용량 처리용 executor (특정 Job만 사용)
    @Bean("heavyTaskExecutor")
    public TaskExecutor heavyTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setThreadNamePrefix("batch-heavy-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }
    
    
}
