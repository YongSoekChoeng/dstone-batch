package net.dstone.batch.common.items;

import java.util.Map;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public class TableItemWriter extends BaseItem implements ItemWriter<Map<String, Object>> {

    private final SqlSessionTemplate sqlSessionTemplate;
    private String queryId;

    public TableItemWriter(SqlSessionTemplate sqlSessionTemplate, String queryId) {
    	this.sqlSessionTemplate = sqlSessionTemplate;
    	this.queryId = queryId;
    }

	@SuppressWarnings({ "rawtypes" })
	@Override
    public void write(Chunk<? extends Map<String, Object>> chunk) {
		this.log(this.getClass().getName() + ".write( chunk.size():"+chunk.size()+" ) has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
    	this.checkParam();
    	
        int successCount = 0;
        int failCount = 0;
        try (SqlSession session = this.sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH)) {

try {
	java.sql.Connection connection = session.getConnection();
	java.sql.DatabaseMetaData metaData = connection.getMetaData();
	// 4. 메타 정보를 통해 DB 종류와 버전 조회
    String dbProductName = metaData.getDatabaseProductName(); // DB 종류 (예: Oracle, MySQL, PostgreSQL)
    String dbProductVersion = metaData.getDatabaseProductVersion(); // DB 버전 (예: 12.2.0.1.0, 8.0.28)

    System.out.println("✅ Database Product Name: " + dbProductName);
    System.out.println("✅ Database Product Version: " + dbProductVersion);
}catch(Exception e) {}

        	
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