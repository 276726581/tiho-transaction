package com.tiho.txtransaction.lancher;

import com.tiho.txtransaction.annotation.EnableTxTransactionServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@EnableTxTransactionServer
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TxTransactionApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(TxTransactionApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }
}
