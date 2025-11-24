package net.dstone.batch.common.items;

import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public abstract class AbstractItemReader extends BaseItem implements ItemReader<Map<String, Object>>{

    @Override
    public abstract Map<String, Object> read();
    
}