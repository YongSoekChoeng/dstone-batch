package net.dstone.batch.common.runner;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import net.dstone.batch.common.DstoneBatchApplication;
import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.config.ConfigProperty;
import net.dstone.batch.common.core.BaseBatchObject;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.common.utils.StringUtil;

/**
 * Spring Cloud Data Flow 에서 호출하는 경우에 대한 조치.
 */
@Component
public class ScdfTaskRunner extends BaseBatchObject implements ApplicationRunner {

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

	/**
	 * SCDF에서 호출하는 Job요청을 처리하는 메소드
	 */
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
        	SimpleBatchRunner.launch(null, 0, strArgs);
        }
	}

}
