package net.dstone.batch.common.core.item;

import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
public abstract class BaseItemProcessor extends BaseItem implements ItemProcessor<Map<String, Object>, Map<String, Object>> {

	@Override
	public abstract Map<String, Object> process(Map item) throws Exception;
}