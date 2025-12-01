package net.dstone.batch.common.partitioner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.consts.Constants;
import net.dstone.batch.common.core.BasePartitioner;

/**
 * 범용 쿼리 기반 Partitioner
 */
@Component
public class QueryToFilePartitioner extends BasePartitioner implements Partitioner {

    private final SqlSessionTemplate sqlSessionTemplate;
    private final String queryId;
    private final String keyColumn;
    private int gridSize = 0;
    private final String inputFileFullPath;
    private final Map<String, Object> params = new HashMap<String, Object>();

    public QueryToFilePartitioner(
    		SqlSessionTemplate sqlSessionTemplate,
            String queryId,
            String keyColumn,
            int gridSize,
            String outputFileDir
    ) {
        this(sqlSessionTemplate, queryId, keyColumn, gridSize, outputFileDir, new HashMap<>());
    }

    public QueryToFilePartitioner(
    		SqlSessionTemplate sqlSessionTemplate,
            String queryId,
            String keyColumn,
            int gridSize,
            String inputFileFullPath,
            Map<String, Object> params
     ) {
    	
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.queryId = queryId;
        this.keyColumn = keyColumn;
        this.gridSize = gridSize;
        this.inputFileFullPath = inputFileFullPath;
        if( params!= null && params.size()>0) {
        	this.params.putAll(params);
        }
        this.params.put(Constants.Partition.PARTITION_YN, "Y");
        this.params.put(Constants.Partition.KEY_COLUMN, this.keyColumn);
        this.params.put(Constants.Partition.GRID_SIZE, this.gridSize);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

    	callLog(this, "partition", String.valueOf(gridSize));
        
    	int actualGridSize = this.gridSize > 0 ? this.gridSize : gridSize;
        this.params.put(Constants.Partition.GRID_SIZE, actualGridSize);
    	Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
        try {
            List<Map<String, Object>> partitionList = this.sqlSessionTemplate.selectList(this.queryId, this.params);
            if( partitionList != null && partitionList.size() > 0 ) {
            	
            	for(int i= 0; i<partitionList.size(); i++) {
            		Map<String, Object> row = partitionList.get(i);
            		ExecutionContext context = new ExecutionContext();
            		String outputFile = this.getOutputFileFullPath(this.inputFileFullPath, i);
            		context.put("MIN_ID", row.get("MIN_ID"));
            		context.put("MAX_ID", row.get("MAX_ID"));
                    context.putString(Constants.Partition.OUTPUT_FILE_PATH, outputFile);
            		result.put("partition"+row.get("PARTITION_NO").toString(), context);
            	}
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create query-based partitions", e);
        } 
    	log(this.getClass().getName() + ".partition() return ["+result+"]" );
        return result;
    }
    
}
