package net.dstone.batch.common.config;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class ConfigAspect extends BatchBaseObject{

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean
	
    @Bean
    public Advisor jobExecuteAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* org.springframework.batch.core.launch.**.*Launcher.run(..))");

        return new DefaultPointcutAdvisor(pointcut, new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
            	StringBuffer buff = new StringBuffer();
            	org.springframework.batch.core.Job job = null;
            	Object result = null;
            	String jobName = "";
            	String jobStatus = "";
            	Object[] args = invocation.getArguments();
            	if( args != null ) {
            		for( Object arg : args) {
            			if( arg instanceof org.springframework.batch.core.Job ) {
            				job = (org.springframework.batch.core.Job)arg;
            				jobName =  job.getName();
            				break;
            			}
            		}
            	}
            	
            	if( job != null ) {
            		jobStatus = BatchStatus.STARTING.name();
                	buff.setLength(0);
                	buff.append("\n");
                	buff.append("\n");
                	buff.append("||======================================= Job["+jobName+"] "+ jobStatus +" =======================================||");
                	info(buff.toString());
                	
                	JobExecution execution = null;
                	try {
                        result = invocation.proceed();

                        String strBatchTimeout = configProperty.getProperty("app.batch-timeout");
                        long batchTimeout = Integer.parseInt( (strBatchTimeout == null||"".equals(strBatchTimeout)) ?"3600":strBatchTimeout );

                        jobStatus = BatchStatus.STARTED.name();
                    	buff.setLength(0);
                        if(result instanceof JobExecution ) {
                        	execution = (JobExecution)result;
                			if ( execution != null ) {
                				int checkTryCnt = 0;
                				while (execution.isRunning()) {
                				    Thread.sleep(1 * 1000);
                				    checkTryCnt++;
                				    if( checkTryCnt > batchTimeout ) {
                				    	break;
                				    }
                				}
                			}
                        }
					} catch (Exception e) {
						throw e;
					} finally {
						if( execution != null) {
							jobStatus = execution.getStatus().name();
						}
	                	buff.append("\n");
	                	buff.append("||======================================= Job["+jobName+"] "+ jobStatus +" =======================================||");
	                	buff.append("\n");
	                	info(buff.toString());
					}
            	}
                return result;
            }
        });
    }
}
