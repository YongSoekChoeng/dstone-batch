package net.dstone.batch.common.core.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
@StepScope
public class TableItemWriter extends BatchBaseObject implements ItemWriter<Map<String, Object>> {

    private void log(Object msg) {
    	//this.debug(msg);
    	this.info(msg);
    }

    private final SqlSessionTemplate sqlSessionSample;
    private String queryId;
	protected Map<String, Object> params = new HashMap<String, Object>();

    public TableItemWriter(SqlSessionTemplate sqlSessionSample, String queryId) {
    	this.sqlSessionSample = sqlSessionSample;
    	this.queryId = queryId;
    }

	@SuppressWarnings({ "rawtypes" })
	@Override
    public void write(Chunk<? extends Map<String, Object>> chunk) {
		log(this.getClass().getName() + ".write( chunk.size():"+chunk.size()+" ) has been called !!! params["+this.params+"] - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        int successCount = 0;
        int failCount = 0;
        try (SqlSession session = this.sqlSessionSample.getSqlSessionFactory().openSession(ExecutorType.BATCH)) {
        	for (Map item : chunk) {
                try {
                    // MyBatis Mapper를 통한 UPDATE 실행
                	this.sqlSessionSample.update(this.queryId, item);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    this.error("Failed to update TestId: {}, Error: ["+ item.get("TEST_ID") + "]. 상세사항:" + e.toString());
                    throw e; // 트랜잭션 롤백을 위해 예외 재발생
                }
        	}
        	this.sqlSessionSample.flushStatements();
        }
        log("Write completed - Thread: {"+Thread.currentThread().getName()+"}, Success: {"+successCount+"}, Fail: {"+failCount+"}");
    }

    @BeforeStep
    protected void beforeStep(StepExecution stepExecution) {
    	JobParameters jobParameters = stepExecution.getJobParameters();
    	if( jobParameters != null ) {
    		Map<String, JobParameter<?>> jobParamMap = jobParameters.getParameters();
    		Iterator<String > jobParamMapKey = jobParamMap.keySet().iterator();
    		while(jobParamMapKey.hasNext()) {
    			String key = jobParamMapKey.next();
    			JobParameter val = jobParamMap.get(key);
    			this.params.put(key, val.getValue());
    		}
    	}
    }
    
}