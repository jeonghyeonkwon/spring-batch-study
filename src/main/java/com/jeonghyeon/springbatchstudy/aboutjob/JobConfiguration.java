package com.jeonghyeon.springbatchstudy.aboutjob;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
@RequiredArgsConstructor
public class JobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job batchJob1(){
        return this.jobBuilderFactory.get("batchJob1")
                .start(step1())
                .next(step2())
                .build();
    }

    private Step step2() {
        return stepBuilderFactory.get("step2").tasklet((contribution, chunkContext) -> {
            System.out.println("step2 has executed");
            return RepeatStatus.FINISHED;
        }).build();
    }

    private Step step1() {
        return stepBuilderFactory.get("step1").tasklet(((contribution, chunkContext) -> {
            System.out.println("step1 has executed");
            return RepeatStatus.FINISHED;
        })).build();
    }

    @Bean
    public Job batchJob2(){
        return this.jobBuilderFactory.get("batchJob")
                .start(flow())
                .next(step5())
                .end()
                .build();
    }

    private Step step5() {
        return stepBuilderFactory.get("step5").tasklet(((contribution, chunkContext) -> {
            System.out.println("step5 has executed");
            return RepeatStatus.FINISHED;
        })).build();
    }

    @Bean
    public Flow flow(){
        FlowBuilder<Flow> flowFlowBuilder = new FlowBuilder<>("flow");
        flowFlowBuilder.start(step3())
                .next(step4())
                .end();
        return flowFlowBuilder.build();
    }

    private Step step4() {
        return stepBuilderFactory.get("step4").tasklet(((contribution, chunkContext) -> {
            System.out.println("step4 has executed");
            return RepeatStatus.FINISHED;
        })).build();
    }

    private Step step3() {
        return stepBuilderFactory.get("step3").tasklet(((contribution, chunkContext) -> {
            System.out.println("step3 has executed");
            return RepeatStatus.FINISHED;
        })).build();
    }
}
