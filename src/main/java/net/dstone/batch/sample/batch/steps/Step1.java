package net.dstone.batch.sample.batch.steps;

import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegStep;
import net.dstone.batch.common.core.AbstractStep;

@Component
@AutoRegStep(parent = "Job1", name = "Step1", order = 1)
public class Step1 extends AbstractStep {
	@Override
	public void execute(StepExecution stepExecution) throws JobInterruptedException{
		this.info("net.dstone.batch.sample.batch.steps.Step1.execute(StepExecution) has been called !!!");
		super.execute(stepExecution);
	}
}
