package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.AbstractJob;

@Component
@AutoRegJob(name = "tableUpdateJob")
public class TableUpdateJob extends AbstractJob {

    private void log(Object msg) {
    	this.info(msg);
    	//System.out.println(msg);
    }

    @Autowired
    private TableUpdateReader tableUpdateReader;
    @Autowired
    private TableUpdateProcessor tableUpdateProcessor;
    @Autowired
    private TableUpdateWriter tableUpdateWriter;

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
				.reader(tableUpdateReader.read(params))
				.processor((ItemProcessor<? super Map, ? extends Map>) tableUpdateProcessor)
				.writer((ItemWriter<? super Map>) tableUpdateWriter)
				.taskExecutor(new SimpleAsyncTaskExecutor()) // 스레드 풀 지정 가능
				.throttleLimit(threadNum) // 동시에 실행할 스레드 개수
				.build();
	}

}
