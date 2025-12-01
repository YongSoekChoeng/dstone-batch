package net.dstone.batch.sample.jobs.job002;

import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.batch.sample.jobs.job002.items.TableDeleteTasklet;
import net.dstone.batch.sample.jobs.job002.items.TableInsertTasklet;

/**
 * <pre>
 * 테이블 SAMPLE_TEST 에 테스트데이터를 입력하는 Job.
 * CREATE TABLE SAMPLE_TEST (
 *   TEST_ID VARCHAR(30) NOT NULL, 
 *   TEST_NAME VARCHAR(200), 
 *   FLAG_YN VARCHAR(1), 
 *   INPUT_DT DATE NOT NULL,  
 *   PRIMARY KEY  (TEST_ID)
 * )
 * 01. 기존데이터 삭제 - Tasklet
 * 02. 신규데이터 입력 - Tasklet
 * </pre>
 */
@Component
@AutoRegJob(name = "tableInsertType01Job")
public class TableInsertType01JobConfig extends BaseJobConfig {

    /**************************************** 00. Job Parameter 선언 시작 ****************************************/
	
    /**************************************** 00. Job Parameter 선언 끝 ******************************************/
	
	/**
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		
        /*******************************************************************
        1. 테이블 SAMPLE_TEST 에 테스트데이터를 입력
        	실행파라메터 : spring.batch.job.names=tableInsertType01Job dataCnt=80
        *******************************************************************/
		// 01. 기존데이터 삭제
		this.addTasklet(new TableDeleteTasklet(this.sqlBatchSessionSample));
		// 02. 신규데이터 입력
		this.addTasklet(new TableInsertTasklet(this.sqlBatchSessionSample));
	}
	
    /**************************************** 01.Reader/Processor/Writer 별도클래스로 생성 ****************************************/

    /*************************************************************************************************************************/
    
}
