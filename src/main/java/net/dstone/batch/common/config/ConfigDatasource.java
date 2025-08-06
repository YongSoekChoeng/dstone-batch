package net.dstone.batch.common.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import com.zaxxer.hikari.HikariDataSource;

import net.dstone.batch.common.core.BatchBaseObject;


@Configuration
public class ConfigDatasource extends BatchBaseObject{

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

    @Bean(name = "dataSourceCommon")
    @ConfigurationProperties("spring.datasource.common.hikari")
    public DataSource dataSourceCommon() {
    	if( "Y".equals(configProperty.getProperty("use-jndi-lookup")) ) {
    		return (new JndiDataSourceLookup()).getDataSource(configProperty.getProperty("jndi-lookup-name"));
    	}else {
    		return DataSourceBuilder.create().type(HikariDataSource.class).build();
    	}
    }
    
}
