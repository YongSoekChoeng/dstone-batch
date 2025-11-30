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

    private final String inputFileFullPath;
    private final String copyToDir;
    private int gridSize = 0; 

    public FileLinesPartitioner(String inputFileFullPath, String copyToDir, int gridSize) {
    	this.inputFileFullPath = inputFileFullPath;
    	this.copyToDir = copyToDir; 
        this.gridSize = gridSize;
    }
    
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
    	callLog(this, "partition", String.valueOf(gridSize));

    	if( FileUtil.isDirectory(inputFileFullPath) || !FileUtil.isFileExist(inputFileFullPath) ) {
    		throw new IllegalStateException("파일["+inputFileFullPath+"]이 존재하지 않습니다.");
    	}
    	
    	int actualGridSize = this.gridSize > 0 ? this.gridSize : gridSize;
    	
    	Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
    	long totalLines = FileUtil.countLines(inputFileFullPath);  // 파일 라인 수
        long chunkSize = totalLines / actualGridSize;
        long fromLine = 1;
        long toLine = chunkSize;

        for (int i = 0; i < actualGridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            
            String outFilePath = "";

            context.putString(Constants.Partition.INPUT_FILE_PATH, inputFileFullPath);		// INPUT파일 Full Path
            context.putLong(Constants.Partition.FROM_LINE, fromLine);				// INPUT파일 From Line
            context.putLong(Constants.Partition.TO_LINE, toLine);					// INPUT파일 To Line
            context.putString(Constants.Partition.OUTPUT_FILE_PATH, outFilePath);	// INPUT파일의 Line Range(from Line~To Line)별 파일들이 복사 될 OUTPUT파일 Full Path
            result.put("partition" + i, context);
            
            fromLine = toLine + 1;
            if(i == actualGridSize - 1) {
            	toLine = totalLines;
            }else {
            	toLine = toLine + chunkSize;
            }
        }
        debug("총 " + result.size() + "개의 파티션 생성");
        return result;
    }

}
