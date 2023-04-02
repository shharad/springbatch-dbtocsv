package com.end.dbtocsv.listener;

import com.end.dbtocsv.repository.RssServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

@Slf4j
@RequiredArgsConstructor
public class RssServiceJobExecutionNotificationListener extends JobExecutionListenerSupport {

    private final RssServiceRepository rssServiceRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("RssServiceJobExecutionNotificationListener | beforeJob | Executing job id : " + jobExecution.getJobId());
        super.beforeJob(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("RssServiceJobExecutionNotificationListener | afterJob | Executing job id : " + jobExecution.getJobId());

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Job Completed");
        }
        rssServiceRepository.findAll()
                .forEach(rssService -> log.info("Found (" + rssService + ">) in the database."));
    }

}
