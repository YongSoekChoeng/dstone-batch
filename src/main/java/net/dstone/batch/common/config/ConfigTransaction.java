package net.dstone.batch.common.config;

import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
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
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 스레드 수 설정
        executor.setCorePoolSize(1);          			// 기본 스레드 수
        executor.setMaxPoolSize(3);           			// 최대 스레드 수
        executor.setQueueCapacity(0);  					// 큐 사용하지 않음 → 즉시 쓰레드 실행

        // 거부 정책 (큐가 가득 찼을 때)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setThreadNamePrefix("batch-default-");

        // ** 이 두 설정이 중요합니다!**
        executor.setWaitForTasksToCompleteOnShutdown(true); 
        // 1시간 대기 설정 (배치 작업 시간에 맞춰 충분히 길게 설정)
        executor.setAwaitTerminationSeconds(60*60*1);
        
        executor.initialize();
        return executor;
    }
    
    // 대용량 처리용 executor (특정 Job만 사용)
    @Bean("heavyTaskExecutor")
    public TaskExecutor heavyTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 수 설정
        executor.setCorePoolSize(5);          			// 기본 스레드 수
        executor.setMaxPoolSize(5);           			// 최대 스레드 수
        executor.setQueueCapacity(0);    				// 큐 사용하지 않음 → 즉시 쓰레드 실행

        // 거부 정책 (큐가 가득 찼을 때)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setThreadNamePrefix("batch-heavy-");

        // ** 이 두 설정이 중요합니다!**
        executor.setWaitForTasksToCompleteOnShutdown(true); 
        // 4시간 대기 설정 (배치 작업 시간에 맞춰 충분히 길게 설정)
        executor.setAwaitTerminationSeconds(60*60*4);
        
        executor.initialize();
        return executor;
    }
    
    
}
