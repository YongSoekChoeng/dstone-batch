package net.dstone.batch.sample.jobs;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// 제네릭 T는 읽어올 데이터의 타입을 나타냅니다.
public class SampleItemReader<T> implements ItemReader<T> {

    private final Queue<T> dataQueue;

    public SampleItemReader(List<T> data) {
        // 생성자에서 데이터를 받아 큐(Queue)에 담아둡니다.
        // 큐를 사용하면 순차적으로 데이터를 꺼내기 편리합니다.
        this.dataQueue = new LinkedList<>(data);
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        // 큐가 비어있지 않다면 데이터를 하나 꺼내서 반환합니다.
        // 큐가 비어있다면 null을 반환하여 읽기가 끝났음을 알립니다.
        return dataQueue.poll();
    }
}
