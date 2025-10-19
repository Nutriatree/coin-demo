package org.example.jiwoo_coin_demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials; // Credentials import 추가
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {

    @Value("${alchemy.api-url}")
    private String alchemyApiUrl;

    // 1. application.properties에서 개인키 값을 읽어올 변수 추가
    @Value("${wallet.private-key}")
    private String privateKey;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(alchemyApiUrl));
    }

    // 2. Credentials 객체를 생성하여 Bean으로 등록하는 메소드 추가
    @Bean
    public Credentials credentials() {
        // 읽어온 개인키 문자열로 Credentials 객체를 생성하여 반환
        return Credentials.create(privateKey);
    }
}
