package org.codelin.oauth.oauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.codelin.oauth.oauth.*.infrastructure.persistence")
public class OauthApplication {

    static void main(String[] args) {
        SpringApplication.run(OauthApplication.class, args);
    }

}
