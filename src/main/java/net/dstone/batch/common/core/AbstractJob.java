package net.dstone.batch.common.core;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersValidator;

public abstract class AbstractJob extends BatchBaseObject implements Job {

	@Override
	public String getName() {
		return this.getName();
	}

	@Override
	public boolean isRestartable() {
		return this.isRestartable();
	}

	@Override
	public void execute(JobExecution execution) {
		this.execute(execution);
	}
	
	@Override
	public JobParametersIncrementer getJobParametersIncrementer() {
		return this.getJobParametersIncrementer();
	}

	@Override
	public JobParametersValidator getJobParametersValidator() {
		return this.getJobParametersValidator();
	}

}
