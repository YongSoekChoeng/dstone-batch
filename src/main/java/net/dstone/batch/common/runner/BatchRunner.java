package net.dstone.batch.common.runner;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import net.dstone.batch.common.DstoneBatchApplication;
import net.dstone.batch.common.core.BatchBaseObject;
import net.dstone.common.utils.LogUtil;
import net.dstone.common.utils.StringUtil;

public class BatchRunner extends BatchBaseObject {
	
    public static void main(String[] args) throws Exception {
    	
    	int exitCode = 0;
    	ConfigurableApplicationContext context = null;
    	
    	try {
            if (args == null || args.length < 1) {
                throw new Exception("Job name must be provided as the first argument.");
            }
            String jobName = args[0];

            context = new SpringApplicationBuilder(DstoneBatchApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(args);

            JobLauncher jobLauncher = context.getBean(JobLauncher.class);
            JobRegistry jobRegistry = context.getBean(JobRegistry.class);
            Job job = jobRegistry.getJob(jobName);

            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addLong("timestamp", System.currentTimeMillis());
            if (args.length > 1) {
                for (int i = 1; i < args.length; i++) {
                    String arg = args[i];
                    if (arg.indexOf("=") == -1) {
                        throw new Exception("Parameters must be in KEY=VALUE format.");
                    }
                    String[] params = StringUtil.toStrArray(arg, "=", true);
                    if (params.length == 0) {
                        throw new Exception("Parameters must be in KEY=VALUE format.");
                    }
                    String key = params[0];
                    String val = "";
                    if (params.length > 1) {
                        val = params[1];
                    }
                    jobParametersBuilder.addString(key, val);
                }
            }
            JobParameters jobParameters = jobParametersBuilder.toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            LogUtil.sysout("Job Status: " + execution.getStatus());
            if (execution.getStatus().isUnsuccessful()) {
            	exitCode = -1;
            }
            
		} catch (Exception e) {
			exitCode = -1;
			e.printStackTrace();
		} finally {
			if (context != null) {
				context.close();
			}
			System.exit(exitCode);
		}
    }
}
