package net.dstone.batch.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
@Import({ 
	ConfigAutoReg.class,
	ConfigBatch.class,
	ConfigDatasource.class,
	ConfigEnc.class,
	ConfigMapper.class,
	ConfigProperty.class,
	ConfigTransaction.class
})
public class Config{
	
}
