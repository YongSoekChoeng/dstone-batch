package net.dstone.batch.sample.jobs.job002;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.batch.core.JobExecution;
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
import net.dstone.batch.common.items.TableItemWriter;
import net.dstone.common.utils.DateUtil;
import net.dstone.common.utils.StringUtil;

/**
 * 테이블 SAMPLE_TEST 에 테스트데이터를 입력하는 Job
 */
@Component
@AutoRegJob(name = "tableInsertTaskletJob")
public class TableInsertJobConfig extends BaseJobConfig {

    /**************************************** 00. Job Parameter 선언 시작 ****************************************/
	private int dataCnt = 0;
    /**************************************** 00. Job Parameter 선언 끝 ******************************************/
	
	/**
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		int chunkSize = 20;
        dataCnt = Integer.parseInt(StringUtil.nullCheck(this.getInitJobParam("dataCnt"), "100")); // 생성데이터 갯수
        
		// 01. 기존데이터 삭제
		this.addTasklet(new TableDeleteTasklet(this.sqlBatchSessionSample));
		// 02. 신규데이터 입력
		//this.addTasklet(new TableInsertTasklet(this.sqlBatchSessionSample));
		this.addStep(this.workerStep("workerStep", chunkSize));
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
    	return new AbstractItemReader() {
    		private ConcurrentLinkedQueue<Map<String, Object>> queue = null;

    		private void fillQueue() {
    			callLog(this, "fillQueue");
    			queue = new ConcurrentLinkedQueue<Map<String, Object>>();
    			int dataCnt = Integer.parseInt(this.getJobParam("dataCnt").toString()) ;
    			for(int i=0; i<dataCnt; i++) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("TEST_ID", StringUtil.filler(String.valueOf(i), 8, "0") );
                    //row.put("TEST_NAME", "이름-" + i);
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
    @Bean
    @StepScope
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {
    	callLog(this, "itemProcessor");
    	return new AbstractItemProcessor() {
			@Override
			public Map<String, Object> process(Map item) throws Exception {
				callLog(this, "process", item);
				// Thread-safe하게 새로운 Map 객체 생성
		        Map<String, Object> processedItem = new HashMap<>(item);
		        processedItem.put("TEST_NAME", "이름-" + processedItem.get("TEST_ID"));
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
    	TableItemWriter writer = new TableItemWriter(this.sqlBatchSessionSample, "net.dstone.batch.sample.SampleTestDao.insertSampleTest");
    	return writer;
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */
    
    /*************************************************************************************************************************/
    
}
