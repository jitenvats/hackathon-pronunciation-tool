package com.wellsfargo.hackathon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	private static final String TITLE = "Pronunciation API";
	private static final String DESCRIPTION = "Hackathon 2022";
	private static final String VERSION = "1.0.0";
	
	ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title(TITLE)
				.description(DESCRIPTION)
	            .termsOfServiceUrl("")
	            .version(VERSION)
	            .contact(new Contact("Cross Border Team", "", "pijush.k.ghosh@wellsfargo.com"))
	            .build();
	}
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.paths(Predicates.not(PathSelectors.regex("/error.*")))
				.paths(Predicates.not(PathSelectors.regex("/actuator.*")))
				.paths(Predicates.not(PathSelectors.regex("/operation-handler.*")))
				.build();
	}
	
	

}
