package net.dstone.batch.sample.jobs.job005;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.batch.common.items.AbstractItemProcessor;
import net.dstone.batch.common.items.FileItemReader;
import net.dstone.batch.common.items.FileItemWriter;
import net.dstone.common.utils.StringUtil;

/**
 * <pre>
 * 파일을 복사 하는 Job.
 * 단일쓰레드처리
 * 1:1복사(단일쓰레드처리).
 *   C:/Temp/aa.txt => C:/Temp/aa-copy.txt.
 * </pre>
 */
@Component
@AutoRegJob(name = "fileCopyType01Job")
public class FileCopyType01JobConfig extends BaseJobConfig {

	/*********************************** 멤버변수 선언 시작 ***********************************/ 
	// spring.batch.job.names : @AutoRegJob 어노테이션에 등록된 name
	// inputFileFullPath : 복사될 Full파일 경로
	// outputFileFullPath : 복사생성될 Full파일 경로.
	// outputFileDir : 복사생성될 파일의 디렉토리. outputFileFullPath가 존재할 경우 무시. outputFileFullPath가 존재하지 않을 경우 이 디렉토리에 생성하되 파일명은 inputFileFullPath의 파일명을 참고하여 자동으로 결정.
	// charset : 생성할 파일의 캐릭터셋
	// append  : 작업수행시 파일 초기화여부. true-초기화 하지않고 이어서 생성. false-초기화 후 새로 생성.
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
	    inputFileFullPath 	= StringUtil.nullCheck(this.getInitJobParam("inputFileFullPath"), "");
	    outputFileFullPath 	= StringUtil.nullCheck(this.getInitJobParam("outputFileFullPath"), "");
	    outputFileDir 		= StringUtil.nullCheck(this.getInitJobParam("outputFileDir"), "");
	    charset 			= StringUtil.nullCheck(this.getInitJobParam("charset"), "UTF-8");
	    append 				= Boolean.valueOf(StringUtil.nullCheck(this.getInitJobParam("append"), "false"));
	    /*** Job Parameter 로부터 멤버변수 세팅 끝 ***/
	    
	    int chunkSize 		= 5;

        /*******************************************************************
        1:1복사(단일쓰레드처리).
        실행파라메터 : spring.batch.job.names=fileCopyType01Job inputFileFullPath=C:/Temp/SAMPLE_DATA/SAMPLE01.sam outputFileFullPath=C:/Temp/SAMPLE_DATA/SAMPLE01-copy.sam
        *******************************************************************/

	    this.addStep(this.workerStep("workerStep", chunkSize));
	}
	
	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
	/**
	 * 단일처리 Step
	 * @param chunkSize
	 * @return
	 */
	private Step workerStep(String stepName, int chunkSize) {
		callLog(this, "workerStep", ""+stepName+", "+chunkSize+"");
		
		return new StepBuilder(stepName, jobRepository)
				.<Map<String, Object>, Map<String, Object>>chunk(chunkSize, txManagerCommon)
				.reader( itemReader() )
				.processor( itemProcessor())
				.writer( itemWriter())
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
    public FileItemReader itemReader() {
    	callLog(this, "itemReader");
    	return new FileItemReader(inputFileFullPath, charset, colInfoMap);
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
    /**
     * File 처리용 ItemWriter
     * @return
     */
    @Bean
    @StepScope
    public FileItemWriter itemWriter() {
    	callLog(this, "itemWriter");
    	FileItemWriter writer = new FileItemWriter(outputFileFullPath, charset, append, colInfoMap);
    	return writer;
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */

    /*************************************************************************************************************************/
    
}
