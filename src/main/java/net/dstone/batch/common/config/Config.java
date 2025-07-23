package net.dstone.batch.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import net.dstone.common.core.BaseObject;

@EnableAsync
@Configuration
@Import({ 
	ConfigBatch.class,
	ConfigDatasource.class,
	ConfigEnc.class,
	ConfigMapper.class,
	ConfigProperty.class,
	ConfigTransaction.class
})
public class Config extends BaseObject{
	
}
