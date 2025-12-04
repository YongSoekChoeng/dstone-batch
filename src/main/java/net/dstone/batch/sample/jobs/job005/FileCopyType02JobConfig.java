package net.dstone.batch.sample.jobs.job005;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.batch.common.items.AbstractItemProcessor;
import net.dstone.batch.common.items.FileItemRangeReader;
import net.dstone.batch.common.items.FileItemWriter;
import net.dstone.batch.common.partitioner.FilePartitioner;
import net.dstone.common.utils.StringUtil;

/**
 * <pre>
 * 파일을 복사 하는 Job.
 * 병렬쓰레드처리 두 가지 모드.
 * 1:N 복사(병렬쓰레드처리). 대량파일을 Line Range로 Partitioning하여 각각 저장.
 *   C:/Temp/aa.txt => C:/Temp/aa-out1.txt
 *                     C:/Temp/aa-out2.txt
 * </pre>
 */
@Component
@AutoRegJob(name = "fileCopyType02Job")
public class FileCopyType02JobConfig extends BaseJobConfig {

	/*********************************** 멤버변수 선언 시작 ***********************************/ 
	// spring.batch.job.names : @AutoRegJob 어노테이션에 등록된 name
	// gridSize : 병렬처리할 쓰레드 갯수
	// inputFileFullPath : 복사될 Full파일 경로
	// outputFileFullPath : 복사생성될 Full파일 경로.
	// outputFileDir : 복사생성될 파일의 디렉토리. outputFileFullPath가 존재할 경우 무시. outputFileFullPath가 존재하지 않을 경우 이 디렉토리에 생성하되 파일명은 inputFileFullPath의 파일명을 참고하여 자동으로 결정.
	// charset : 생성할 파일의 캐릭터셋
	// append  : 작업수행시 파일 초기화여부. true-초기화 하지않고 이어서 생성. false-초기화 후 새로 생성.
	private int gridSize = 0;		// 쓰레드 갯수
	String inputFileFullPath = "";	// 원본 Full파일 경로
	String outputFileFullPath = "";	// 1:1 복사에서 생성될 Full파일 경로 
	String outputFileDir = "";		// 1:N 복사에서 복사파일들이 생성될 디렉토리
    String charset = "";			// 파일 인코딩
    boolean append = false;			// 기존파일이 존재 할 경우 기존데이터에 추가할지 여부
    LinkedHashMap<String,Integer> colInfoMap = new LinkedHashMap<String,Integer>(); // 데이터의 Layout 정의
    {
	    colInfoMap.put("TEST_ID", 30);
	    colInfoMap.put("TEST_NAME", 200);
	    colInfoMap.put("FLAG_YN", 1);
	    colInfoMap.put("INPUT_DT", 14);
    }
	/*********************************** 멤버변수 선언 끝 ***********************************/ 
    
	/** 
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");

		/*** Job Parameter 로부터 멤버변수 세팅 시작 ***/
		gridSize 			= Integer.parseInt(StringUtil.nullCheck(this.getInitJobParam("gridSize"), "2")); // 쓰레드 갯수
	    inputFileFullPath 	= StringUtil.nullCheck(this.getInitJobParam("inputFileFullPath"), "");
	    outputFileFullPath 	= StringUtil.nullCheck(this.getInitJobParam("outputFileFullPath"), "");
	    outputFileDir 		= StringUtil.nullCheck(this.getInitJobParam("outputFileDir"), "");
	    charset 			= StringUtil.nullCheck(this.getInitJobParam("charset"), "UTF-8");
	    append 				= Boolean.valueOf(StringUtil.nullCheck(this.getInitJobParam("append"), "false"));
	    /*** Job Parameter 로부터 멤버변수 세팅 끝 ***/
	    
	    int chunkSize 		= 5;

        /*******************************************************************
        1:N 복사(병렬쓰레드처리). 대량파일을 Line Range로 Partitioning하여 각각 저장.
        실행파라메터 : spring.batch.job.names=fileCopyType02Job gridSize=4 inputFileFullPath=C:/Temp/SAMPLE_DATA/SAMPLE01.sam outputFileDir=C:/Temp/SAMPLE_DATA/split
        *******************************************************************/
		this.addStep(this.parallelLinesRangeMasterStep(chunkSize, gridSize));
		
	}
	
	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
	/**
	 * 병렬처리(Line Range) Master Step
	 * @param chunkSize
	 * @param gridSize
	 * @return
	 */
	private Step parallelLinesRangeMasterStep(int chunkSize, int gridSize) {
		callLog(this, "parallelLinesRangeMasterStep", ""+chunkSize+", "+gridSize+"");
		return new StepBuilder("parallelLinesRangeMasterStep", jobRepository)
				.partitioner("parallelSlaveStep", fileCopyType02JobFilePartitioner(gridSize))
				.step(parallelLinesRangeSlaveStep(chunkSize))
				.gridSize(gridSize)
				.taskExecutor(baseTaskExecutor())
				.build();
	}
	
	/**
	 * 병렬처리(Line Range) Slave Step
	 * @return
	 */
	public Step parallelLinesRangeSlaveStep(int chunkSize) {
		callLog(this, "parallelLinesRangeSlaveStep");
		return new StepBuilder("parallelLinesRangeSlaveStep", jobRepository)
				.<Map<String, Object>, Map<String, Object>>chunk(chunkSize, txManagerCommon)
				.reader(fileCopyType02JobFileItemRangeReader) 	/* 멀티쓰레드에서는 메서드호출 방식이 아닌, 프록시주입 형식으로 해야 함. */
				.processor( itemProcessor())
				.writer(fileCopyType02JobFileItemWriter) 		/* 멀티쓰레드에서는 메서드호출 방식이 아닌, 프록시주입 형식으로 해야 함. */
				.build();
	}
	/* --------------------------------- Step 설정 끝 ---------------------------------- */ 

	/* --------------------------------- Partitioner 설정 시작 -------------------------- */
    /**
     * File 처리용 Partitioner(대용량 파일을 라인별로 Partition 을 생성하는 Partitioner)
     * @return
     */
    @Bean
    @StepScope
	public FilePartitioner fileCopyType02JobFilePartitioner(int gridSize) {
		callLog(this, "filePartitioner", gridSize);
		FilePartitioner filePartitioner = new FilePartitioner(inputFileFullPath, gridSize, outputFileDir);
		return filePartitioner;
	}
	/* --------------------------------- Partitioner 설정 끝 --------------------------- */

	/* --------------------------------- Reader 설정 시작 ------------------------------- */ 
	@Autowired
	FileItemRangeReader fileCopyType02JobFileItemRangeReader;
    /**
     * File 읽어오는 ItemReader. Partitioner 와 함께 사용.
     * @return
     */
    @Bean
    @StepScope
    public FileItemRangeReader fileCopyType02JobFileItemRangeReader() {
    	callLog(this, "fileCopyType02JobFileItemRangeReader");
    	FileItemRangeReader fileItemRangeReader = new FileItemRangeReader(inputFileFullPath, charset, colInfoMap);
        return fileItemRangeReader;
    }
	/* --------------------------------- Reader 설정 끝 -------------------------------- */ 

	/* --------------------------------- Processor 설정 시작 ---------------------------- */ 
    /**
     * File 처리용 ItemProcessor
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
    	callLog(this, "itemProcessor");
    	return new AbstractItemProcessor() {
			@Override
			public Object process(Object item) throws Exception {
				//callLog(this, "process", item);

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
	@Autowired
	FileItemWriter fileCopyType02JobFileItemWriter;
    /**
     * File 읽어오는 ItemReader. Partitioner 와 함께 사용.
     * @param minId
     * @param maxId
     * @return
     */
    /**
     * File 처리용 ItemWriter
     * @return
     */
    @Bean
    @StepScope
    public FileItemWriter fileCopyType02JobFileItemWriter() {
    	callLog(this, "itemWriter");
    	FileItemWriter writer = new FileItemWriter();
    	return writer;
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */

    /*************************************************************************************************************************/
    
}
