package net.dstone.batch.common.partitioner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.consts.Constants;
import net.dstone.batch.common.core.BasePartitioner;

/**
 * 범용 쿼리 기반 Partitioner
 * <pre>
 * < JobParameter >
 * 1. charset : 생성할 파일의 캐릭터셋. 옵션(기본값-UTF-8).
 * 2. append : 작업수행시 파일 초기화여부. true-초기화 하지않고 이어서 생성. false-초기화 후 새로 생성. 옵션(기본값-false).
 * 3. div : 컬럼정보 구분자. 특정하지 않았을 경우 고정길이로 핸들링. 옵션(기본값-"").
 * </pre>
 */
@Component
public class QueryToFilePartitioner extends BasePartitioner {
	
    private final SqlSessionTemplate sqlSessionTemplate;
    private final String queryId;
    private final String keyColumn;
    private int gridSize = 0;
    private final String outputFileFullPath;
    private final Map<String, Object> params = new HashMap<String, Object>();

    /**
	 * 범용 쿼리 기반 Partitioner 셍성자
	 * <pre>
	 * < JobParameter >
	 * 1. charset : 생성할 파일의 캐릭터셋. 옵션(기본값-UTF-8).
	 * 2. append : 작업수행시 파일 초기화여부. true-초기화 하지않고 이어서 생성. false-초기화 후 새로 생성. 옵션(기본값-false).
	 * 3. div : 컬럼정보 구분자. 특정하지 않았을 경우 고정길이로 핸들링. 옵션(기본값-"").
	 * </pre>
     * @param sqlSessionTemplate
     * @param queryId
     * @param keyColumn
     * @param gridSize
     * @param outputFileFullPath
     */
    public QueryToFilePartitioner(
    		SqlSessionTemplate sqlSessionTemplate,
            String queryId,
            String keyColumn,
            int gridSize,
            String outputFileFullPath
    ) {
        this(sqlSessionTemplate, queryId, keyColumn, gridSize, outputFileFullPath, new HashMap<>());
    }

    /**
	 * 범용 쿼리 기반 Partitioner 셍성자
	 * <pre>
	 * < JobParameter >
	 * 1. charset : 생성할 파일의 캐릭터셋. 옵션(기본값-UTF-8).
	 * 2. append : 작업수행시 파일 초기화여부. true-초기화 하지않고 이어서 생성. false-초기화 후 새로 생성. 옵션(기본값-false).
	 * 3. div : 컬럼정보 구분자. 특정하지 않았을 경우 고정길이로 핸들링. 옵션(기본값-"").
	 * </pre>
     * @param sqlSessionTemplate
     * @param queryId
     * @param keyColumn
     * @param gridSize
     * @param outputFileFullPath
     * @param params
     */
    public QueryToFilePartitioner(
    		SqlSessionTemplate sqlSessionTemplate,
            String queryId,
            String keyColumn,
            int gridSize,
            String outputFileFullPath,
            Map<String, Object> params
     ) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.queryId = queryId;
        this.keyColumn = keyColumn;
        this.gridSize = gridSize;
        this.outputFileFullPath = outputFileFullPath;
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
            		String outputFile = this.getOutputFileFullPath(this.outputFileFullPath, i);
            		context.put("MIN_ID", row.get("MIN_ID"));
            		context.put("MAX_ID", row.get("MAX_ID"));
                    context.putString(Constants.Partition.OUTPUT_FILE_PATH, outputFile);
                    
                    context.putString("charset", this.getJobParam("charset", "UTF-8").toString());
                    context.putString("append", this.getJobParam("append", "false").toString());
                    context.putString("div", this.getJobParam("div", "").toString());
                    
            		result.put("partition"+row.get("PARTITION_NO").toString(), context);
            	}
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create query-based partitions", e);
        } 
        return result;
    }
    
}
