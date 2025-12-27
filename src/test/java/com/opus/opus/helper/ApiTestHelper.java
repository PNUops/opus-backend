package com.opus.opus.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;

public abstract class ApiTestHelper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    protected String asJsonString(final Object content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected MockPart getMockPart(final String name, final Object content) {
        return new MockPart(name, asJsonString(content).getBytes(StandardCharsets.UTF_8));
    }

}

