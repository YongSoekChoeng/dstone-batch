package net.dstone.batch.sample.jobs.job002;

import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.AbstractJob;

@Component
@AutoRegJob(name = "singleTaskletJob")
public class SingleTaskletJob extends AbstractJob {

	@Override
	public void configJob() throws Exception {
		this.addTasklet(new SingleTasklet());
	}

}
