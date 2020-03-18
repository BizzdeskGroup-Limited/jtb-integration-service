package com.bizzdesk.jtb.integration;

import com.bizzdesk.jtb.integration.repository.UtilsHashRepository;
import com.gotax.framework.library.filter.LogFilter;
import com.gotax.framework.library.helpers.GoTaxLogHandler;
import com.gotax.framework.library.kafka.GoTaxLogChannel;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.servlet.Filter;

@Configuration
@EnableRedisRepositories(basePackageClasses = {UtilsHashRepository.class})
//@EnableScheduling
@EnableBinding(GoTaxLogChannel.class)
public class JtbIntegrationServiceConfiguration {

    @Bean
    public GoTaxLogHandler customHandlerInterceptor() {
        return new GoTaxLogHandler();
    }

    @Bean
    public Filter LogsFilter() {
        return new LogFilter(customHandlerInterceptor());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
