package com.opus.opus.helper;

import com.opus.opus.modules.file.application.processor.ImageProcessor;
import com.opus.opus.modules.file.application.storage.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public abstract class FileModuleIntegrationTest extends ApiTestHelper {

    @MockitoBean
    protected FileStorage fileStorage;

    @MockitoBean
    protected ImageProcessor imageProcessor;

    @BeforeEach
    void setUpFileModule(final WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        Mockito.when(imageProcessor.getOutputExtension()).thenReturn("webp");
    }
}