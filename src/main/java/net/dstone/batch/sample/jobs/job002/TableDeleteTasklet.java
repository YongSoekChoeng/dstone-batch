package net.dstone.batch.sample.jobs.job002;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

/**
 * 데이블 삭제 Tasklet
 */
@Component
@StepScope
public class TableDeleteTasklet extends BaseItem implements Tasklet{

	private final SqlSessionTemplate sqlSessionSample; 
	
	public TableDeleteTasklet(SqlSessionTemplate sqlSessionSample) {
		this.sqlSessionSample = sqlSessionSample;
	}
	
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	log(this.getClass().getName() + "이(가) 실행됩니다." );
    	this.checkParam();
    	
		// SAMPLE_TEST 테이블 삭제
		String deleteQueryId = "net.dstone.batch.sample.SampleTestDao.deleteSampleTestAll";
		this.sqlSessionSample.delete(deleteQueryId);

        return RepeatStatus.FINISHED;
    }

}
