package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.batch.common.items.AbstractItemProcessor;
import net.dstone.batch.common.items.TableItemReader;
import net.dstone.batch.common.items.TableItemWriter;

@Component
@AutoRegJob(name = "tableUpdateJob")
public class TableUpdateJobConfig extends BaseJobConfig {

    private void log(Object msg) {
    	this.info(msg);
    }
    
	@Override
	public void configJob() throws Exception {
		log(this.getClass().getName() + ".configJob() has been called !!!");
		int chunkSize = 5000;
		//this.addStep(this.workerStep1("01.Reader/Processor/Writer 별도클래스로 생성 스텝", chunkSize));
		this.addStep(this.workerStep2("02.Reader/Processor/Writer 동일클래스내에 생성 스텝", chunkSize));
	}
	
    /**************************************** 01.Reader/Processor/Writer 별도클래스로 생성 ****************************************/
	private Step workerStep1(String stepName, int chunkSize) {
		log(this.getClass().getName() + ".workerStep1("+stepName+", "+chunkSize+" ) has been called !!!");
		return new StepBuilder(stepName, jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerCommon)
				.reader( itemReader() )
				.processor((ItemProcessor<? super Map, ? extends Map>) itemProcessor())
				.writer((ItemWriter<? super Map>) itemWriter())
				.build();
	}

    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemReader() {
        return new TableItemReader(this.sqlSessionFactorySample, "net.dstone.batch.sample.SampleTestDao.selectListSampleTest");
    }

    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
    	return new AbstractItemProcessor() {
			@Override
			public Map<String, Object> process(Map item) throws Exception {
				this.log(this.getClass().getName() + ".process("+item+") has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );

				// Thread-safe하게 새로운 Map 객체 생성
		        Map<String, Object> processedItem = new HashMap<>(item);
				// 예: TEST_NAME, FLAG_YN 값을 변경 
		        processedItem.put("TEST_NAME", item.get("TEST_ID")+"-이름");
				processedItem.put("FLAG_YN", "Y");

		    	return processedItem;
			}
    	};
    }

    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> itemWriter() {
        return new TableItemWriter(this.sqlBatchSessionSample, "net.dstone.batch.sample.SampleTestDao.updateSampleTest");
    }
    /*************************************************************************************************************************/
    
    /*************************************** 02.Reader/Processor/Writer 동일클래스내에 생성 ***************************************/
    private Step workerStep2(String stepName, int chunkSize) {
        return new StepBuilder(stepName, jobRepository)
                .<Map<String, Object>, Map<String, Object>>chunk(chunkSize, txManagerCommon)
                .reader(tableUpdateReader(chunkSize))
                .processor(tableUpdateProcessor())
                .writer(tableUpdateWriter())
                .build();
    }

    @Bean
    @StepScope
    public MyBatisPagingItemReader<Map<String, Object>> tableUpdateReader(int chunkSize) {
        Map<String, Object> params = new HashMap<>();
        // 필요시 파라미터 추가 가능
        return new MyBatisPagingItemReaderBuilder<Map<String, Object>>()
                .sqlSessionFactory(this.sqlBatchSessionSample.getSqlSessionFactory())
                .queryId("net.dstone.batch.sample.SampleTestDao.selectListSampleTestPaging")
                .pageSize(chunkSize)
                .build(); 
    }

    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> tableUpdateProcessor() {
        return item -> {
            // 데이터 수정 로직
            String testId = (String) item.get("TEST_ID");
            String testName = (String) item.get("TEST_NAME");
            
            // FLAG_YN을 'Y'로 변경
            item.put("FLAG_YN", "Y");
            
            // 수정된 데이터 반환
            return item;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> tableUpdateWriter() {
        return new MyBatisBatchItemWriterBuilder<Map<String, Object>>()
                .sqlSessionFactory(this.sqlBatchSessionSample.getSqlSessionFactory())
                .statementId("net.dstone.batch.sample.SampleTestDao.updateSampleTest")
                .build();
    }
    /*************************************************************************************************************************/

}
