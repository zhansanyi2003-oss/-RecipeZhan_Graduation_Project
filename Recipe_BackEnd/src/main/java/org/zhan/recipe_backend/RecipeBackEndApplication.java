package org.zhan.recipe_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecipeBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipeBackEndApplication.class, args);
	}

}
