package net.dstone.batch.common.runner;

import java.util.Arrays;

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

public class SimpleBatchRunner extends BatchBaseObject {
	
    public static void main(String[] args) throws Exception {
    	
    	int exitCode = 0;
    	ConfigurableApplicationContext context = null;
    	
    	try {
    		net.dstone.batch.common.DstoneBatchApplication.setSysProperties();

    		context = launchJob(null, exitCode, args);

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
    
    public static ConfigurableApplicationContext launchJob(ConfigurableApplicationContext context, int exitCode, String[] args) throws Exception {
    	LogUtil.sysout( SimpleBatchRunner.class.getName() + ".launchJob("+Arrays.toString(args)+") has been called !!!");
    	
    	exitCode = 0;

        String jobName = "";
        String[] jobParams = new String[0];
        JobExecution execution = null;
        
    	try {
            if (args == null || args.length < 1) {
                throw new Exception("Job name must be provided as the first argument.");
            }
            
            String firstArg = args[0];
            if( firstArg.indexOf("spring.batch.job.name=") > -1 ||  firstArg.indexOf("spring.batch.job.names=") > -1 ) {
            	String[] words = StringUtil.toStrArray(firstArg, "=");
            	if(words.length > 1) {
            		jobName = words[1];
            	}
            }
            if( !StringUtil.isEmpty(jobName) ) {
                if(args.length > 1) {
                	jobParams = new String[args.length-1];
                	for(int i=1; i<args.length; i++) {
                		jobParams[i-1] = args[i];
                	}
                }

        		if(StringUtil.isEmpty(jobName)) {
        			throw new Exception("Job name must be provided as the first argument.");
        		}

        		if(context == null) {
                    context = new SpringApplicationBuilder(DstoneBatchApplication.class)
                            .web(WebApplicationType.NONE)
                            .run(jobParams);
        		}

                JobLauncher jobLauncher = context.getBean(JobLauncher.class);
                JobRegistry jobRegistry = context.getBean(JobRegistry.class);
                Job job = jobRegistry.getJob(jobName);

                JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
                jobParametersBuilder.addLong("timestamp", System.currentTimeMillis());
                if (jobParams.length > 0) {
                    for (int i = 0; i < jobParams.length; i++) {
                        String arg = jobParams[i];
                        if (arg.indexOf("=") == -1) {
                            throw new Exception("Parameters must be in KEY=VALUE format.");
                        }
                        String[] batchParams = StringUtil.toStrArray(arg, "=", true);
                        if (batchParams.length == 0) {
                            throw new Exception("Parameters must be in KEY=VALUE format.");
                        }
                        String key = batchParams[0];
                        String val = "";
                        if (batchParams.length > 1) {
                            val = batchParams[1];
                        }
                        jobParametersBuilder.addString(key, val);
                    }
                }
                JobParameters jobParameters = jobParametersBuilder.toJobParameters();
                execution = jobLauncher.run(job, jobParameters);
                if (execution.getStatus().isUnsuccessful()) {
                	exitCode = -1;
                }
            }
            
		} catch (Throwable e) {
			exitCode = -1;
			e.printStackTrace();
		}

    	return context;
    }
}
