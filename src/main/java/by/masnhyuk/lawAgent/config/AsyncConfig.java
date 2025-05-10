package by.masnhyuk.lawAgent.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {
    @Bean
    public ExecutorService documentProcessingExecutor() {
        return Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder()
                        .setNameFormat("doc-parser-%d")
                        .build());
    }
}