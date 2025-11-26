package net.dstone.batch.common.items;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public abstract class AbstractFileItemProcessor extends BaseItem implements ItemProcessor<String, String> {

	@Override
	public abstract String process(String item) throws Exception;
}