package mas.curs.infsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InfsysApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfsysApplication.class, args);
    }

}
