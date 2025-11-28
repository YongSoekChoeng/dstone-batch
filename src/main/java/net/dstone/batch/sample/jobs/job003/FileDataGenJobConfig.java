package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import net.dstone.batch.common.items.AbstractItemReader;
import net.dstone.batch.common.items.FileItemWriter;
import net.dstone.common.utils.DateUtil;
import net.dstone.common.utils.FileUtil;
import net.dstone.common.utils.StringUtil;

/**
 * 테스트용 파일정보를 생성하는 Job
 */
@Component
@AutoRegJob(name = "fileDataGenJob")
public class FileDataGenJobConfig extends BaseJobConfig {

    /**************************************** 00. Job Parameter 선언 시작 ****************************************/
	private int dataCnt = 0;	// 생성데이터 갯수
	String filePath = "";		// 생성될 Full파일 경로
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
		
		dataCnt 	= Integer.parseInt(StringUtil.nullCheck(this.getInitJobParam("dataCnt"), "100")); 
	    filePath 	= "";
	    charset 	= StringUtil.nullCheck(this.getInitJobParam("charset"), "UTF-8");
	    append 		= Boolean.valueOf(StringUtil.nullCheck(this.getInitJobParam("append"), "false"));
	    
	    colInfoMap.put("TEST_ID", 30);
	    colInfoMap.put("TEST_NAME", 200);
	    colInfoMap.put("FLAG_YN", 1);
	    colInfoMap.put("INPUT_DT", 14);
	    
	    int chunkSize = 50;

        /*******************************************************************
        1. 테스트용 파일정보를 생성
        	실행파라메터 : spring.batch.job.names=fileDataGenJob dataCnt=100 append=false filePath=C:/Temp/SAMPLE_DATA/SAMPLE01.sam
        *******************************************************************/
	    filePath 	= StringUtil.nullCheck(this.getInitJobParam("filePath"), "");
		this.addStep(this.workerStep("workerStep", chunkSize));
	}
	
    /**************************************** 01.Reader/Processor/Writer 별도클래스로 생성 ****************************************/

	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
	/**
	 * 단일처리 Step
	 * @param chunkSize
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
     * Table 읽어오는 ItemReader
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemReader() {
    	callLog(this, "itemReader");
    	return new AbstractItemReader() {
    		private ConcurrentLinkedQueue<Map<String, Object>> queue = null;

    		private void fillQueue() {
    			callLog(this, "fillQueue");

    			// 기존데이터 삭제
    		    if( !append && FileUtil.isFileExist(filePath) ) {
    		    	FileUtil.deleteFile(filePath);
    		    }
    		    
    			queue = new ConcurrentLinkedQueue<Map<String, Object>>();
    			int dataCnt = Integer.parseInt(this.getJobParam("dataCnt").toString()) ;
    			for(int i=0; i<dataCnt; i++) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("TEST_ID", StringUtil.filler(String.valueOf(i), 8, "0") );
                    row.put("TEST_NAME", "이름-" + row.get("TEST_ID"));
                    row.put("FLAG_YN", "N");
                    row.put("INPUT_DT", DateUtil.getToDate("yyyyMMddHHmmss"));
                    queue.add(row);
    			}
    		}
    		
			@Override
			public Map<String, Object> read() {
				callLog(this, "read");
				Map<String, Object> row = null;
				if(queue == null) {
					fillQueue();
				}
				row = this.queue.poll();
				return row;
			}
		};
    }
	/* --------------------------------- Reader 설정 끝 -------------------------------- */ 

	/* --------------------------------- Processor 설정 시작 ---------------------------- */ 
    /**
     * Table 처리용 ItemProcessor
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
    	callLog(this, "itemProcessor");
    	return new AbstractItemProcessor() {
			@Override
			public Map<String, Object> process(Object item) throws Exception {
				callLog(this, "process", item);
				// Thread-safe하게 새로운 Map 객체 생성
		        Map<String, Object> processedItem = (HashMap)item;
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
    	FileItemWriter writer = new FileItemWriter(filePath, charset, append, colInfoMap);
    	return writer;
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */
    
    /*************************************************************************************************************************/
    
}
