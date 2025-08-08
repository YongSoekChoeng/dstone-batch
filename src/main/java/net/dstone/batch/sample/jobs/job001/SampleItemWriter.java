package net.dstone.batch.sample.jobs.job001;

import org.springframework.batch.item.ItemWriter;

import net.dstone.batch.common.core.BatchBaseObject;

import java.util.List;

public class SampleItemWriter extends BatchBaseObject implements ItemWriter<String> {

    @Override
    public void write(List<? extends String> items) throws Exception {
        this.info("--- ItemWriter 시작 ---");
        // 리스트에 담긴 아이템들을 순회하며 출력합니다.
        for (String item : items) {
            this.info("ItemWriter: 데이터를 출력합니다 -> " + item);
        }
        this.info("--- ItemWriter 종료 ---");
    }
}