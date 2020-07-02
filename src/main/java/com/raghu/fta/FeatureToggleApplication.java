package com.raghu.fta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class FeatureToggleApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeatureToggleApplication.class, args);
	}

	@Bean(name = "test", value = "")
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/token/generate-token").allowedOrigins("http://localhost:4200");
			}
		};
	}

}
