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
 * 디렉토리내의 파일별로 Partition 을 생성하는 Partitioner
 */
@Component
public class FilesPartitioner extends BaseBatchObject implements Partitioner {

    private final String directoryPath;

    public FilesPartitioner(String directoryPath) {
    	this.directoryPath = directoryPath;
    }
    
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
    	callLog(this, "partition", String.valueOf(gridSize));

    	Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
    	String[] files = FileUtil.readFileList(directoryPath);
    	
    	if( !FileUtil.isDirectory(directoryPath) || !FileUtil.isFileExist(directoryPath) ) {
    		throw new IllegalStateException("디렉토리["+directoryPath+"]가 존재하지 않습니다.");
    	}
    	if( FileUtil.readFileList(directoryPath).length == 0 ) {
    		throw new IllegalStateException("처리할 파일이 없습니다.");
    	}
        // 각 파일을 파티션으로 분할
        int partitionNumber = 0;
        for (String file : files) {
            ExecutionContext context = new ExecutionContext();
            context.putString(Constants.Partition.FILE_PATH, directoryPath + "/" + file);
            result.put("partition" + partitionNumber, context);
            partitionNumber++;
            // gridSize 제한
            if (partitionNumber >= gridSize) {
                break;
            }
        }
        debug("총 " + result.size() + "개의 파티션 생성");
        return result;
    }

}
