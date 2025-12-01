package net.dstone.batch.common.core;

import java.util.Map;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import net.dstone.common.utils.FileUtil;

/**
 * Step에서 내부적으로 사용되는 Partitioner객체들의 부모 클래스
 */
@Component
@StepScope
public abstract class BasePartitioner extends BaseBatchObject implements Partitioner {

	protected StepExecution stepExecution;
	
	public abstract Map<String, ExecutionContext> partition(int gridSize);
	
	protected String getOutputFileFullPath(String inputFileFullPath) {
    	return getOutputFileFullPath(inputFileFullPath, -1);
    }
	
	protected String getOutputFileFullPath(String inputFileFullPath, int i) {
    	String outputFileFullPath = "";
    	
    	String outputFileDir = "";
    	String outputFileName = "";
    	String outputFileExt = "";
    	
    	String inputFileDir = FileUtil.getFilePath(inputFileFullPath);
    	String inputFileName = FileUtil.getFileName(inputFileFullPath, false);
    	String inputFileExt = FileUtil.getFileExt(inputFileFullPath);
    	
    	outputFileDir = inputFileDir;
    	if(i < 0) {
    		outputFileName = inputFileName + "-out";
    	}else {
    		outputFileName = inputFileName + "-out" + i;
    	}
    	outputFileExt = inputFileExt;
    	
    	outputFileFullPath = outputFileDir + "/" + outputFileName +"."+ outputFileExt;

    	return outputFileFullPath;
    }

	protected String getOutputFileFullPath(String inputFileFullPath, String outputFileDir) {
    	return getOutputFileFullPath(inputFileFullPath, outputFileDir, -1);
    }

	protected String getOutputFileFullPath(String inputFileFullPath, String outputFileDir, int i) {
    	String outputFileFullPath = "";
    	
    	String outputFileName = "";
    	String outputFileExt = "";
    	
    	String inputFileName = FileUtil.getFileName(inputFileFullPath, false);
    	String inputFileExt = FileUtil.getFileExt(inputFileFullPath);
    	
    	if(i < 0) {
    		outputFileName = inputFileName + "-out";
    	}else {
    		outputFileName = inputFileName + "-out" + i;
    	}
    	outputFileExt = inputFileExt;
    	
    	outputFileFullPath = outputFileDir + "/" + outputFileName +"."+ outputFileExt;

    	return outputFileFullPath;
	}
	
}
