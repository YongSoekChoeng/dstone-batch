package net.dstone.batch.common.runner;

import java.util.Iterator;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import net.dstone.common.utils.LogUtil;
import net.dstone.common.utils.StringUtil;

@RestController
@RequestMapping("/batch")
public class RestApiRunner {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRegistry jobRegistry;

	@RequestMapping("/restapi/{jobName}")
    public String runJob(@PathVariable String jobName, @RequestParam Map<String, String> params, HttpServletRequest request) throws Exception {
		String status = "";
		JobExecution execution = null;
		try {
            execution = launchJob(jobName, params, request);
            status = execution.getStatus().name();
		} catch (Exception e) {
			status = BatchStatus.FAILED.name();
			e.printStackTrace();
		}
		return status;
    }
	
    private JobExecution launchJob(String jobName, Map<String, String> params, HttpServletRequest request) throws Exception {
    	LogUtil.sysout( RestApiRunner.class.getName() + ".launchJob("+params+") has been called !!!");
    	
    	HttpSession session = request.getSession();
        JobExecution execution = null;

    	try {

            if( !StringUtil.isEmpty(jobName) ) {

        		// Job파라메터 등록
    			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
    			jobParametersBuilder.addString("timestamp", String.valueOf(System.currentTimeMillis()));
                if( params != null ) {
                	Iterator<String> paramKeys = params.keySet().iterator();
                	while( paramKeys.hasNext() ) {
                		String paramKey = paramKeys.next();
                		Object paramVal = params.get(paramKey);
                		if(paramVal != null) {
                			jobParametersBuilder.addString(paramKey, paramVal.toString());
                		}
                	}
                }
                JobParameters jobParameters = jobParametersBuilder.toJobParameters();

                // Job 등록
                Job job = jobRegistry.getJob(jobName);
                
				// Job 실행
                execution = jobLauncher.run(job, jobParameters);
                
            }
            
		} catch (Throwable e) {
			e.printStackTrace();
		}
    	return execution;
    }
}
