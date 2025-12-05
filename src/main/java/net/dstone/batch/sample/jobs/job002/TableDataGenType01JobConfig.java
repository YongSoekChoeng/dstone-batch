package net.dstone.batch.sample.jobs.job002;

import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;
import net.dstone.batch.sample.jobs.job002.items.TableDeleteTasklet;
import net.dstone.batch.sample.jobs.job002.items.TableInsertTasklet;

/**
 * 테이블 SAMPLE_TEST 에 테스트데이터를 입력하는 Job.
 * <pre>
 * 두 개의 Tasklet 으로 구현.
 * 01. 기존데이터 삭제 - Tasklet
 * 02. 신규데이터 입력 - Tasklet
 * 
 * CREATE TABLE SAMPLE_TEST (
 *   TEST_ID VARCHAR(30) NOT NULL, 
 *   TEST_NAME VARCHAR(200), 
 *   FLAG_YN VARCHAR(1), 
 *   INPUT_DT DATE NOT NULL,  
 *   PRIMARY KEY  (TEST_ID)
 * )
 * </pre>
 */
@Component
@AutoRegJob(name = "tableDataGenType01Job")
public class TableDataGenType01JobConfig extends BaseJobConfig {

	/*********************************** 멤버변수 선언 시작 ***********************************/ 
	// spring.batch.job.names : @AutoRegJob 어노테이션에 등록된 name
	// dataCnt : 생성할 데이터 건수
	private int dataCnt 		= 10000;		// 생성할 데이터 건수
    /*********************************** 멤버변수 선언 끝 ***********************************/ 
	
	/**
	 * Job 구성
	 */
	@Override
	public void configJob() throws Exception {
		callLog(this, "configJob");
		
        /*******************************************************************
        1. 테이블 SAMPLE_TEST 에 테스트데이터를 입력
        	실행파라메터 : spring.batch.job.names=tableDataGenType01Job dataCnt=10000
        *******************************************************************/
		// 01. 기존데이터 삭제
		this.addTasklet(new TableDeleteTasklet(this.sqlBatchSessionSample));
		// 02. 신규데이터 입력
		this.addTasklet(new TableInsertTasklet(this.sqlBatchSessionSample));
	}
	
}
