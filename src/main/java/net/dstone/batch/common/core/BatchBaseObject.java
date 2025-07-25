package net.dstone.batch.common.core;

import java.lang.annotation.Annotation;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import net.dstone.common.core.BaseObject;

@Component
public class BatchBaseObject extends BaseObject {
	
	@Autowired
	protected ApplicationContext applicationContext;
	@Autowired
	protected JobBuilderFactory jobBuilderFactory;
	@Autowired
	protected StepBuilderFactory stepBuilderFactory;
	@Autowired
	protected JobRegistry jobRegistry;
	
	protected String getAnnotationVal(String annotationKey) {
		String annotationVal = "";
		
		Annotation[] annotations = this.getClass().getAnnotations();
		if( annotations != null ) {
			for( Annotation annotation : annotations ) {
				
				this.info("annotation====>>>" + annotation);
			}
		}
		
		return annotationVal;
	}
}
