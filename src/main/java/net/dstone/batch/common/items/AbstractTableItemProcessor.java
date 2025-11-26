package net.dstone.batch.common.items;

import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public abstract class AbstractTableItemProcessor extends BaseItem implements ItemProcessor<Map<String, Object>, Map<String, Object>> {

	@Override
	public abstract Map<String, Object> process(Map item) throws Exception;
}