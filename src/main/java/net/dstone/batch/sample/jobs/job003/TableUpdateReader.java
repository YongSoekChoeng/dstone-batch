package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.ibatis.session.ResultHandler;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class TableUpdateReader extends BatchBaseObject implements ItemReader<Map<String, Object>> {

    private void log(Object msg) {
    	//this.info(msg);
    	this.debug(msg);
    }

    private final SqlSessionTemplate sqlSessionSample;
    
    private volatile int current = 0;
    private volatile boolean selected = false;
    private volatile List<Map<String, Object>> results;
    private final Lock lock = new ReentrantLock();

    public TableUpdateReader(SqlSessionTemplate sqlSessionSample) {
    	this.sqlSessionSample = sqlSessionSample;
    }

    @Override
    public Map<String, Object> read() {
    	log(this.getClass().getName() + ".read() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
    	
		/* Job Parameter 얻어오는 부분 시작 */
		Map<String, Object> params = new HashMap<String, Object>();
		StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();
		JobParameters jobParameters = stepExecution.getJobParameters();
		params.put("timestamp", jobParameters.getLong("timestamp"));
		log("params=================>>>" + params);
		/* Job Parameter 얻어오는 부분 끝 */
		
		this.lock.lock();
		try {
			if(!selected) {
				selected = true;
				results = new CopyOnWriteArrayList<>();
				results.addAll(sqlSessionSample.selectList("net.dstone.batch.sample.SampleTestDao.selectListSampleTest", params));
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
