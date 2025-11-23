package net.dstone.batch.sample.jobs.job002;

import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.BaseJobConfig;

@Component
@AutoRegJob(name = "tableInsertTaskletJob")
public class TableInsertTaskletJobConfig extends BaseJobConfig {

    private void log(Object msg) {
    	this.debug(msg);
    	//System.out.println(msg);
    }

	@Override
	public void configJob() throws Exception {
		log(this.getClass().getName() + ".configJob() has been called !!!");
		this.addTasklet(new TableInsertTasklet(this.sqlBatchSessionSample));
	}

}
