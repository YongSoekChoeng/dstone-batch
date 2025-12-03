package net.dstone.batch.common.items;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public abstract class AbstractItemProcessor<I, O> extends BaseItem implements ItemProcessor<I, O> {

	@Override
	public abstract O process(I item) throws Exception;

	/**
	 * Step 시작 전에 진행할 작업
	 */
	@Override
	protected void doBeforeStep(StepExecution stepExecution) {
		
	}

}