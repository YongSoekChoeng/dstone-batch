package net.dstone.batch.sample.jobs.job002;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import net.dstone.batch.common.core.BatchBaseObject;
import net.dstone.common.utils.FileUtil;

public class SingleTasklet extends BatchBaseObject implements Tasklet{
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		this.info(this.getClass().getName() + "이(가) 실행됩니다.");
		Thread.sleep(Integer.parseInt(net.dstone.common.utils.StringUtil.getRandomNumber(1)) * 1000);
		
		String threadId = String.valueOf(Thread.currentThread().threadId());
    	FileUtil.writeFile("C:/Temp/SampleItem", "singleTaskletItem["+threadId+"].txt", threadId);
        
		this.info(this.getClass().getName()  + "이(가) 종료됩니다.");
		return RepeatStatus.FINISHED;
	}
}
