package net.dstone.batch.common.items;

import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

/**
 * DB핸들링을 위한 ItemWriter 구현체. 
 * <pre>
 * 멀티쓰레드에서 writer 는 메서드호출 방식이 아닌, 프록시주입(Lazy) 형식으로 해야 함.
 * 예)
 * new StepBuilder("parallelSlaveStep", jobRepository)
 * ....
 * .writer(itemWriter()) ==>> X
 * .writer(itemWriter)   ==>> O
 * </pre>
 */
@Component
@StepScope
public class TableItemWriter extends BaseItem implements ItemStreamWriter<Map<String, Object>> {

    /**************************************** 생성자-주입 멤버선언 시작 ****************************************/
	// SqlSessionTemplate : SqlSessionTemplate. 생성자로 주입.
	// queryId : MyBatis 쿼리 ID. 생성자로 주입.
    private final SqlSessionTemplate sqlSessionTemplate;
    private String queryId;
    /**************************************** 생성자-주입 멤버선언 끝 ****************************************/

    public TableItemWriter(SqlSessionTemplate sqlSessionTemplate, String queryId) {
    	this.sqlSessionTemplate = sqlSessionTemplate;
    	this.queryId = queryId;
    }

	@SuppressWarnings({ "rawtypes" })
	@Override
    public void write(Chunk<? extends Map<String, Object>> chunk) {
		callLog(this, "write", "chunk[size:"+chunk.size()+"]");
    	//this.checkParam();
        int successCount = 0;
        int failCount = 0;
        try (SqlSession session = this.sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH)) {
        	for (Map item : chunk) {
                try {
                    // MyBatis Mapper를 통한 UPDATE 실행
                	this.sqlSessionTemplate.update(this.queryId, item);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    this.error("Failed to update TestId: {}, Error: ["+ item.get("TEST_ID") + "]. 상세사항:" + e.toString());
                    throw e; // 트랜잭션 롤백을 위해 예외 재발생
                }
        	}
        	this.sqlSessionTemplate.flushStatements();
        }
        this.log("Write completed - Thread: {"+Thread.currentThread().getName()+"}, Success: {"+successCount+"}, Fail: {"+failCount+"}");
    }

}