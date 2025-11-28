package net.dstone.batch.common.items;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;
import net.dstone.common.utils.FileUtil;
import net.dstone.common.utils.StringUtil;

/**
 * 파일핸들링을 위한 ItemWriter 구현체. 
 */
@Component
@StepScope
public class FileItemWriter extends BaseItem implements ItemStreamWriter<Map<String, Object>> {

    private final String outputFilePath;
    private final String charset;
    private boolean append;
    /**
     * 컬럼정보(컬럼명, 컬럼바이트길이)
     */
    private LinkedHashMap<String,Integer> colInfoMap = new LinkedHashMap<String,Integer>();
    /**
     * 구분자-한 라인을 파싱하여 맵에 담을때 파싱용 구분자. 구분자가 존재할 경우 컬럼정보.컬럼바이트길이를 무시하고 구분자로 분리. 반면 구분자가 빈 값일 경우 컬럼정보.컬럼바이트길이대로 고정길이로 파싱.
     */
    private String div = "";


    private BufferedWriter writer;

    /**
     * 읽어온 데이터를 파일로 저장하는 생성자
     * @param outputFilePath(저장파일 전체경로)
     * @param charset(대상파일의 캐릭터셋)
     * @param append(파일이 존재할 경우 데이터를 추가할지 여부)
     * @param colInfoMap(라인 기준 데이터정보)
     */
    public FileItemWriter(String outputFilePath, String charset, boolean append, LinkedHashMap<String,Integer> colInfoMap) {
    	this(outputFilePath, charset, append, colInfoMap, "");
    }

    /**
     * 읽어온 데이터를 파일로 저장하는 생성자
     * @param outputFilePath(저장파일 전체경로)
     * @param charset(대상파일의 캐릭터셋)
     * @param append(파일이 존재할 경우 데이터를 추가할지 여부)
     * @param colInfoMap(라인 기준 데이터정보)
     * @param div(라인 기준 데이터경계구분자)
     */
    public FileItemWriter(String outputFilePath, String charset, boolean append, LinkedHashMap<String,Integer> colInfoMap, String div) {
    	this.outputFilePath = outputFilePath;
    	this.charset = charset;
    	this.append = append;
    	this.colInfoMap = colInfoMap;
    	this.div = div;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
    	callLog(this, "open", outputFilePath);
        try {
        	if( !FileUtil.isFileExist(outputFilePath) ) {
        		FileUtil.writeFile(
        			FileUtil.getFilePath(outputFilePath)
        			, FileUtil.getFileName(outputFilePath, true)
        			, ""
        			, charset
        		);
        	}
			writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputFilePath, append), Charset.forName(charset))
			);

        } catch (Exception e) {
            throw new ItemStreamException("파일 오픈 실패: " + outputFilePath, e);
        }
    }

    @Override
    public synchronized void write(Chunk<? extends Map<String, Object>> chunk) throws Exception {
    	callLog(this, "write", "chunk[size:"+chunk.size()+"]");
        if (writer == null) {
            throw new IllegalStateException("Writer is not opened.");
        }
        for (Map<String, Object> item : chunk) {
        	
        	String line = "";
        	Iterator<String> colInfoKeys = colInfoMap.keySet().iterator();
        	
        	// 고정길이 일 경우
        	if( StringUtil.isEmpty(div) ) {
            	int offset = 0;
            	while(colInfoKeys.hasNext()) {
            		String key = colInfoKeys.next();
            		Integer len = colInfoMap.get(key);
            		String val = StringUtil.nullCheck(item.get(key), "");
            		line = StringUtil.appendFld(line, val, len, charset);
            		offset = offset + len;
            	}
            // 구분자 일 경우	
        	}else {
        		while(colInfoKeys.hasNext()) {
        			String key = colInfoKeys.next();
        			String val = StringUtil.nullCheck( item.get(key), "");
        			if(!StringUtil.isEmpty(line)) {
        				line = line + div;
        			}
        			line = line + val;

        		}
        	}
        	
        	
            writer.write(line);
            writer.newLine();
        }
        writer.flush();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // 필요 시 상태 저장 가능 (현재는 스킵 가능)
    }

    @Override
    public void close() throws ItemStreamException {
    	callLog(this, "close");
        try {
            if (writer != null) {
                log("[FileItemWriter] CLOSE : {"+outputFilePath+"}");
                writer.close();
            }
        } catch (Exception e) {
            throw new ItemStreamException("파일 닫기 실패: " + outputFilePath, e);
        }
    }

}