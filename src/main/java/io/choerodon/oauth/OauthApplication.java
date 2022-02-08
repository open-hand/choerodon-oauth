package io.choerodon.oauth;

import org.hzero.autoconfigure.oauth.EnableHZeroOauth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableHZeroOauth
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"io.choerodon.oauth","org.hzero.oauth"})
public class OauthApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(OauthApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
