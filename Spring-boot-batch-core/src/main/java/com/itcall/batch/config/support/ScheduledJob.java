package com.itcall.batch.config.support;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

//import lombok.RequiredArgsConstructor;

//@Target(ElementType.TYPE)
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//@Configuration
//@EnableBatchProcessing
//@RequiredArgsConstructor
//@Scope("prototype")
//@DependsOn(value={"batchJobConfig"})
//public @interface ScheduledJob {
//
//}

@Configuration
@EnableBatchProcessing
public @interface ScheduledJob {

}
