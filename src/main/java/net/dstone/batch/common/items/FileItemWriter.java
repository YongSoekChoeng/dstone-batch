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

import net.dstone.batch.common.consts.Constants;
import net.dstone.batch.common.core.BaseItem;
import net.dstone.common.utils.FileUtil;
import net.dstone.common.utils.StringUtil;

/**
 * 파일핸들링을 위한 ItemWriter 구현체. 
 */
@Component
@StepScope
public class FileItemWriter extends BaseItem implements ItemStreamWriter<Map<String, Object>> {

    private final String outputFileFullPath;
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
     * @param outputFileFullPath(저장파일 전체경로)
     * @param charset(대상파일의 캐릭터셋)
     * @param append(파일이 존재할 경우 데이터를 추가할지 여부)
     * @param colInfoMap(라인 기준 데이터정보)
     */
    public FileItemWriter(String outputFileFullPath, String charset, boolean append, LinkedHashMap<String,Integer> colInfoMap) {
    	this(outputFileFullPath, charset, append, colInfoMap, "");
    }

    /**
     * 읽어온 데이터를 파일로 저장하는 생성자
     * @param charset(대상파일의 캐릭터셋)
     * @param append(파일이 존재할 경우 데이터를 추가할지 여부)
     * @param colInfoMap(라인 기준 데이터정보)
     */
    public FileItemWriter(String charset, boolean append, LinkedHashMap<String,Integer> colInfoMap) {
    	this(charset, append, colInfoMap, "");
    }

    /**
     * 읽어온 데이터를 파일로 저장하는 생성자
     * @param outputFileFullPath(저장파일 전체경로)
     * @param charset(대상파일의 캐릭터셋)
     * @param append(파일이 존재할 경우 데이터를 추가할지 여부)
     * @param colInfoMap(라인 기준 데이터정보)
     * @param div(라인 기준 데이터경계구분자. 구분자가 없을 경우 고정길이.)
     */
    public FileItemWriter(String outputFileFullPath, String charset, boolean append, LinkedHashMap<String,Integer> colInfoMap, String div) {
    	this.outputFileFullPath = outputFileFullPath;
    	this.charset = charset;
    	this.append = append;
    	this.colInfoMap = colInfoMap;
    	this.div = div;
    }

    /**
     * 읽어온 데이터를 파일로 저장하는 생성자
     * @param charset(대상파일의 캐릭터셋)
     * @param append(파일이 존재할 경우 데이터를 추가할지 여부)
     * @param colInfoMap(라인 기준 데이터정보)
     * @param div(라인 기준 데이터경계구분자. 구분자가 없을 경우 고정길이.)
     */
    public FileItemWriter( String charset, boolean append, LinkedHashMap<String,Integer> colInfoMap, String div) {
    	this.outputFileFullPath = "";
    	this.charset = charset;
    	this.append = append;
    	this.colInfoMap = colInfoMap;
    	this.div = div;
    }
    
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
    	callLog(this, "open", outputFileFullPath);
    	String filePath = getOutputFileFullPath();
        try {
        	if( !FileUtil.isFileExist(filePath) ) {
        		FileUtil.writeFile(
        			FileUtil.getFilePath(filePath)
        			, FileUtil.getFileName(filePath, true)
        			, ""
        			, charset
        		);
        	}
			writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePath, append), Charset.forName(charset))
			);

        } catch (Exception e) {
            throw new ItemStreamException("파일 오픈 실패: " + filePath, e);
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
                log("[FileItemWriter] CLOSE : {"+getOutputFileFullPath()+"}");
                writer.close();
            }
        } catch (Exception e) {
            throw new ItemStreamException("파일 닫기 실패: " + getOutputFileFullPath(), e);
        }
    }
    
    /**
     * Writing 을 위한 파일경로를 반환.<br>
     * <pre>
     * - 우선순위(1번값이 없을 경우 2번값 반환) - 
     * 1. 생성자 파라메터로 들어온 outputFileFullPath
     * 2. Step Parameter 에 키값(OUTPUT_FILE_PATH)로 저장된 값.
     * </pre>
     * @return
     */
    private String getOutputFileFullPath() {
    	String fileFullPath = this.outputFileFullPath;
    	if( StringUtil.isEmpty(fileFullPath) ) {
    		fileFullPath = this.getStepParam(Constants.Partition.OUTPUT_FILE_PATH).toString();
    	}
    	return fileFullPath;
    }

}