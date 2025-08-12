package net.dstone.batch.sample.jobs.job001;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import net.dstone.batch.common.core.BatchBaseObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// 제네릭 T는 읽어올 데이터의 타입을 나타냅니다.
public class SampleItemReader<T> extends BatchBaseObject implements ItemReader<T> {

    private Queue<T> dataQueue = null;

    public SampleItemReader() {
        this.fillQueue();
    }
    
    private void fillQueue() {
    	LinkedList list = new LinkedList<String>();
    	this.dataQueue = list;
    	for(int i=0; i<100; i++) {
    		list.add("QueueItem-" + (i+1));
    	}
    	this.info("net.dstone.batch.sample.jobs.job001.SampleItemReader.fillQueue() ::: this.dataQueue==>>" + this.dataQueue);
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    	this.info("net.dstone.batch.sample.jobs.job001.SampleItemReader.read()");
    	T item = dataQueue.poll();
    	this.info("read() ::: item==>>" + item);
        // 큐가 비어있지 않다면 데이터를 하나 꺼내서 반환합니다.
        // 큐가 비어있다면 null을 반환하여 읽기가 끝났음을 알립니다.
        return item;
    }
}
