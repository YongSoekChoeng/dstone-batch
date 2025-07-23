package net.dstone.batch.common.core;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;

public abstract class AbstractStep extends BatchBaseObject implements Step {

	@Override
	public String getName() {
		return this.getName();
	}

	@Override
	public boolean isAllowStartIfComplete() {
		return this.isAllowStartIfComplete();
	}

	@Override
	public int getStartLimit() {
		return this.getStartLimit();
	}

	@Override
	public void execute(StepExecution stepExecution) throws JobInterruptedException{
		this.execute(stepExecution);
	}

}
