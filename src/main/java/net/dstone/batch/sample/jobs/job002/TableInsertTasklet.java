package net.dstone.batch.sample.jobs.job002;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;
import net.dstone.common.utils.DateUtil;

@Component
public class TableInsertTasklet extends BaseItem implements Tasklet{

	private final SqlSessionTemplate sqlSessionSample; 
	private final int threadCount = 5;
	
	public TableInsertTasklet(SqlSessionTemplate sqlSessionSample) {
		this.sqlSessionSample = sqlSessionSample;
	}
	
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	log(this.getClass().getName() + "이(가) 실행됩니다.");
    	
		// SAMPLE_TEST 테이블 삭제
		String deleteQueryId = "net.dstone.batch.sample.SampleTestDao.deleteSampleTestAll";
		this.sqlSessionSample.delete(deleteQueryId);
		this.sqlSessionSample.flushStatements();
		
		// SAMPLE_TEST 테이블 입력
		int dataCnt = Integer.parseInt(this.getParam("dataCnt", "1000").toString());
		final String insertQueryId = "net.dstone.batch.sample.SampleTestDao.insertSampleTest";

        int chunkPerThread = dataCnt / threadCount;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();
        

        for (int t = 0; t < threadCount; t++) {
            int startIdx = t * chunkPerThread;
            int endIdx = (t == threadCount - 1) ? dataCnt : (t + 1) * chunkPerThread;
            
            futures.add(executor.submit(() -> {
                // ✅ try 블록 안에서 모든 작업 수행
                try (SqlSession session = this.sqlSessionSample.getSqlSessionFactory().openSession(ExecutorType.BATCH, false)) {
                    
                    int batchSize = 1000;
                    for (int i = startIdx; i < endIdx; i++) {
                        Map<String, String> row = new HashMap<>();
                        row.put("TEST_ID", String.valueOf(i));
                        row.put("TEST_NAME", "이름-" + i);
                        row.put("FLAG_YN", "N");
                        row.put("INPUT_DT", DateUtil.getToDate("yyyyMMddHHmmss"));
                        
                        session.insert(insertQueryId, row);
                        
                        if ((i - startIdx + 1) % batchSize == 0) {
                            session.flushStatements();
                            session.commit();
                        }
                    }
                    session.flushStatements();
                    session.commit();
                    
                    this.log("Completed: {"+startIdx+"} - {"+endIdx+"}");
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }
        // 모든 Future 완료 대기
        for (Future<?> future : futures) {
            future.get();  // 예외 발생 시 여기서 throw
        }

        executor.shutdown();
        executor.awaitTermination(60*10, TimeUnit.SECONDS);

        return RepeatStatus.FINISHED;
    }

}
