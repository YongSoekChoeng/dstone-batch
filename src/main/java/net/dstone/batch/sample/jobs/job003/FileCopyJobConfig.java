package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
import net.dstone.batch.common.items.FileItemRangeReader;
import net.dstone.batch.common.items.FileItemReader;
import net.dstone.batch.common.items.FileItemWriter;
import net.dstone.batch.common.partitioner.FileLinesPartitioner;
import net.dstone.batch.common.partitioner.FilesPartitioner;
import net.dstone.common.utils.StringUtil;

/**
 * 파일을 복사 하는 Job.<br>
 * 단일쓰레드처리, 병렬쓰레드처리 두 가지 모드.<br>
 * <pre>
 * 1. 1:1복사.
 *   C:/Temp/aa.txt => C:/Temp/aa-copy.txt.
 * 2. 1:N 복사. 대량파일을 Line Range로 Partitioning하여 각각 저장.
 *   C:/Temp/aa.txt => C:/Temp/aa-copy1.txt
 *                     C:/Temp/aa-copy2.txt
 * 3. 1:N 분할복사. 대량파일을 여러파일로 Partitioning하여 각각 저장.
 *   C:/Temp/aa.txt => C:/Temp/aa1.txt => C:/Temp/aa1-copy.txt
 *                     C:/Temp/aa2.txt    C:/Temp/aa1-copy.txt
 * </pre>
 */
@Component
@AutoRegJob(name = "fileCopyJob")
public class FileCopyJobConfig extends BaseJobConfig {

    /**************************************** 00. Job Parameter 선언 시작 ****************************************/
	private int gridSize = 0;	// 쓰레드 갯수
	String filePath = "";		// 원본 Full파일 경로
	String copyFilePath = "";	// 1:1 복사에서 생성될 Full파일 경로 
	String copyToDir = "";		// 1:N 복사에서 복사파일들이 생성될 디렉토리
    String charset = "";		// 파일 인코딩
    boolean append = false;		// 기존파일이 존재 할 경우 기존데이터에 추가할지 여부
    /**************************************** 00. Job Parameter 선언 끝 ******************************************/
	
    LinkedHashMap<String,Integer> colInfoMap = new LinkedHashMap<String,Integer>();
	
	/**
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		
		gridSize 		= Integer.parseInt(StringUtil.nullCheck(this.getInitJobParam("gridSize"), "2")); // 쓰레드 갯수
	    filePath 		= StringUtil.nullCheck(this.getInitJobParam("filePath"), "");
	    copyFilePath 	= StringUtil.nullCheck(this.getInitJobParam("copyFilePath"), "");
	    copyToDir 		= StringUtil.nullCheck(this.getInitJobParam("copyToDir"), "");
	    charset 		= StringUtil.nullCheck(this.getInitJobParam("charset"), "UTF-8");
	    append 			= Boolean.valueOf(StringUtil.nullCheck(this.getInitJobParam("append"), "false"));
	    
	    colInfoMap.put("TEST_ID", 30);
	    colInfoMap.put("TEST_NAME", 200);
	    colInfoMap.put("FLAG_YN", 1);
	    colInfoMap.put("INPUT_DT", 14);
	    
	    int chunkSize = 5;

        /*******************************************************************
        1. 테스트용 파일을 복사(1:1복사)
        	실행파라메터 : spring.batch.job.names=fileCopyJob filePath=C:/Temp/SAMPLE_DATA/SAMPLE01.sam
        *******************************************************************/
        
		// 단일처리 Step(filePath 을 copyFilePath로 복사한다.)
	    this.addStep(this.workerStep("workerStep", chunkSize));
	    
		// 병렬처리 Step
		//this.addStep(this.parallelMasterStep(chunkSize, gridSize));
	}
	
    /**************************************** 01.Reader/Processor/Writer 별도클래스로 생성 ****************************************/

	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
	/**
	 * 단일처리 Step
	 * @param chunkSize
	 * @return
	 */
	private Step workerStep(String stepName, int chunkSize) {
		callLog(this, "workerStep", ""+stepName+", "+chunkSize+"");
		
		return new StepBuilder(stepName, jobRepository)
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
				.partitioner("parallelSlaveStep", fileLinesPartitioner(gridSize))
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
     * File 읽어오는 ItemReader
     * @return
     */
    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemReader() {
    	callLog(this, "itemReader");
    	return new FileItemReader(filePath, charset, colInfoMap);
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
        return new FileItemRangeReader(filePath, charset, colInfoMap);
    }
	/* --------------------------------- Reader 설정 끝 -------------------------------- */ 

	/* --------------------------------- Processor 설정 시작 ---------------------------- */ 
    /**
     * File 처리용 ItemProcessor
     * @return
     */
    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
    	callLog(this, "itemProcessor");
    	return new AbstractItemProcessor() {
			@Override
			public Object process(Object item) throws Exception {
				callLog(this, "process", item);

				// Thread-safe하게 새로운 Map 객체 생성
		        Map<String, Object> processedItem = (HashMap<String, Object>)item;
				// 예: FLAG_YN 값을 변경 
				processedItem.put("FLAG_YN", "Y");

		    	return processedItem;
			}
    	};
    }
	/* --------------------------------- Processor 설정 끝 ---------------------------- */ 

	/* --------------------------------- Writer 설정 시작 ------------------------------ */
    /**
     * File 처리용 ItemWriter
     * @return
     */
    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> itemWriter() {
    	callLog(this, "itemWriter");
    	FileItemWriter writer = new FileItemWriter(copyFilePath, charset, append, colInfoMap);
    	return writer;
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */

	/* --------------------------------- Partitioner 설정 시작 -------------------------- */
    /**
     * File 처리용 Partitioner(디렉토리내의 파일별로 Partition 을 생성하는 Partitioner)
     * @return
     */
    @Bean
    @Qualifier("filesPartitioner")
    @StepScope
    public FilesPartitioner filesPartitioner(int gridSize) {
    	callLog(this, "filesPartitioner", gridSize);
    	FilesPartitioner filesPartitioner = new FilesPartitioner(
    		filePath
    	);
        return filesPartitioner;
    }
    /**
     * File 처리용 Partitioner(대용량 파일을 라인별로 Partition 을 생성하는 Partitioner)
     * @return
     */
    @Bean
    @Qualifier("fileLinesPartitioner")
    @StepScope
    public FileLinesPartitioner fileLinesPartitioner(int gridSize) {
    	callLog(this, "fileLinesPartitioner", gridSize);
    	FileLinesPartitioner fileLinesPartitioner = new FileLinesPartitioner(
    		filePath
    	);
        return fileLinesPartitioner;
    }
	/* --------------------------------- Partitioner 설정 끝 --------------------------- */

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
	
    /*************************************************************************************************************************/
    
}
