package net.dstone.batch.sample.jobs.job001;

import java.util.LinkedList;
import java.util.Queue;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import net.dstone.batch.common.core.BatchBaseObject;

// 제네릭 T는 읽어올 데이터의 타입을 나타냅니다.
public class SampleItemReader<T> extends BatchBaseObject implements ItemReader<T> {

    private Queue<T> dataQueue = null;

    public SampleItemReader() {
        this.fillQueue();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void fillQueue() {
    	this.info("net.dstone.batch.sample.jobs.job001.SampleItemReader.fillQueue() has been called !!!");
    	LinkedList list = new LinkedList<String>();
    	for(int i=0; i<100; i++) {
    		list.add("QueueItem-" + (i+1));
    	}
    	this.dataQueue = list;
    	this.info("this.dataQueue.size() ===>>> " + this.dataQueue.size());
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    	T item = dataQueue.poll();
    	this.info("net.dstone.batch.sample.jobs.job001.SampleItemReader.read() has been called !!! ::: item["+item+"]" + "this.dataQueue.size() ===>>> " + this.dataQueue.size());
        // 큐가 비어있지 않다면 데이터를 하나 꺼내서 반환합니다.
        // 큐가 비어있다면 null을 반환하여 읽기가 끝났음을 알립니다.
        return item;
    }
}
