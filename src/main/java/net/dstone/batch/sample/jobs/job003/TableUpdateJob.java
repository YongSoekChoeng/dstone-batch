package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.AbstractJob;

@Component
@AutoRegJob(name = "tableUpdateJob")
public class TableUpdateJob extends AbstractJob {

    private void log(Object msg) {
    	this.debug(msg);
    	//System.out.println(msg);
    }

	@Override
	public void configJob() throws Exception {
		log(this.getClass().getName() + ".configJob() has been called !!!");
		this.addStep(this.createMultiThreadStep("01.멀티쓰레드스텝1", 10, 5));
	}

	private Step createMultiThreadStep(String stepName, int chunkSize, int threadNum) {
		log(this.getClass().getName() + ".createMultiThreadStep("+stepName+") has been called !!!");
		Map<String, Object> params = new HashMap<String, Object>();
		return new StepBuilder(stepName, jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerCommon)
				.reader(itemReader())
				.processor((ItemProcessor<? super Map, ? extends Map>) itemProcessor())
				.writer((ItemWriter<? super Map>) itemWriter())
				.taskExecutor(taskExecutor()) // 스레드 풀 지정 가능
				.throttleLimit(threadNum) // 동시에 실행할 스레드 개수
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
        return new TableUpdateWriter(this.sqlSessionSample);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("batch-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
