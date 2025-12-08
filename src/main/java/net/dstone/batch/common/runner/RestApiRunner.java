package net.dstone.batch.common.runner;

import java.util.Iterator;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import net.dstone.batch.common.config.ConfigAutoReg;
import net.dstone.common.utils.StringUtil;

@RestController
@RequestMapping("/batch")
public class RestApiRunner {

	@Autowired
	@Qualifier("asyncJobLauncher")
	private JobLauncher asyncJobLauncher;

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	protected JobExplorer jobExplorer;

	@Autowired
	private ConfigAutoReg configAutoReg;

	@RequestMapping("/restapi/{jobName}")
    public ResponseEntity<?> runJob(@PathVariable String jobName, @RequestParam Map<String, String> params, HttpServletRequest request) throws Exception {
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
                configAutoReg.registerJob(jobName);
                // Job 조회
                Job job = jobRegistry.getJob(jobName);
				// Job 실행
                execution = asyncJobLauncher.run(job, jobParameters);
            }else {
            	throw new Exception("jobName["+jobName+"]은 필수값입니다.");
            }
		} catch (Exception e) {
			e.printStackTrace();
	        return ResponseEntity.ok(Map.of(
	        	"jobInstanceId", execution.getJobId(),
	        	"jobExecutionId", execution.getId(),
	        	"status", BatchStatus.FAILED
	        ));
		}
        return ResponseEntity.ok(Map.of(
	        "jobInstanceId", execution.getJobId(),
	        "jobExecutionId", execution.getId(),
             "status", BatchStatus.STARTED
        ));
    }
	
    @GetMapping("/status/{jobExecutionId}")
    public ResponseEntity<?> status(@PathVariable Long jobExecutionId) {
        JobExecution execution = jobExplorer.getJobExecution(jobExecutionId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        if( execution.isRunning() ) {
            return ResponseEntity.ok(Map.of(
                    "status", execution.getStatus(),
                    "startTime", execution.getStartTime()
            ));
        }else {
            return ResponseEntity.ok(Map.of(
                    "status", execution.getStatus(),
                    "exitStatus", execution.getExitStatus(),
                    "startTime", execution.getStartTime(),
                    "endTime", execution.getEndTime()
                    //,"stepExecutions", execution.getStepExecutions()
            ));
        }
    }
}
