package net.dstone.batch.common.items;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.core.BaseItem;

/**
 * ItemReader 구현체. 
 * 아래와 같은 흐름을 갖는다.
 * 
 * Job 시작
 *     │
 *     ▼
 * Step 시작
 *     │
 *     ▼
 * ┌─────────────────────────────────────┐
 * │  reader.open(executionContext)      │  ◀── Step 시작 시 1회 자동으로 호출. 이때의 ExecutionContext는 ItemStream 관리용context로 StepExecutionContext와는 별개임.(여기서 Step파라메터 세팅은 Step내에서 공유되지 않음)
 * └─────────────────────────────────────┘
 *     │
 *     ▼
 * ┌─────────────────────────────────────┐
 * │  Chunk 반복 (chunk size: 1000 기준)   │
 * │                                     │
 * │  ┌───────────────────────────────┐  │
 * │  │ reader.read() × 1000          │  │  ◀── null 반환될 때까지 반복. 여기서 비로소 StepExecutionContext가 생성됨.(여기서 Step파라메터 세팅은 Step내에서 공유됨)
 * │  │ processor.process() × 1000    │  │
 * │  │ writer.write(items)           │  │  
 * │  │ reader.update(context)        │  │  ◀── 매 chunk 커밋 후 자동으로 호출
 * │  └───────────────────────────────┘  │
 * │              ...반복...              │
 * └─────────────────────────────────────┘
 *     │
 *     ▼
 * ┌─────────────────────────────────────┐
 * │  reader.close()                     │  ◀── Step 종료 시 1회 자동으로 호출
 * └─────────────────────────────────────┘
 *     │
 *     ▼
 * Step 종료
 * 
 */
@Component
@StepScope
public class FileItemReader extends BaseItem implements ItemReader<String>, ItemStream {

    @Value("#{jobParameters['filePath']}")
    private String filePath;

    @Value("#{jobParameters['charset'] ?: 'UTF-8'}")
    private String charset;

    private BufferedReader reader;
    private long lineCount = 0;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
    	log(this.getClass().getName() + ".open() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), Charset.forName(charset))
            );
        } catch (Exception e) {
            throw new ItemStreamException("파일 오픈 실패: " + filePath, e);
        }
    }

    @Override
    public synchronized String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    	log(this.getClass().getName() + ".read() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        if (reader == null) {
            throw new IllegalStateException("Reader is not opened.");
        }
        String line = reader.readLine();
        if (line != null) {
            lineCount++;
            return line;
        }

        return null; // EOF → Step 종료
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    	log(this.getClass().getName() + ".update() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        executionContext.put("fileItemReader.lineCount", lineCount);
    }

    @Override
    public void close() throws ItemStreamException {
    	log(this.getClass().getName() + ".close() has been called !!! - 쓰레드명[" + Thread.currentThread().getName() + "]" );
        try {
            if (reader != null) {
                log("[FileItemReader] CLOSE. Total read lines = {"+lineCount+"}");
                reader.close();
            }
        } catch (Exception e) {
            throw new ItemStreamException("파일 종료 실패", e);
        }
    }

}
