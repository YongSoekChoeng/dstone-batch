package net.dstone.batch.common.partitioner;

import java.util.Map;

import org.springframework.batch.core.JobParameters;

public interface PartitionKeyGenerator {
	
	/**
     * 주어진 JobParameters와 gridSize를 기반으로 파티션 기준 키 집합을 생성합니다.
     * @param jobParameters 현재 Job의 파라미터
     * @param gridSize 파티션 개수
     * @return 각 파티션에 전달될 기준 키 (예: startId, endId) 맵의 집합
     */
    Map<String, Map<String, Object>> generateKeys(JobParameters jobParameters, int gridSize);
    
}
