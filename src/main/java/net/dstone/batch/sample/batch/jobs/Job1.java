package net.dstone.batch.sample.batch.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;
import net.dstone.batch.common.core.AbstractJob;

@Component
@AutoRegJob(name = "Job1")
public class Job1 extends AbstractJob {
	
	@Override
	public void execute(JobExecution execution) {
		this.info("net.dstone.batch.sample.batch.jobs.Job1.execute(JobExecution) has been called !!!");
		super.execute(execution);
	}
	
}
