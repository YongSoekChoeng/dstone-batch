package net.dstone.batch.common.items;

import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public abstract class AbstractItemProcessor<I, O> extends BaseItem implements ItemProcessor<I, O> {

	@Override
	public abstract O process(I item) throws Exception;
}