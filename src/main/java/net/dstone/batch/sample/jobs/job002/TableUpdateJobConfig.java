package net.dstone.batch.sample.jobs.job002;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
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
import net.dstone.common.utils.StringUtil;

/**
 * 테이블 SAMPLE_TEST 에 테스트데이터를 수정하는 Job.<br>
 * 
 */
@Component
@AutoRegJob(name = "tableUpdateJob")
public class TableUpdateJobConfig extends BaseJobConfig {

    /**************************************** 00. Job Parameter 선언 시작 ****************************************/
	private int gridSize = 2;
    /**************************************** 00. Job Parameter 선언 끝 ******************************************/
	
	/**
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		
        int chunkSize = 30;
        gridSize = Integer.parseInt(StringUtil.nullCheck(this.getInitJobParam("gridSize"), "2")); // 파티션 개수 (병렬 처리할 스레드 수)
        
        /*** Reader/Processor/Writer 별도클래스 용 ***/
        // 단일처리 Step
		//this.addStep(this.singleStep(chunkSize));
        // 병렬처리 Step
		this.addStep(this.parallelMasterStep(chunkSize, gridSize));

        /*** Reader/Processor/Writer 동일클래스 용 ***/
        // 단일처리 Step
		//this.addStep(this.singleInAllStep(chunkSize));
	}
	
	/**
	 * Step 스코프에 해당하는 TaskExecutor
	 * @param executor
	 * @return
	 */
	@Bean
	@StepScope
	public TaskExecutor executor(@Qualifier("taskExecutor") TaskExecutor executor) {
	    return executor;
	}
	
    /**************************************** 01.Reader/Processor/Writer 별도클래스로 생성 ****************************************/

	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
	/**
	 * 단일처리 Step
	 * @param chunkSize
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Step singleStep(int chunkSize) {
		callLog(this, "singleStep", chunkSize);
		return new StepBuilder("singleStep", jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerCommon)
				.reader( itemReader() )
				.processor((ItemProcessor<? super Map, ? extends Map>) itemProcessor())
				.writer((ItemWriter<? super Map>) itemWriter())
				.build();
	}
	/**
	 * 병렬처리 Master Step
	 * @param chunkSize
	 * @param gridSize
	 * @return
	 */
	private Step parallelMasterStep(int chunkSize, int gridSize) {
		callLog(this, "parallelMasterStep", ""+chunkSize+", "+gridSize+"");
		return new StepBuilder("parallelMasterStep", jobRepository)
				.partitioner("parallelSlaveStep", queryPartitioner(gridSize))
				.step(parallelSlaveStep(chunkSize))
				.gridSize(gridSize)
				.taskExecutor(executor(null))
				.build();
	}
	
	/**
	 * 병렬처리 Slave Step
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Step parallelSlaveStep(int chunkSize) {
		callLog(this, "parallelSlaveStep");
		return new StepBuilder("parallelSlaveStep", jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerCommon)
				.reader(itemPartitionReader()) // Spring이 런타임에 주입
				.processor((ItemProcessor<? super Map, ? extends Map>) itemProcessor())
				.writer((ItemWriter<? super Map>) itemWriter())
				.build();
	}
	/* --------------------------------- Step 설정 끝 ---------------------------------- */ 

	/* --------------------------------- Reader 설정 시작 ------------------------------- */ 
    /**
     * Table 읽어오는 ItemReader
     * @return
     */
    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemReader() {
    	callLog(this, "itemReader");
    	Map<String, Object> baseParams = new HashMap<String, Object>();
        return new TableItemReader(this.sqlSessionFactorySample, "net.dstone.batch.sample.SampleTestDao.selectListSampleTestAll", baseParams);
    }

    /**
     * Table 읽어오는 ItemReader. Partitioner 와 함께 사용.
     * @param minId
     * @param maxId
     * @return
     */
    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemPartitionReader() {
    	callLog(this, "itemPartitionReader");
    	Map<String, Object> baseParams = new HashMap<String, Object>();
        return new TableItemReader(this.sqlSessionFactorySample, "net.dstone.batch.sample.SampleTestDao.selectListSampleTestBetween", baseParams);
    }
	/* --------------------------------- Reader 설정 끝 -------------------------------- */ 

	/* --------------------------------- Processor 설정 시작 ---------------------------- */ 
    /**
     * Table 처리용 ItemProcessor
     * @return
     */
    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
    	callLog(this, "itemProcessor");
    	return new AbstractItemProcessor() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Map<String, Object> process(Map item) throws Exception {
				callLog(this, "process", item);

				// Thread-safe하게 새로운 Map 객체 생성
		        Map<String, Object> processedItem = new HashMap<>(item);
				// 예: TEST_NAME, FLAG_YN 값을 변경 
		        processedItem.put("TEST_NAME", item.get("TEST_ID")+"-이름");
				processedItem.put("FLAG_YN", "Y");

		    	return processedItem;
			}
    	};
    }
	/* --------------------------------- Processor 설정 끝 ---------------------------- */ 

	/* --------------------------------- Writer 설정 시작 ------------------------------ */
    /**
     * Table 처리용 ItemWriter
     * @return
     */
    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> itemWriter() {
    	callLog(this, "itemWriter");
        return new TableItemWriter(this.sqlBatchSessionSample, "net.dstone.batch.sample.SampleTestDao.updateSampleTest");
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */
    
	/* --------------------------------- Partitioner 설정 시작 -------------------------- */
    /**
     * Table 처리용 Partitioner
     * @return
     */
    @Bean
    @Qualifier("queryPartitioner")
    @StepScope
    public QueryPartitioner queryPartitioner(int gridSize) {
    	callLog(this, "queryPartitioner", gridSize);
        QueryPartitioner queryPartitioner = new QueryPartitioner(
            sqlBatchSessionSample, 
            "net.dstone.batch.sample.SampleTestDao.selectListSampleTestAll", 
            "TEST_ID", 
            gridSize
        );
        return queryPartitioner;
    }
	/* --------------------------------- Partitioner 설정 끝 --------------------------- */

    /*************************************************************************************************************************/
    
    /*************************************** 02.Reader/Processor/Writer 동일클래스내에 생성 ***************************************/

	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
    /**
     * 단일처리 Step
     * @param chunkSize
     * @return
     */
    private Step singleInAllStep(int chunkSize) {
    	callLog(this, "singleInAllStep", chunkSize);
        return new StepBuilder("singleInAllStep", jobRepository)
                .<Map<String, Object>, Map<String, Object>>chunk(chunkSize, txManagerCommon)
                .reader(tableUpdateReader(chunkSize))
                .processor(tableUpdateProcessor())
                .writer(tableUpdateWriter())
                .build();
    }
	/* --------------------------------- Step 설정 끝 ----------------------------------- */ 

	/* --------------------------------- Reader 설정 시작 -------------------------------- */ 
    /**
     * Table 읽어오는 ItemReader<br>
     * 프레임웍 공통 컴퍼넌트인 net.dstone.batch.common.items.TableItemReader 를 사용하지 않고 
     * org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder 를 사용하도록 샘플링.
     * @param chunkSize
     * @return
     */
    @Bean
    @StepScope
    public MyBatisPagingItemReader<Map<String, Object>> tableUpdateReader(int chunkSize) {
    	callLog(this, "tableUpdateReader", chunkSize);
    	Map<String, Object> params = new HashMap<>();
        // 필요시 파라미터 추가 가능
        return new MyBatisPagingItemReaderBuilder<Map<String, Object>>()
                .sqlSessionFactory(this.sqlBatchSessionSample.getSqlSessionFactory())
                .queryId("net.dstone.batch.sample.SampleTestDao.selectListSampleTestPaging")
                .parameterValues(params)
                .pageSize(chunkSize)
                .build(); 
    }
	/* --------------------------------- Reader 설정 끝 --------------------------------- */ 

	/* --------------------------------- Processor 설정 시작 ---------------------------- */ 
    /**
     * Table 처리용 ItemProcessor
     * @return
     */
    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> tableUpdateProcessor() {
    	callLog(this, "tableUpdateProcessor");
        return item -> {
			// Thread-safe하게 새로운 Map 객체 생성
	        Map<String, Object> processedItem = new HashMap<>(item);
			// 예: TEST_NAME, FLAG_YN 값을 변경 
	        processedItem.put("TEST_NAME", item.get("TEST_ID")+"-이름");
			processedItem.put("FLAG_YN", "Y");
            // 수정된 데이터 반환
            return processedItem;
        };
    }
	/* --------------------------------- Processor 설정 끝 ----------------------------- */ 

	/* --------------------------------- Writer 설정 시작 ------------------------------- */
    /**
     * Table 처리용 ItemWriter
     * @return
     */
    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> tableUpdateWriter() {
    	callLog(this, "tableUpdateWriter");
        return new MyBatisBatchItemWriterBuilder<Map<String, Object>>()
                .sqlSessionFactory(this.sqlBatchSessionSample.getSqlSessionFactory())
                .statementId("net.dstone.batch.sample.SampleTestDao.updateSampleTest")
                .build();
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */
    
    /*************************************************************************************************************************/

}
