package net.dstone.batch.common.items;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

@Component
@StepScope
public class FileItemWriter extends BaseItem implements ItemStreamWriter<String> {

    private final String outputFilePath;
    private final String charset;
    private final boolean append;

    private BufferedWriter writer;
    
    public FileItemWriter(String outputFilePath, String charset, boolean append) {
    	this.outputFilePath = outputFilePath;
    	this.charset = charset;
    	this.append = append;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
    	callLog(this, "open", outputFilePath);
        try {
			writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputFilePath, append), Charset.forName(charset))
			);

        } catch (Exception e) {
            throw new ItemStreamException("파일 오픈 실패: " + outputFilePath, e);
        }
    }

    @Override
    public synchronized void write(Chunk<? extends String> chunk) throws Exception {
    	callLog(this, "write", "chunk[size:"+chunk.size()+"]");
        if (writer == null) {
            throw new IllegalStateException("Writer is not opened.");
        }
        for (String line : chunk) {
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