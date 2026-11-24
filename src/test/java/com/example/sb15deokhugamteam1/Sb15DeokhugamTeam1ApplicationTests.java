package com.example.sb15deokhugamteam1;

import com.example.sb15deokhugamteam1.config.PostgresTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(PostgresTestConfig.class)
@SpringBootTest
class Sb15DeokhugamTeam1ApplicationTests {

    @Test
    void contextLoads() {
    }

}
