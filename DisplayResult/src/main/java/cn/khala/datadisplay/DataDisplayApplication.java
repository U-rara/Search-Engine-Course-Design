package cn.khala.datadisplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class DataDisplayApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DataDisplayApplication.class, args);
    }

}
