package net.dstone.batch.sample.jobs.job003;

import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class TableUpdateWriter extends BatchBaseObject implements ItemWriter<Map<String, Object>> {

    private void log(Object msg) {
    	this.debug(msg);
    	//this.info(msg);
    }

    private final SqlSessionTemplate sqlBatchSessionSample;

    public TableUpdateWriter(SqlSessionTemplate sqlBatchSessionSample) {
    	this.sqlBatchSessionSample = sqlBatchSessionSample;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public void write(Chunk<? extends Map<String, Object>> chunk) {
		log(this.getClass().getName() + ".write( chunk.size():"+chunk.size()+" ) has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        int successCount = 0;
        int failCount = 0;
        try (SqlSession session = this.sqlBatchSessionSample.getSqlSessionFactory().openSession(ExecutorType.BATCH)) {
        	for (Map item : chunk) {
                try {
                    // MyBatis Mapper를 통한 UPDATE 실행
                	item.put("TEST_NAME", item.get("TEST_ID")+"-이름");
                	item.put("FLAG_YN", "Y");
                    sqlBatchSessionSample.update("net.dstone.batch.sample.SampleTestDao.updateSampleTest", item);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    this.error("Failed to update TestId: {}, Error: ["+ item.get("TEST_ID") + "]. 상세사항:" + e.toString());
                    throw e; // 트랜잭션 롤백을 위해 예외 재발생
                }
        	}
        	sqlBatchSessionSample.flushStatements();
        }
        log("Write completed - Thread: {"+Thread.currentThread().getName()+"}, Success: {"+successCount+"}, Fail: {"+failCount+"}");
    }

}