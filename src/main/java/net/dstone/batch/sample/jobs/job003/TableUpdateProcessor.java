package net.dstone.batch.sample.jobs.job003;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BatchBaseObject;

@Component
public class TableUpdateProcessor extends BatchBaseObject implements ItemProcessor<Map<String, Object>, Map<String, Object>> {

    private void log(Object msg) {
    	this.debug(msg);
    	//System.out.println(msg);
    }

	@Override
	public Map<String, Object> process(Map item) throws Exception {
		log(this.getClass().getName() + ".process("+item+") has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
    	// Thread-safe하게 새로운 Map 객체 생성
        Map<String, Object> processedItem = new HashMap<>(item);
		// 예: TEST_NAME, FLAG_YN 값을 변경 
        processedItem.put("TEST_NAME", item.get("TEST_ID")+"-이름");
		processedItem.put("FLAG_YN", "Y");

    	return processedItem;
	}
}