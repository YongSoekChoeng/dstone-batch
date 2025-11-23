package net.dstone.batch.common.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

/**
 * ItemReader, ItemProcessor, ItemWriter, Tasklet 등 Step에서 내부적으로 사용되는 Item객체들의 부모 클래스
 */
@Component
@StepScope
public class BaseItem extends BaseBatchObject implements StepExecutionListener {

	protected void log(Object msg) {
    	this.info(msg);
    	//this.debug(msg);
    }

	/**
	 * 파라메터
	 */
	protected Map<String, Object> params = new HashMap<String, Object>();

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
    	this.populateParam(stepExecution);
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
    	// TO-DO : 추후 필요하면 구현
    	return null;
    }
    
    /**
     * 파라메터 수집
     * @param stepExecution
     */
    private void populateParam(StepExecution stepExecution) {
    	// Job Parameter 수집
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
    
    /**
     * 파라메터 값 얻어오는 메소드
     * @param key
     * @return
     */
    public Object getParam(String key) {
    	return params.get(key);
    }

    /**
     * 파라메터 값 얻어오는 메소드
     * @param key
     * @param defaultVal
     * @return
     */
    public Object getParam(String key, String defaultVal) {
    	Object val = getParam(key);
    	if( val == null || "".equals(val.toString()) ) {
    		val = defaultVal;
    	}
    	return val;
    }
}
