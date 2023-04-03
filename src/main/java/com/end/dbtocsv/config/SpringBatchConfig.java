package com.end.dbtocsv.config;

import com.end.dbtocsv.entity.RssService;
import com.end.dbtocsv.listener.RssServiceJobExecutionNotificationListener;
import com.end.dbtocsv.listener.RssServiceStepCompleteNotificationListener;
import com.end.dbtocsv.repository.RssServiceRepository;
import com.end.dbtocsv.util.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

@Configuration // Informs Spring that this class contains configurations
@EnableBatchProcessing // Enables batch processing for the application
@RequiredArgsConstructor
@Slf4j
public class SpringBatchConfig {

    private final RssServiceRepository rssServiceRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private final AppProperties appProperties;

    @Autowired
    RssServiceProcessor rssServiceProcessor;

    private final String[] headers = new String[]{"serviceId", "serviceName", "serviceType", "sourceSystem", "status", "remark", "createdBy", "createdOn", "modifiedBy", "modifiedOn"};

    Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
    String dateFormat = new SimpleDateFormat("yyyy-MM-dd'-'HH-mm-ss-SSS", Locale.forLanguageTag("tr-TR")).format(now);
    String outputFileName = "RssService_" + dateFormat + ".csv";

    @Bean
    public RepositoryItemReader<RssService> reader() {
        RepositoryItemReader<RssService> repositoryItemReader = new RepositoryItemReader<>();
        repositoryItemReader.setName("DbReader");
        repositoryItemReader.setRepository(rssServiceRepository);
        repositoryItemReader.setMethodName("findAll");
        final HashMap<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("serviceId", Sort.Direction.ASC);
        repositoryItemReader.setSort(sorts);
        return repositoryItemReader;
    }

    @Bean
    public FlatFileItemWriter<RssService> writer() {

        FlatFileItemWriter<RssService> flatFileItemWriter = new FlatFileItemWriter<>();
        flatFileItemWriter.setName("FileWriter");
        flatFileItemWriter.setResource(new FileSystemResource(appProperties.getFileOutputPath() + File.separator + outputFileName));
        flatFileItemWriter.setAppendAllowed(true);

        flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<RssService>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<RssService>() {
                    {
                        setNames(headers);
                    }
                });
            }
        });

        return flatFileItemWriter;
    }

    @Bean
    public ItemProcessor processor() {
        return new RssServiceProcessor();
    }

    @Bean
    public RssServiceJobExecutionNotificationListener stepExecutionListener() {
        return new RssServiceJobExecutionNotificationListener(rssServiceRepository);
    }

    @Bean
    public RssServiceStepCompleteNotificationListener jobExecutionListener() {
        return new RssServiceStepCompleteNotificationListener();
    }

    @Bean
    public Step step1() {

        return new StepBuilder("DbToCsv-step", jobRepository)
                .<RssService, RssService>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .listener(stepExecutionListener())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob() {

        return new JobBuilder("exportDataFromDbToCsv", jobRepository)
                .listener(jobExecutionListener())
                .flow(step1())
                .end().build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(10); // 10 Threads will execute in parallel
        return simpleAsyncTaskExecutor;
    }

}
