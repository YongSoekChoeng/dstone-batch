package net.dstone.batch.common.config;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.jmx.ParentAwareNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class ConfigAspect extends BatchBaseObject{
	
    @Bean
    public Advisor jobExecuteAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* org.springframework.batch.core.launch.**.*Launcher.run(..))");

        return new DefaultPointcutAdvisor(pointcut, new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
            	StringBuffer buff = new StringBuffer();
            	
            	buff.setLength(0);
            	buff.append("\n");
            	buff.append("\n");
            	buff.append("||======================================= Job[singleTaskletJob] Start =======================================||");
            	info(buff.toString());
            	
                Object result = invocation.proceed();

            	buff.setLength(0);
            	buff.append("\n");
            	buff.append("||======================================= Job[singleTaskletJob] End =======================================||");
            	buff.append("\n");
            	info(buff.toString());
            	
                return result;
            }
        });
    }
}
