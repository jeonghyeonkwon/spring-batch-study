package com.jeonghyeon.springbatchstudy.aboutstep;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class CustomTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        contribution.getStepExecution().getStepName();
        chunkContext.getStepContext().getJobName();
        return RepeatStatus.FINISHED;
    }
}
