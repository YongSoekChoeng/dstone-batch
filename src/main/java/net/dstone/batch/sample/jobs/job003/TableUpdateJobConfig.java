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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.batch.common.items.AbstractItemProcessor;
import net.dstone.batch.common.items.TableItemReader;
import net.dstone.batch.common.items.TableItemWriter;
import net.dstone.batch.common.partitioner.QueryPartitioner;

@Component
@AutoRegJob(name = "tableUpdateJob")
public class TableUpdateJobConfig extends BaseJobConfig {

    private void log(Object msg) {
    	this.info(msg);
    }
    
	@Override
	public void configJob() throws Exception {
		log(this.getClass().getName() + ".configJob() has been called !!!");
		
        int chunkSize = 30;
        int gridSize = 4; // 파티션 개수 (병렬 처리할 스레드 수)
        
		//this.addStep(this.workerStep1(chunkSize));
		this.addStep(this.workerMultiStep1(chunkSize, gridSize));
		
		//this.addStep(this.workerStep2("02.Reader/Processor/Writer 동일클래스내에 생성 스텝", chunkSize));
	}
	
	@Bean
	@StepScope
	public TaskExecutor stepExecutor(@Qualifier("taskExecutor") TaskExecutor executor) {
	    return executor;
	}
	
    /**************************************** 01.Reader/Processor/Writer 별도클래스로 생성 ****************************************/
	private Step workerStep1(int chunkSize) {
		log(this.getClass().getName() + ".workerStep1("+chunkSize+" ) has been called !!!");
		return new StepBuilder("workerStep1", jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerSample)
				.reader( itemReader() )
				.processor((ItemProcessor<? super Map, ? extends Map>) itemProcessor())
				.writer((ItemWriter<? super Map>) itemWriter())
				.build();
	}

    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemReader() {
		log(this.getClass().getName() + ".itemReader() has been called !!!");
    	Map<String, Object> baseParams = new HashMap<String, Object>();
        return new TableItemReader(this.sqlSessionFactorySample, "net.dstone.batch.sample.SampleTestDao.selectListSampleTestAll", baseParams);
    }

    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemPartitionReader(
    	@Value("#{stepExecutionContext['MIN_ID']}") String minId
    	, @Value("#{stepExecutionContext['MAX_ID']}") String maxId
    ) {
		log(this.getClass().getName() + ".itemPartitionReader("+minId+", "+maxId+") has been called !!!");
    	Map<String, Object> baseParams = new HashMap<String, Object>();
    	baseParams.put("MIN_ID", minId);
    	baseParams.put("MAX_ID", maxId);
        return new TableItemReader(this.sqlSessionFactorySample, "net.dstone.batch.sample.SampleTestDao.selectListSampleTestBetween", baseParams);
    }

    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
		log(this.getClass().getName() + ".itemProcessor() has been called !!!");
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
		log(this.getClass().getName() + ".itemWriter() has been called !!!");
        return new TableItemWriter(this.sqlBatchSessionSample, "net.dstone.batch.sample.SampleTestDao.updateSampleTest");
    }
    
    @Bean
    @Qualifier("queryPartitioner")
    public QueryPartitioner queryPartitioner() {
        int gridSize = 4; // 파티션 개수
        QueryPartitioner queryPartitioner = new QueryPartitioner(
            sqlBatchSessionSample, 
            "net.dstone.batch.sample.SampleTestDao.selectListSampleTestAll", 
            "TEST_ID", 
            gridSize
        );
        return queryPartitioner;
    }
    
	private Step workerMultiStep1(int chunkSize, int gridSize) {
		log(this.getClass().getName() + ".workerMultiStep1("+chunkSize+", "+gridSize+" ) has been called !!!");
        
		return new StepBuilder("workerMultiStep1", jobRepository)
				.partitioner("slaveStep1", queryPartitioner())
				.gridSize(gridSize)
				.step(slaveStep1())
				.taskExecutor(stepExecutor(null))
				.build();
	}
	
	@Bean
	public Step slaveStep1() {
		log(this.getClass().getName() + ".slaveStep1() has been called !!!");
		int chunkSize = 30;
		return new StepBuilder("slaveStep1", jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerSample)
				.reader(itemPartitionReader(null, null)) // Spring이 런타임에 주입
				.processor((ItemProcessor<? super Map, ? extends Map>) itemProcessor())
				.writer((ItemWriter<? super Map>) itemWriter())
				.build();
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
