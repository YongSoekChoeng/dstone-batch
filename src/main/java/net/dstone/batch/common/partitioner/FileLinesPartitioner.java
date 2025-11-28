package net.dstone.batch.common.partitioner;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.consts.Constants;
import net.dstone.batch.common.core.BaseBatchObject;
import net.dstone.common.utils.FileUtil;

/**
 * 대용량 파일을 라인별로 Partition 을 생성하는 Partitioner
 */
@Component
public class FileLinesPartitioner extends BaseBatchObject implements Partitioner {

    private final String filePath;

    public FileLinesPartitioner(String filePath) {
    	this.filePath = filePath;
    }
    
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
    	callLog(this, "partition", String.valueOf(gridSize));

    	if( FileUtil.isDirectory(filePath) || !FileUtil.isFileExist(filePath) ) {
    		throw new IllegalStateException("파일["+filePath+"]이 존재하지 않습니다.");
    	}
    	
    	Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
    	long totalLines = FileUtil.countLines(filePath);  // 파일 라인 수
        long chunkSize = totalLines / gridSize;
        long fromLine = 1;
        long toLine = chunkSize;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            
            context.putLong(Constants.Partition.FROM_LINE, fromLine);
            context.putLong(Constants.Partition.TO_LINE, toLine);
            context.putString(Constants.Partition.FILE_PATH, filePath);
            result.put("partition" + i, context);
            
            fromLine = toLine + 1;
            if(i == gridSize - 1) {
            	toLine = totalLines;
            }else {
            	toLine = toLine + chunkSize;
            }
        }
        debug("총 " + result.size() + "개의 파티션 생성");
        return result;
    }

}
