package com.adprintops.design;

import com.adprintops.design.domain.DesignActivityLogRepository;
import com.adprintops.design.domain.DesignFileRepository;
import com.adprintops.design.domain.DesignTask;
import com.adprintops.design.domain.DesignTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesignServiceImplTest {

    @Mock
    private DesignTaskRepository taskRepository;

    @Mock
    private DesignFileRepository fileRepository;

    @Mock
    private DesignActivityLogRepository logRepository;

    @TempDir
    private Path storageDirectory;

    @Test
    void uploadMultipartFileStoresTheFileInsideTheConfiguredDirectory() throws Exception {
        DesignTask task = DesignTask.builder()
                .id(10L)
                .taskCode("DSG-TEST")
                .orderItemId(20L)
                .status("IN_PROGRESS")
                .priority("NORMAL")
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(fileRepository.findByDesignTaskIdOrderByVersionNumberDesc(10L)).thenReturn(List.of());
        when(fileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DesignServiceImpl service = new DesignServiceImpl(
                taskRepository,
                fileRepository,
                logRepository,
                storageDirectory.toString()
        );
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "../mau-thiet-ke.cdr",
                "application/octet-stream",
                "corel-content".getBytes()
        );

        var response = service.uploadMultipartFile(10L, multipartFile, "SOURCE_COREL");

        Path savedPath = Path.of(response.filePath());
        assertThat(savedPath.getParent()).isEqualTo(storageDirectory.toAbsolutePath().normalize());
        assertThat(savedPath.getFileName().toString()).endsWith("_mau-thiet-ke.cdr");
        assertThat(Files.readString(savedPath)).isEqualTo("corel-content");
    }
}
