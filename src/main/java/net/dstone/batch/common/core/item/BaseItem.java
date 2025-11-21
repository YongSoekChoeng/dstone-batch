package net.dstone.batch.common.core.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
@StepScope
public class BaseItem extends BatchBaseObject {

	protected void log(Object msg) {
    	this.info(msg);
    	//this.debug(msg);
    }

	protected Map<String, Object> params = new HashMap<String, Object>();

    @BeforeStep
    protected void beforeStep(StepExecution stepExecution) {
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

    @AfterStep
    protected void afterStep(StepExecution stepExecution) {
    	// TO-DO : 추후 필요하면 구현
    }
    
    public Object getParam(String key) {
    	return params.get(key);
    }

    public Object getParam(String key, String defaultVal) {
    	Object val = getParam(key);
    	if( val == null || "".equals(val.toString()) ) {
    		val = defaultVal;
    	}
    	return val;
    }
}
