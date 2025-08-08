package net.dstone.batch.sample.jobs;

import java.util.Arrays;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.AbstractJob;

@Component
@AutoRegJob(name = "sampleJob")
public class SampleJob extends AbstractJob {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

	@Override
	public void configJob() throws Exception {
		this.addStep(this.createStep("스텝1"));
		this.addStep(this.createStep("스텝2"));
		this.addStep(this.createMultiThreadStep("멀티쓰레드스텝1", 2, 2, new SimpleItemReader<>(Arrays.asList("hello", "world", "spring", "batch", "example")), new SimpleItemProcessor(), new SimpleItemWriter()));
		this.addFlow(this.createSimpleFlow("심플플로우1"));
		this.addFlow(this.createSplitFlow("스프릿플로우1"));
		this.addTasklet(this.createTasklet("타스크렛1"));
	}

	private Step createStep(String stepName) {
		return stepBuilderFactory.get(stepName).tasklet(new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println(stepName + "이(가) 실행됩니다.");
				Thread.sleep(Integer.parseInt(net.dstone.common.utils.StringUtil.getRandomNumber(1)) * 1000);
				System.out.println(stepName + "이(가) 종료됩니다.");
				
				// 파라메터 전달(세팅)
	            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("NAME", "홍길동");
				
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	private Step createMultiThreadStep(String stepName, int chunkSize, int threadNum, ItemReader<String> reader, ItemProcessor<String, String> processor, ItemWriter<String> writer) {
		return stepBuilderFactory.get(stepName).<String, String>chunk(chunkSize)
				.reader(reader)
				.processor(processor)
				.writer(writer).taskExecutor(new SimpleAsyncTaskExecutor()) // 스레드 풀 지정 가능
				.throttleLimit(threadNum) // 동시에 실행할 스레드 개수
				.build();
	}
	
	private Flow createSimpleFlow(String flowName) {
	    return new FlowBuilder<SimpleFlow>(flowName)
	            .start(this.createStep(flowName + "-스텝3"))
	            .next(this.createStep(flowName + "-스텝4"))
	            .end();
	}
	
	private Flow createSplitFlow(String flowName) {
	    return new FlowBuilder<SimpleFlow>(flowName)
	            .split(new SimpleAsyncTaskExecutor())
	            .add(createSimpleFlow(flowName + "-스프릿서브플로우1"), createSimpleFlow(flowName + "-스프릿서브플로우2"))
	            .build(); // 내부적으로 SplitFlow 생성
	}

	private Tasklet createTasklet(String taskletName) {
	    return new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println(taskletName + "이(가) 실행됩니다.");

				// 파라메터 전달(조회)
	            String paramName = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("NAME").toString();
	            System.out.println("paramName["+paramName+"]");
				
				Thread.sleep(Integer.parseInt(net.dstone.common.utils.StringUtil.getRandomNumber(1)) * 1000);
				System.out.println(taskletName + "이(가) 종료됩니다.");
				return RepeatStatus.FINISHED;
			}
		};
	}

}
