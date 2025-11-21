package net.dstone.batch.common.core.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
@StepScope
public class TableItemReader extends BaseItem implements ItemReader<Map<String, Object>> {

    private final SqlSessionTemplate sqlSessionSample;
    private final String queryId;
    
    private volatile int current = 0;
    private volatile boolean selected = false;
    private volatile List<Map<String, Object>> results;
    private final Lock lock = new ReentrantLock();

    public TableItemReader(SqlSessionTemplate sqlSessionSample, String queryId) {
    	this.sqlSessionSample = sqlSessionSample;
    	this.queryId = queryId;
    }

    public TableItemReader(SqlSessionTemplate sqlSessionSample, String queryId, Map<String, Object> params) {
    	this.sqlSessionSample = sqlSessionSample;
    	this.queryId = queryId;
    	if( params!=null ) {
    		this.params.putAll(params);
    	}
    }

    @Override
    public Map<String, Object> read() {
    	super.log(this.getClass().getName() + ".read() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
    	
		this.lock.lock();
		try {
			if(!selected) {
				selected = true;
				results = new CopyOnWriteArrayList<>();
				if( this.params != null ) {
					results.addAll(sqlSessionSample.selectList(this.queryId, this.params));
				}else {
					results.addAll(sqlSessionSample.selectList(this.queryId));
				}
			}
			int next = current++;
			if (next < results.size()) {
				return results.get(next);
			}else {
				return null;
			}
		}finally {
			this.lock.unlock();
		}
    }

}
