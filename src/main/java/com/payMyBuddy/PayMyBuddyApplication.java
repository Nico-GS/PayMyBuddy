package com.payMyBuddy;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableEncryptableProperties
@SpringBootApplication
public class PayMyBuddyApplication {

	@Autowired
	private Environment env;

	@Bean
	public ModelMapper modelMapper() {return new ModelMapper();}

	public static void main(String[] args) {
		SpringApplication.run(PayMyBuddyApplication.class, args);
	}

}
