package net.dstone.batch.sample.jobs.job003;

import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class TableUpdateProcessor extends BatchBaseObject implements ItemProcessor<Map<String, Object>, Map<String, Object>> {

	@Override
	public Map<String, Object> process(Map item) throws Exception {
		this.info(this.getClass().getName() + ".process("+item+") has been called !!!");

		// 예: TEST_NAME, FLAG_YN 값을 변경
		item.put("TEST_NAME", item.get("TEST_ID")+"-이름");
		item.put("FLAG_YN", "Y");

    	return item;
	}
}