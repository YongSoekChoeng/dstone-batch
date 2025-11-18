package net.dstone.batch.common.runner;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;
import net.dstone.common.utils.StringUtil;

/**
 * Spring Cloud Data Flow 에서 호출하는 경우에 대한 조치.
 */
@Component
public class CloudTaskRunner extends BatchBaseObject implements ApplicationRunner {

    @Value("${spring.batch.job.names}")
    private String jobName;
    
	@Override
	public void run(ApplicationArguments args) throws Exception {
		this.info("jobName=========================["+jobName+"]");
        if( !StringUtil.isEmpty(this.jobName) ) {
        	ArrayList<String> listArgs = new ArrayList<String>();
    		listArgs.add( "spring.batch.job.names=" + this.jobName);
        	String[] strArgs = null;
        	if( args.getSourceArgs() != null ) {
                for (String arg : args.getSourceArgs()) {
                	listArgs.add(arg);
                }
        	}
        	strArgs = new String[listArgs.size()];
        	listArgs.toArray(strArgs);
        	listArgs.clear();
        	listArgs = null;
        	SimpleBatchRunner.launchJob(null, 0, strArgs);
        }
	}

}
