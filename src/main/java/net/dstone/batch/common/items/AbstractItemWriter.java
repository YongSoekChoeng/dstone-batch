package net.dstone.batch.common.items;

import java.util.Map;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import net.dstone.batch.common.core.BaseItem;

public abstract class AbstractItemWriter<O> extends BaseItem implements ItemWriter<O> {

	@Override
    public abstract void write(Chunk<? extends O> chunk);

}
