package raining.java_web_exp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@ServletComponentScan
//@EntityScan("raining.java_web_exp.model")
//@EnableJpaRepositories("raining.java_web_exp.repository, raining.java_web_exp.model")
//@ComponentScan("raining.java_web_exp.model")
//@ComponentScan(basePackages={"raining.java_web_exp.model"})
//@EntityScan(basePackages={"raining.java_web_exp.entity"})

public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
