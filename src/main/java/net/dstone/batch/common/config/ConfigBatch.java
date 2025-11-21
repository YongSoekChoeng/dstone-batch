package net.dstone.batch.common.config;

import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import net.dstone.batch.common.core.BatchBaseObject;
import net.dstone.common.utils.LogUtil;

@Configuration
public class ConfigBatch extends BatchBaseObject {

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

    private void log(Object msg) {
    	this.info(msg);
    }

    @Bean("jobRepository")
    public JobRepository jobRepository(DataSource dataSource, @Qualifier("txManagerCommon") PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTransactionManager(transactionManager);
        factoryBean.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
        factoryBean.setTablePrefix(configProperty.getProperty("spring.batch.jdbc.table-prefix"));
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Bean("jobExplorer")
    public JobExplorer jobExplorer(DataSource dataSource, @Qualifier("txManagerCommon") PlatformTransactionManager transactionManager) throws Exception {
        JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setTransactionManager(transactionManager);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /*** Job Timeout 관련 컴퍼넌트 시작 ***/
    @Bean("jobRegisterListener")
    public JobExecutionListener jobRegisterListener() {
    	return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
            	this.executeLog(jobExecution);
                JobThreadRegistry.register(jobExecution.getId(), Thread.currentThread());
                (new JobTimeoutKiller()).monitor(jobExecution.getId());
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
            	buff.append("22||======================================= Job["+jobName+"] "+ jobStatus +" =======================================||");
            	buff.append("\n");
            	info(buff.toString());
            }
    	};
    }
    @Component
    private static class JobThreadRegistry {
        private static ConcurrentHashMap<Long, Thread> jobThreads = new ConcurrentHashMap<>();
        public static void register(Long executionId, Thread thread) {
            jobThreads.put(executionId, thread);
        }
        public static void interrupt(Long executionId) {
        	LogUtil.sysout("executionId["+executionId+"] has been interrupted !!!");
            Thread t = jobThreads.get(executionId);
            if (t != null) {
            	t.interrupt();
            }
        }
        public static void unregister(Long executionId) {
            jobThreads.remove(executionId);
        }
    }
    private class JobTimeoutKiller{
        public void monitor(Long executionId) {
            String strBatchTimeout = configProperty.getProperty("app.batch-timeout");
            long timeoutMs = Long.parseLong( (strBatchTimeout == null||"".equals(strBatchTimeout)) ?"3600":strBatchTimeout );

            Thread thread = new Thread( new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(timeoutMs*1000);
						JobThreadRegistry.interrupt(executionId); // 🔥 강제 interrupt
					} catch (Exception ignored) {
					}
				};
            });
            thread.setDaemon(false);
            thread.start();
        }
    }
    /*** Job Timeout 관련 컴퍼넌트 끝 ***/
}
