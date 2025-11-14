package net.dstone.batch.common.runner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch")
public class RestRunner {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    
	@Autowired
    public RestRunner(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

	@RequestMapping("/restapi/{jobName}")
    public String runJob(@PathVariable String jobName, @RequestParam Map<String, String> params) throws Exception {
		String status = "";
		try {

            // JobParameters 생성
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            if( params != null ) {
            	Iterator<String> paramKeys = params.keySet().iterator();
            	while( paramKeys.hasNext() ) {
            		String paramKey = paramKeys.next();
            		Object paramVal = params.get(paramKey);
            		if(paramVal != null) {
            			jobParametersBuilder.addJobParameter(paramKey, new JobParameter(paramVal, paramVal.getClass()));
            		}
            	}
            }
            jobParametersBuilder.addJobParameter("timestamp", new JobParameter(System.currentTimeMillis(), Long.class));
			
            // Job 가져오기
            Job job = jobRegistry.getJob(jobName);

            // Job 실행
            JobExecution execution = jobLauncher.run(job, jobParametersBuilder.toJobParameters());
            
            status = execution.getStatus().name();
            
		} catch (Exception e) {
			status = BatchStatus.FAILED.name();
			e.printStackTrace();
		}
		return status;
    }
}
