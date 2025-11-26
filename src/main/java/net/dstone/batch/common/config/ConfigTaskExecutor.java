package net.dstone.batch.common.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import net.dstone.batch.common.core.BaseBatchObject;

@Configuration
public class ConfigTaskExecutor extends BaseBatchObject{

	/********************************************************************************
	TaskExecutor 관련 설정
	********************************************************************************/
    // 기본 executor (대부분의 Job이 사용)	
    @Bean
    @Qualifier("taskExecutor")
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
    @Bean
    @Qualifier("heavyTaskExecutor")
    public TaskExecutor heavyTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 스레드 수 설정
        executor.setCorePoolSize(2);          			// 기본 스레드 수
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
