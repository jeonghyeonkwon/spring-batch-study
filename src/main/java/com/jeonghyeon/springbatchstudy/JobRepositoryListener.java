package com.jeonghyeon.springbatchstudy;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


public class JobRepositoryListener implements JobExecutionListener {
    @Autowired
    private JobRepository jobRepository;
    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();

        JobParameters jobParameters = new JobParametersBuilder().addString("requestDate", "20221024").toJobParameters();

        /*
        * getLastJobExecution : 맨 마지막으로 저장된 JobExecution을 가져올 수 있다.
        * */

        JobExecution lastJobExecution = jobRepository.getLastJobExecution(jobName, jobParameters);
    }
}
