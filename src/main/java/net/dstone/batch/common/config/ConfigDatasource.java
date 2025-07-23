package net.dstone.batch.common.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

import net.dstone.common.core.BaseObject;

@Component
public class ConfigDatasource extends BaseObject{

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

	@Primary
    @Bean(name = "dataSourceCommon")
    @ConfigurationProperties("spring.datasource.common.hikari")
    public DataSource dataSourceCommon() {
    	if( "Y".equals(configProperty.getProperty("use-jndi-lookup")) ) {
    		return (new JndiDataSourceLookup()).getDataSource(configProperty.getProperty("jndi-lookup-name"));
    	}else {
    		return DataSourceBuilder.create().type(HikariDataSource.class).build();
    	}
    }

    @Bean(name = "dataSourceSample")
    @ConfigurationProperties("spring.datasource.sample.hikari")
    public DataSource dataSourceSample() {
    	if( "Y".equals(configProperty.getProperty("use-jndi-lookup")) ) {
    		return (new JndiDataSourceLookup()).getDataSource(configProperty.getProperty("jndi-lookup-name"));
    	}else {
    		return DataSourceBuilder.create().type(HikariDataSource.class).build();
    	}
    }

}
