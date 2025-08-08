package net.dstone.batch.sample.jobs;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class SampleItemWriter implements ItemWriter<String> {

    @Override
    public void write(List<? extends String> items) throws Exception {
        System.out.println("--- ItemWriter 시작 ---");
        // 리스트에 담긴 아이템들을 순회하며 출력합니다.
        for (String item : items) {
            System.out.println("ItemWriter: 데이터를 출력합니다 -> " + item);
        }
        System.out.println("--- ItemWriter 종료 ---");
    }
}