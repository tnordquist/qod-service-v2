package edu.cnm.deepdive.qod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class QodApplicationTest {

  public static void main(String[] args) {
    SpringApplication.run(QodApplicationTest.class, args);
  }

}
