package net.dstone.batch.common.items;

import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public abstract class AbstractItemReader<I> extends BaseItem implements ItemReader<I>{

    @Override
    public abstract I read();
    
}