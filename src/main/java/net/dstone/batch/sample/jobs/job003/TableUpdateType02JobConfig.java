package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * <pre>
 * 테이블 SAMPLE_TEST 의 데이터를 수정하는 Job.
 * 전체데이터를 읽어와서 SAMPLE_TEST.FLAG_YN 를 'N' => 'Y'로 수정.
 * 
 * CREATE TABLE SAMPLE_TEST (
 *   TEST_ID VARCHAR(30) NOT NULL, 
 *   TEST_NAME VARCHAR(200), 
 *   FLAG_YN VARCHAR(1), 
 *   INPUT_DT DATE NOT NULL,  
 *   PRIMARY KEY  (TEST_ID)
 * )
 * 
 * 병렬쓰레드처리. 
 * Reader/Processor/Writer 별도클래스로 구현.
 * </pre>
 */
@Component
@AutoRegJob(name = "tableUpdateType02Job")
public class TableUpdateType02JobConfig extends BaseJobConfig {

    /**************************************** 00. Job Parameter 선언 시작 ****************************************/
	// spring.batch.job.names : @AutoRegJob 어노테이션에 등록된 name
	// gridSize : 병렬처리할 쓰레드 갯수
	private int gridSize = 2;	// 쓰레드 갯수
    /**************************************** 00. Job Parameter 선언 끝 ******************************************/
	
	/**
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		
        int chunkSize = 30;
        gridSize = Integer.parseInt(StringUtil.nullCheck(this.getInitJobParam("gridSize"), "1")); // 파티션 개수 (병렬 처리할 스레드 수)
        
        /*******************************************************************
        테이블 SAMPLE_TEST에 데이터를 수정(병렬쓰레드처리). Reader/Processor/Writer 별도클래스로 구현.
        실행파라메터 : spring.batch.job.names=tableUpdateType02Job gridSize=3
        *******************************************************************/
		this.addStep(this.parallelMasterStep(chunkSize, gridSize));
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
	
	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
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
	public Step parallelSlaveStep(int chunkSize) {
		callLog(this, "parallelSlaveStep");
		return new StepBuilder("parallelSlaveStep", jobRepository)
				.<Map<String, Object>, Map<String, Object>>chunk(chunkSize, txManagerCommon)
				.reader(itemPartitionReader()) // Spring이 런타임에 주입
				.processor( itemProcessor())
				.writer( itemWriter())
				.build();
	}
	/* --------------------------------- Step 설정 끝 ---------------------------------- */ 

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

	/* --------------------------------- Reader 설정 시작 ------------------------------- */ 
    
    /**
     * Table 읽어오는 ItemReader. Partitioner 와 함께 사용.
     * @param minId
     * @param maxId
     * @return
     */
    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemPartitionReader() {
    	//callLog(this, "itemPartitionReader");
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
    	//callLog(this, "itemProcessor");
    	return new AbstractItemProcessor() {
			@Override
			public Object process(Object item) throws Exception {
				callLog(this, "process", item);

				// Thread-safe하게 새로운 Map 객체 생성
		        Map<String, Object> processedItem = (HashMap<String, Object>)item;
				// 예: TEST_NAME, FLAG_YN 값을 변경 
		        processedItem.put("TEST_NAME", processedItem.get("TEST_ID")+"-이름");
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
    
}
