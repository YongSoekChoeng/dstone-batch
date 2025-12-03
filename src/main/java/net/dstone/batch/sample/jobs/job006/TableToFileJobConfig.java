package net.dstone.batch.sample.jobs.job006;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.batch.common.items.AbstractItemProcessor;
import net.dstone.batch.common.items.FileItemWriter;
import net.dstone.batch.common.items.TableItemReader;
import net.dstone.batch.common.partitioner.QueryToFilePartitioner;
import net.dstone.common.utils.StringUtil;

/**
 * <pre>
 * 테이블 SAMPLE_TEST 의 데이터를 파일로 저장하는 Job.
 * 
 * CREATE TABLE SAMPLE_TEST (
 *   TEST_ID VARCHAR(30) NOT NULL, 
 *   TEST_NAME VARCHAR(200), 
 *   FLAG_YN VARCHAR(1), 
 *   INPUT_DT DATE NOT NULL,  
 *   PRIMARY KEY  (TEST_ID)
 * )
 * 
 * 병렬쓰레드처리
 * </pre>
 */
@Component
@AutoRegJob(name = "tableToFileJob")
public class TableToFileJobConfig extends BaseJobConfig {

    /**************************************** 00. Job Parameter 선언 시작 ****************************************/
	// spring.batch.job.names : @AutoRegJob 어노테이션에 등록된 name
	// gridSize : 병렬처리할 쓰레드 갯수
	// chunkSize : 트랜젝션묶음 크기
	// outputFileFullPath : 복사생성될 Full파일 경로. 복수개의 파일이 생성되어야 할 경우 outputFileFullPath의 디렉토리 + outputFileFullPath 의 파일명을 참고하여 자동으로 파일생성. 
	// charset : 생성할 파일의 캐릭터셋
	// append  : 작업수행시 파일 초기화여부. true-초기화 하지않고 이어서 생성. false-초기화 후 새로 생성.
	private int gridSize = 0;		// 쓰레드 갯수
	private int chunkSize = 0;		// 청크 사이즈
	String outputFileFullPath = "";
    String charset = "";			// 파일 인코딩
    boolean append = false;			// 기존파일이 존재 할 경우 기존데이터에 추가할지 여부
    /**************************************** 00. Job Parameter 선언 끝 ******************************************/

    LinkedHashMap<String,Integer> colInfoMap = new LinkedHashMap<String,Integer>();
    {
	    colInfoMap.put("TEST_ID", 30);
	    colInfoMap.put("TEST_NAME", 200);
	    colInfoMap.put("FLAG_YN", 1);
	    colInfoMap.put("INPUT_DT", 14);
    }
	
	/**
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		
		gridSize 			= Integer.parseInt(StringUtil.nullCheck(this.getInitJobParam("gridSize"), "2")); // 쓰레드 갯수
	    chunkSize 			= Integer.parseInt(StringUtil.nullCheck(this.getInitJobParam("chunkSize"), "20"));
		outputFileFullPath 	= StringUtil.nullCheck(this.getInitJobParam("outputFileFullPath"), "");
	    charset 			= StringUtil.nullCheck(this.getInitJobParam("charset"), "UTF-8");
	    append 				= Boolean.valueOf(StringUtil.nullCheck(this.getInitJobParam("append"), "false"));
	    
        /*******************************************************************
        테이블 SAMPLE_TEST에 데이터를 파일로 저장(병렬쓰레드처리). Reader/Processor/Writer 별도클래스로 구현.
        실행파라메터 : spring.batch.job.names=tableToFileJob gridSize=3 chunkSize=20 outputFileFullPath=C:/Temp/SAMPLE_DATA/table/SAMPLE_TEST.sam
        *******************************************************************/
		this.addStep(this.parallelMasterStep());
	}
	
	/**
	 * Step 스코프에 해당하는 TaskExecutor
	 * @param executor
	 * @return
	 */
    @Bean
	public TaskExecutor partitionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 스레드 수 설정
        executor.setCorePoolSize(gridSize);          	// 기본 스레드 수
        executor.setMaxPoolSize(gridSize);           	// 최대 스레드 수
        executor.setQueueCapacity(0);  					// 큐 사용하지 않음 → 즉시 쓰레드 실행
        // 거부 정책 (큐가 가득 찼을 때)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("batch-default-");
        
        // Shutdown 시 작업완료 대기시간 설정여부
        executor.setWaitForTasksToCompleteOnShutdown(true); 
        // Shutdown 시 작업완료 대기시간 설정. 1시간 대기 설정 (배치 작업 시간에 맞춰 충분히 길게 설정)
        executor.setAwaitTerminationSeconds(60*60*1);
        
        executor.initialize();
        return executor;
	}

	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
	/**
	 * 병렬처리 Master Step
	 * @param chunkSize
	 * @param gridSize
	 * @return
	 */
	private Step parallelMasterStep() {
		callLog(this, "parallelMasterStep");
		return new StepBuilder("parallelMasterStep", jobRepository)
				.partitioner("parallelSlaveStep", queryToFilePartitioner(gridSize))
				.step(parallelSlaveStep())
				.gridSize(gridSize)
				.taskExecutor(partitionTaskExecutor())
				.build();
	}
	
	/**
	 * 병렬처리 Slave Step
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Step parallelSlaveStep() {
		callLog(this, "parallelSlaveStep");
		return new StepBuilder("parallelSlaveStep", jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerCommon)
				.reader(itemPartitionReader()) // Spring이 런타임에 주입
				.processor((ItemProcessor<? super Map, ? extends Map>) itemProcessor())
				/* 멀티쓰레드에서 writer 는 메서드호출 방식이 아닌, 프록시주입 형식으로 해야 함. */
				.writer((ItemWriter<? super Map>) itemWriter)
				.build();
	}
	/* --------------------------------- Step 설정 끝 ---------------------------------- */ 

	/* --------------------------------- Partitioner 설정 시작 -------------------------- */
    /**
     * Table 처리용 Partitioner
     * @return
     */
    @Bean
    @StepScope
    public QueryToFilePartitioner queryToFilePartitioner(int gridSize) {
    	callLog(this, "queryPartitioner", gridSize);
    	Map<String, Object> params = new HashMap<String, Object>();
    	params.put("Name", "하하");
    	
    	QueryToFilePartitioner queryToFilePartitioner = new QueryToFilePartitioner(
            sqlBatchSessionSample, 
            "net.dstone.batch.sample.SampleTestDao.selectListSampleTestAll", 
            "TEST_ID", 
            gridSize,
            this.outputFileFullPath
            ,params
        );
    	// partition 메서드에 로그 추가
        return queryToFilePartitioner;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
    	//callLog(this, "itemProcessor");
    	return new AbstractItemProcessor() {
			@Override
			public Object process(Object item) throws Exception {
				//callLog(this, "process", item);
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
	@Autowired
	FileItemWriter itemWriter;
	
    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> itemWriter() {
    	callLog(this, "itemWriter");
    	FileItemWriter writer = new FileItemWriter(outputFileFullPath, charset, append, colInfoMap); 
    	return writer;
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */

}
