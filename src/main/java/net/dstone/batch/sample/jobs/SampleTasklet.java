package net.dstone.batch.sample.jobs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegJob;

@Component
@AutoRegJob
public class SampleTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // 업무 로직 수행 (MyBatis XML Mapper 호출)
        Map<String, Object> param = new HashMap<>();
        param.put("value1", "데이터1");
        param.put("value2", "데이터2");

        //sqlSession.insert("SampleMapper.insertSampleData", param);

        System.out.println("SampleTasklet completed : " + param);

        return RepeatStatus.FINISHED; // 작업 완료
    }
}
