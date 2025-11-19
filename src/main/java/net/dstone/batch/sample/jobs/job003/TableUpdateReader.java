package net.dstone.batch.sample.jobs.job003;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.ibatis.session.ResultHandler;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class TableUpdateReader extends BatchBaseObject implements ItemReader<Map<String, Object>> {

    private void log(Object msg) {
    	this.debug(msg);
    	//System.out.println(msg);
    }

    private final SqlSessionTemplate sqlBatchSessionSample;
    private Iterator<Map<String, Object>> iterator;
    private final ConcurrentLinkedQueue<Map<String, Object>> queue = new ConcurrentLinkedQueue<>();
    
    public TableUpdateReader(SqlSessionTemplate sqlBatchSessionSample) {
    	this.sqlBatchSessionSample = sqlBatchSessionSample;
    }

    @Override
    public Map<String, Object> read() {
    	log(this.getClass().getName() + ".read() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        if (iterator == null) {
            // MyBatis streaming 방식 처리
            sqlBatchSessionSample.select("net.dstone.batch.sample.SampleTestDao.selectListSampleTest", 
                new ResultHandler<Map<String, Object>>() {
                    @Override
                    public void handleResult(org.apache.ibatis.session.ResultContext<? extends Map<String, Object>> resultContext) {
                        queue.add(resultContext.getResultObject());
                    }
                });
            iterator = queue.iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }
    
}
