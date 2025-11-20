package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.AbstractJob;

@Component
@AutoRegJob(name = "tableUpdateJob")
public class TableUpdateJob extends AbstractJob {

    private void log(Object msg) {
    	this.info(msg);
    }

	@Override
	public void configJob() throws Exception {
		log(this.getClass().getName() + ".configJob() has been called !!!");
		int chunkSize = 1000;
		this.addStep(this.createStepByOperator("01.Reader/Processor/Writer 별도클래스로 생성 스텝", chunkSize));
		//this.addStep(this.createStepInAll("02.Reader/Processor/Writer 동일클래스내에 생성 스텝", chunkSize));
	}

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    
    /**************************************** 01.Reader/Processor/Writer 별도클래스로 생성 ****************************************/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Step createStepByOperator(String stepName, int chunkSize) {
		log(this.getClass().getName() + ".createStepByOperator("+stepName+", "+chunkSize+" ) has been called !!!");
		return new StepBuilder(stepName, jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerCommon)
				.reader(itemReader())
				.processor((ItemProcessor<? super Map, ? extends Map>) itemProcessor())
				.writer((ItemWriter<? super Map>) itemWriter())
				.taskExecutor(taskExecutor()) // 스레드 풀 지정 가능
				.build();
	}

    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemReader() {
        return new TableUpdateReader(this.sqlSessionSample);
    }

    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
        return new TableUpdateProcessor();
    }

    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> itemWriter() {
        return new TableUpdateWriter(this.sqlBatchSessionSample);
    }
    /*************************************************************************************************************************/
    
    /*************************************** 02.Reader/Processor/Writer 동일클래스내에 생성 ***************************************/
    public Step createStepInAll(String stepName, int chunkSize) {
        return new StepBuilder(stepName, jobRepository)
                .<Map<String, Object>, Map<String, Object>>chunk(chunkSize, txManagerCommon)
                .reader(tableUpdateReader(chunkSize))
                .processor(tableUpdateProcessor())
                .writer(tableUpdateWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public MyBatisPagingItemReader<Map<String, Object>> tableUpdateReader(int chunkSize) {
        Map<String, Object> params = new HashMap<>();
        // 필요시 파라미터 추가 가능
        return new MyBatisPagingItemReaderBuilder<Map<String, Object>>()
                .sqlSessionFactory(this.sqlSessionSample.getSqlSessionFactory())
                .queryId("net.dstone.batch.sample.SampleTestDao.selectListSampleTestPaging")
                .pageSize(chunkSize)
                .build(); 
    }

    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> tableUpdateProcessor() {
        return item -> {
            // 데이터 수정 로직
            String testId = (String) item.get("TEST_ID");
            String testName = (String) item.get("TEST_NAME");
            
            // FLAG_YN을 'Y'로 변경
            item.put("FLAG_YN", "Y");
            
            // 수정된 데이터 반환
            return item;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> tableUpdateWriter() {
        return items -> {
            // MyBatis를 사용한 배치 업데이트
            for (Map<String, Object> item : items) {
            	sqlBatchSessionSample.update("net.dstone.batch.sample.SampleTestDao.updateSampleTest", item );
            }
        };
    }
    /*************************************************************************************************************************/

}
