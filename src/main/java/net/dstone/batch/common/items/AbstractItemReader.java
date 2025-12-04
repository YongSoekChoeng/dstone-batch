package net.dstone.batch.common.items;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public abstract class AbstractItemReader<I> extends BaseItem implements ItemReader<I>{

    @Override
    public abstract I read();

	/**
	 * Step 시작 전에 진행할 작업
	 */
	@Override
	protected void doBeforeStep(StepExecution stepExecution) {
		
	}

}