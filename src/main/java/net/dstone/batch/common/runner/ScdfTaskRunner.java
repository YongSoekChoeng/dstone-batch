package net.dstone.batch.common.runner;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.config.ConfigProperty;
import net.dstone.batch.common.core.BaseBatchObject;
import net.dstone.common.utils.StringUtil;

/**
 * Spring Cloud Data Flow 에서 호출하는 경우에 대한 조치.
 */
@Component
public class ScdfTaskRunner extends BaseBatchObject implements ApplicationRunner {

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String jobName = configProperty.getProperty("spring.batch.job.names");
        if( !StringUtil.isEmpty(jobName) ) {
        	ArrayList<String> listArgs = new ArrayList<String>();
    		listArgs.add( "spring.batch.job.names=" + jobName);
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
        	SimpleBatchRunner.launchJob(null, 0, false, strArgs);
        }
	}

}
