package net.dstone.batch.sample.jobs.job002;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.AbstractJob;

@Component
@AutoRegJob(name = "tableInsertTaskletJob")
public class TableInsertTaskletJob extends AbstractJob {

	@Override
	public void configJob() throws Exception {
		this.info(this.getClass().getName() + ".configJob() has been called !!!");
		this.addTasklet(new TableInsertTasklet(this.sqlSessionSample));
	}

}
