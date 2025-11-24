package net.dstone.batch.common.items;

import java.util.Map;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import net.dstone.batch.common.core.BaseItem;

public abstract class AbstractItemWriter extends BaseItem implements ItemWriter<Map<String, Object>> {

	@Override
    public abstract void write(Chunk<? extends Map<String, Object>> chunk);
}
