package net.dstone.batch.sample.jobs.job005;

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

    /**************************************** 00. Job Parameter 선언 시작 ****************************************/
	String inputFileFullPath = "";	// 원본 Full파일 경로
	String outputFileFullPath = "";	// 1:1 복사에서 생성될 Full파일 경로 
	String outputFileDir = "";		// 1:N 복사에서 복사파일들이 생성될 디렉토리
    String charset = "";			// 파일 인코딩
    boolean append = false;			// 기존파일이 존재 할 경우 기존데이터에 추가할지 여부
    /**************************************** 00. Job Parameter 선언 끝 ******************************************/
	
    LinkedHashMap<String,Integer> colInfoMap = new LinkedHashMap<String,Integer>();
	
	/** 
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		
	    inputFileFullPath 	= "";
	    outputFileFullPath 	= "";
	    outputFileDir 		= "";
	    charset 			= StringUtil.nullCheck(this.getInitJobParam("charset"), "UTF-8");
	    append 				= Boolean.valueOf(StringUtil.nullCheck(this.getInitJobParam("append"), "false"));
	    
	    colInfoMap.put("TEST_ID", 30);
	    colInfoMap.put("TEST_NAME", 200);
	    colInfoMap.put("FLAG_YN", 1);
	    colInfoMap.put("INPUT_DT", 14);
	    
	    int chunkSize = 5;

        /*******************************************************************
        1:1복사(단일쓰레드처리).
        실행파라메터 : spring.batch.job.names=fileCopyType01Job inputFileFullPath=C:/Temp/SAMPLE_DATA/SAMPLE01.sam outputFileFullPath=C:/Temp/SAMPLE_DATA/SAMPLE01-copy.sam
        *******************************************************************/
	    inputFileFullPath 	= StringUtil.nullCheck(this.getInitJobParam("inputFileFullPath"), "");
	    outputFileFullPath 	= StringUtil.nullCheck(this.getInitJobParam("outputFileFullPath"), "");
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
				.<Map, Map>chunk(chunkSize, txManagerCommon)
				.reader( itemReader() )
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
    public ItemWriter<Map<String, Object>> itemWriter() {
    	callLog(this, "itemWriter");
    	FileItemWriter writer = new FileItemWriter(outputFileFullPath, charset, append, colInfoMap);
    	return writer;
    }
    /**
     * File 처리용 ItemWriter
     * @return
     */
    @Bean
    @StepScope
    public ItemWriter<Map<String, Object>> itemLinesRangeWriter() {
    	callLog(this, "itemWriter");
    	FileItemWriter writer = new FileItemWriter(charset, append, colInfoMap);
    	return writer;
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */

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
