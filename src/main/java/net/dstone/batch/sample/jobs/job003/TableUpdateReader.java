package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private Iterator<Map<String, Object>> iterator;
    private final ConcurrentLinkedQueue<Map<String, Object>> queue = new ConcurrentLinkedQueue<>();

    public TableUpdateReader(SqlSessionTemplate sqlSessionSample) {
    	this.sqlSessionSample = sqlSessionSample;
    }

    @Override
    public Map<String, Object> read() {
    	log(this.getClass().getName() + ".read() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
    	
		if (iterator == null) {
			/* Job Parameter 얻어오는 부분 시작 */
			Map<String, Object> params = new HashMap<String, Object>();
			StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();
			JobParameters jobParameters = stepExecution.getJobParameters();
			params.put("timestamp", jobParameters.getLong("timestamp"));
			log("params=================>>>" + params);
			/* Job Parameter 얻어오는 부분 끝 */

			/* MyBatis streaming 방식 처리 */
			sqlSessionSample.select("net.dstone.batch.sample.SampleTestDao.selectListSampleTest", params,
					new ResultHandler<Map<String, Object>>() {
						@Override
						public void handleResult(
								org.apache.ibatis.session.ResultContext<? extends Map<String, Object>> resultContext) {
							queue.add(resultContext.getResultObject());
						}
					}
			);
			iterator = queue.iterator();
		}
        return iterator.hasNext() ? iterator.next() : null;
    }
    
}
