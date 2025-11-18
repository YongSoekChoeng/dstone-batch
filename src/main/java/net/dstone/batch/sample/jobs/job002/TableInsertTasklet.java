package net.dstone.batch.sample.jobs.job002;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;
import net.dstone.common.utils.DateUtil;
import net.dstone.common.utils.GuidUtil;

@Component
public class TableInsertTasklet extends BatchBaseObject implements Tasklet{

	private final SqlSessionTemplate sqlSessionSample; 
	
	public TableInsertTasklet(SqlSessionTemplate sqlSessionSample) {
		this.sqlSessionSample = sqlSessionSample;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		this.info(this.getClass().getName() + "이(가) 실행됩니다.");
		
		// SAMPLE_TEST 테이블 삭제
		String queryId = "net.dstone.batch.sample.SampleTestDao.deleteSampleTestAll";
		this.sqlSessionSample.delete(queryId);
		
		// SAMPLE_TEST 테이블 입력
		JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
		int dataCnt = Integer.parseInt(jobParameters.getString("dataCnt", "100"));
		
		queryId = "net.dstone.batch.sample.SampleTestDao.insertSampleTest";
		GuidUtil guidUtil = new GuidUtil();
		for(int i=0; i<dataCnt; i++) {
			Map<String, String> row = new HashMap<String, String>();
			row.put("TEST_ID", guidUtil.getNewGuid().substring(0, 8));
			row.put("TEST_NAME", row.get("TEST_ID"));
			row.put("FLAG_YN", "N");
			row.put("INPUT_DT", DateUtil.getToDate("yyyyMMddHHmmss"));
			this.sqlSessionSample.insert(queryId, row);
		}
		
		this.info(this.getClass().getName()  + "이(가) 종료됩니다.");
		return RepeatStatus.FINISHED;
	}
}
