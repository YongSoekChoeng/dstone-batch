package net.dstone.batch.common.items;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

/**
 * ItemReader 구현체. 
 * 아래와 같은 흐름을 갖는다.
 * 
 * Job 시작
 *     │
 *     ▼
 * Step 시작
 *     │
 *     ▼
 * ┌─────────────────────────────────────┐
 * │  reader.open(executionContext)      │  ◀── Step 시작 시 1회 자동으로 호출. 이때의 ExecutionContext는 ItemStream 관리용context로 StepExecutionContext와는 별개임.(여기서 Step파라메터 세팅은 Step내에서 공유되지 않음)
 * └─────────────────────────────────────┘
 *     │
 *     ▼
 * ┌─────────────────────────────────────┐
 * │  Chunk 반복 (chunk size: 1000 기준)   │
 * │                                     │
 * │  ┌───────────────────────────────┐  │
 * │  │ reader.read() × 1000          │  │  ◀── null 반환될 때까지 반복. 여기서 비로소 StepExecutionContext가 생성됨.(여기서 Step파라메터 세팅은 Step내에서 공유됨)
 * │  │ processor.process() × 1000    │  │
 * │  │ writer.write(items)           │  │  
 * │  │ reader.update(context)        │  │  ◀── 매 chunk 커밋 후 자동으로 호출
 * │  └───────────────────────────────┘  │
 * │              ...반복...              │
 * └─────────────────────────────────────┘
 *     │
 *     ▼
 * ┌─────────────────────────────────────┐
 * │  reader.close()                     │  ◀── Step 종료 시 1회 자동으로 호출
 * └─────────────────────────────────────┘
 *     │
 *     ▼
 * Step 종료
 * 
 */
@Component
@StepScope
public class TableItemReader extends BaseItem implements ItemReader<Map<String, Object>>, ItemStream {

    private final SqlSessionFactory sqlSessionFactory;
    private final String queryId;

    private SqlSession sqlSession;
    private Cursor<Map<String, Object>> cursor;
    private Iterator<Map<String, Object>> iterator;
    int readCnt = 0;
    
    public TableItemReader(SqlSessionFactory sqlSessionFactory, String queryId) {
    	this.sqlSessionFactory = sqlSessionFactory;
    	this.queryId = queryId;
    }

    public TableItemReader(SqlSessionFactory sqlSessionFactory, String queryId, Map<String, Object> baseParams) {
    	this.sqlSessionFactory = sqlSessionFactory;
    	this.queryId = queryId;
    	if(baseParams != null && baseParams.size() > 0) {
    		this.baseParam.putAll(baseParams);
    	}
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
    	log(this.getClass().getName() + ".open() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        try {

        	Map<String,Object> paramMap = new HashMap<String,Object>();
        	
        	Map<String,Object> stepParamMap = this.getStepParamMap();
        	if( stepParamMap != null ) {
        		paramMap.putAll(stepParamMap);
        	}
        	
        	Map<String,Object> executionMap = this.stepExecution.getExecutionContext().toMap();
        	if( executionMap != null ) {
        		paramMap.putAll(executionMap);
        	}
        	
            this.sqlSession = this.sqlSessionFactory.openSession();
            this.cursor = this.sqlSession.selectCursor(queryId, paramMap);
            this.iterator = this.cursor.iterator();
            log(">>> Cursor 열기 성공");
        } catch (Exception e) {
        	log(">>> Cursor 열기 실패. 상세사항:" + e);
        	close();
            throw new ItemStreamException("Cursor 열기 실패: " + queryId, e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    	log(this.getClass().getName() + ".update() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        // TO-DO : 향후 필요하면 구현
    }

    @Override
    public Map<String, Object> read() {
    	log(this.getClass().getName() + ".read() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        if (this.cursor == null) {
        	return null;
        }
        if (this.iterator != null && this.iterator.hasNext()) {
			Map<String, Object> item = this.iterator.next();
			readCnt++;
			this.setStepParam("readCnt", readCnt);
        	return item;
        }
        return null;
    }

    @Override
    public void close() throws ItemStreamException {
    	super.log(this.getClass().getName() + ".close() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
    	this.closeQuietly();
    }

    private void closeQuietly() {
        try {
            if (this.cursor != null) {
                try {
                	this.cursor.close();
                } catch (Exception e) {}
                this.cursor = null;
            }
            if (this.sqlSession != null) {
                try {
                	this.sqlSession.close();
                } catch (Exception e) {}
                this.sqlSession = null;
            }
            this.iterator = null;
        } finally {}
    }
    
}
