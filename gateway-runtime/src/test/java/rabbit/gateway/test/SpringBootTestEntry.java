package rabbit.gateway.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "rabbit.gateway")
@EnableR2dbcRepositories
@EnableTransactionManagement
@EnableScheduling
public class SpringBootTestEntry {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootTestEntry.class);
    }
}
