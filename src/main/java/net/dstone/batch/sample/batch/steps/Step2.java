package net.dstone.batch.sample.batch.steps;

import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegStep;
import net.dstone.batch.common.core.AbstractStep;

@Component
@AutoRegStep(parent = "Job1", name = "Step2", order = 3)
public class Step2 extends AbstractStep {
	@Override
	public void execute(StepExecution stepExecution) throws JobInterruptedException{
		this.info("net.dstone.batch.sample.batch.steps.Step2.execute(StepExecution) has been called !!!");
		super.execute(stepExecution);
	}
}
