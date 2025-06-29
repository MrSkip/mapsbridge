package com.example.mapsbridge;

import com.example.mapsbridge.service.MailtrapService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MapsBridgeApplicationTests {
    @MockitoBean
    private MailtrapService mailtrapService;

    @Test
    void contextLoads() {
    }

}
