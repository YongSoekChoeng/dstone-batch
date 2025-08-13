package net.dstone.batch.common.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

import net.dstone.batch.common.core.BatchBaseObject;


@Configuration
public class ConfigDatasource extends BatchBaseObject{

	@Autowired 
	ConfigProperty configProperty; // 프로퍼티 가져오는 bean

	/********************************************************************************
	1. DataSource 관련 설정
	********************************************************************************/
    @Bean(name = "dataSourceCommon")
    @ConfigurationProperties("spring.datasource.common.hikari")
    public DataSource dataSourceCommon() {
    	return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

	/********************************************************************************
	2. TransactionManager 관련 설정
	********************************************************************************/
	@Bean(name = "txManagerCommon")
	public PlatformTransactionManager txManagerCommon(@Qualifier("dataSourceCommon") DataSource dataSourceCommon) {
		PlatformTransactionManager txManagerCommon = new DataSourceTransactionManager(dataSourceCommon);
		return txManagerCommon;
	}

}
