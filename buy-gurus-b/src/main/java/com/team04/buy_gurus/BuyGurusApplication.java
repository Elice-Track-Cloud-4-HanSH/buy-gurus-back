package com.team04.buy_gurus;

import com.team04.buy_gurus.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
// @EnableConfigurationProperties(JwtProperties.class)
public class BuyGurusApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuyGurusApplication.class, args);
	}

}
