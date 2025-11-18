package net.dstone.batch.sample.jobs.job003;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class TableUpdateReader extends BatchBaseObject {

    @Autowired 
    @Qualifier("sqlSessionFactorySample")
    protected SqlSessionFactory sqlSessionFactorySample; 
    
    @Bean
    public ItemReader<Map> read(Map<String, Object> params) {
    	this.info(this.getClass().getName() + ".read("+params+") has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        return new MyBatisPagingItemReaderBuilder<Map>()
                .sqlSessionFactory(sqlSessionFactorySample)
                .queryId("net.dstone.batch.sample.SampleTestDao.selectListSampleTest")
                .parameterValues(params)
                .pageSize(100)
                .build();
    }
	
}
