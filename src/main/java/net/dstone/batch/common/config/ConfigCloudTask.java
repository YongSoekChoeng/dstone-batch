package net.dstone.batch.common.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.task.configuration.DefaultTaskConfigurer;
import org.springframework.cloud.task.configuration.TaskConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.common.core.BaseObject;

@Component
public class ConfigCloudTask extends BaseObject{

    /**
     * Spring Cloud Task를 사용할 경우 Task용 DataSource를 명시적으로 지정해 주어야 함.
     * @param dataSource
     * @return
     */
    @Bean
    public TaskConfigurer taskConfigurer(@Qualifier("dataSourceCommon") DataSource dataSource) {
        return new DefaultTaskConfigurer(dataSource);
    }
}
