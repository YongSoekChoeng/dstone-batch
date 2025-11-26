package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.batch.common.items.AbstractFileItemProcessor;
import net.dstone.batch.common.items.AbstractItemReader;
import net.dstone.batch.common.items.FileItemWriter;
import net.dstone.common.utils.DateUtil;
import net.dstone.common.utils.FileUtil;
import net.dstone.common.utils.StringUtil;

/**
 * 파일 C:/Temp/SAMPLE_TEST.sam 파일 에 테스트데이터를 입력하는 Job
 */
@Component
@AutoRegJob(name = "fileInsertJobConfig")
public class FileInsertJobConfig extends BaseJobConfig {

	/**
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		int chunkSize = 5000;
		// 01. 기존데이터 삭제
		this.addTasklet(this.deleteTasklet("fileInsertJobConfig-deleteTasklet"));
		// 02. 신규데이터 입력
		this.addStep(this.workerStep("fileInsertJobConfig-workerStep", chunkSize));
	}
	
    /**************************************** 01.Reader/Processor/Writer 별도클래스로 생성 ****************************************/

	/* --------------------------------- Step 설정 시작 --------------------------------- */ 
	/**
	 * 단일처리 Step
	 * @param chunkSize
	 * @return
	 */
	private Step workerStep(String stepName, int chunkSize) {
		callLog(this, "workerStep("+stepName+", "+chunkSize+" )", ""+stepName+", "+chunkSize+"");
		return new StepBuilder(stepName, jobRepository)
				.<Map, Map>chunk(chunkSize, txManagerSample)
				.reader( itemReader() )
				.processor((ItemProcessor<String, String>) itemProcessor())
				.writer(itemWriter(null, null, null))
				.build();
	}
	/* --------------------------------- Step 설정 끝 ---------------------------------- */ 

	/* --------------------------------- Tasklet 설정 시작 ------------------------------- */ 
	@SuppressWarnings("unused")
	private Tasklet deleteTasklet(String taskletName) {
		callLog(this, "deleteTasklet");
	    return new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				callLog(this, "execute");
				
				// 파라메터 전달(조회)
	            String filePath = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("filePath").toString();
	            log("filePath["+filePath+"]");
	            
	            if( FileUtil.isFileExist(filePath) ) {
	            	FileUtil.deleteFile(filePath);
	            }
				return RepeatStatus.FINISHED;
			}
		};
	}
	/* --------------------------------- Tasklet 설정 끝 ------------------------------- */ 

	/* --------------------------------- Reader 설정 시작 ------------------------------- */ 
    /**
     * Table 읽어오는 ItemReader
     * @return
     */
    @Bean
    @StepScope
    public ItemReader<Map<String, Object>> itemReader() {
    	return new AbstractItemReader() {
    		private ConcurrentLinkedQueue<Map<String, Object>> queue = null;

    		private void fillQueue() {
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
    public ItemProcessor<String, String> itemProcessor() {
    	return new AbstractFileItemProcessor() {
			@Override
			public String process(String item) throws Exception {
				callLog(this, "process", (item==null?"":item) );
		    	return item;
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
    public ItemWriter<? super String> itemWriter(
    	@Value("#{jobParameters['outputFilePath']}") String outputFilePath,
    	@Value("#{jobParameters['charset']}") String charset,
    	@Value("#{jobParameters['append']}") Boolean append
    ) {
    	return new FileItemWriter(outputFilePath, charset, append);
    }
	/* --------------------------------- Writer 설정 끝 -------------------------------- */
    
    /*************************************************************************************************************************/
    
}
