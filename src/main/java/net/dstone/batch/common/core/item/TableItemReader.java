package net.dstone.batch.common.core.item;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.stereotype.Component;

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
 * │  reader.open(executionContext)      │  ◀── Step 시작 시 1회 자동으로 호출
 * └─────────────────────────────────────┘
 *     │
 *     ▼
 * ┌─────────────────────────────────────┐
 * │  Chunk 반복 (chunk size: 1000 기준)   │
 * │                                     │
 * │  ┌───────────────────────────────┐  │
 * │  │ reader.read() × 1000          │  │  ◀── null 반환될 때까지 반복
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

    /** open/close 중복 방지용 플래그 */
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public TableItemReader(SqlSessionFactory sqlSessionFactory, String queryId) {
    	this.sqlSessionFactory = sqlSessionFactory;
    	this.queryId = queryId;
    }

    public TableItemReader(SqlSessionFactory sqlSessionFactory, String queryId, Map<String, Object> params) {
    	this.sqlSessionFactory = sqlSessionFactory;
    	this.queryId = queryId;
    	if( params!=null ) {
    		this.params.putAll(params);
    	}
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
    	log(this.getClass().getName() + ".open() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        if (opened.compareAndSet(false, true)) {
            try {
                this.sqlSession = this.sqlSessionFactory.openSession();
                this.cursor = this.sqlSession.selectCursor(queryId, params);
                this.iterator = this.cursor.iterator();
                log(">>> Cursor 열기 성공");
            } catch (Exception e) {
            	log(">>> Cursor 열기 실패. 상세사항:" + e);
            	close();
                throw new ItemStreamException("Cursor 열기 실패: " + queryId, e);
            }
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
        	return item;
        }
        return null;
    }

    @Override
    public void close() throws ItemStreamException {
    	super.log(this.getClass().getName() + ".close() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
    	if (closed.compareAndSet(false, true)) {
    		this.closeQuietly();
    	}
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
