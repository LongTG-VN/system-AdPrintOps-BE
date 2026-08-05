package com.adprintops.design;

import com.adprintops.design.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DesignService {
    DesignTaskResponse createDesignTaskForOrderItem(Long orderItemId, String categoryCode, String productName);
    DesignTaskResponse getTaskById(Long id);
    DesignTaskResponse getTaskByCode(String taskCode);
    List<DesignTaskResponse> getAllTasks();
    List<DesignTaskResponse> getTasksByDesigner(Long designerId);
    List<DesignTaskResponse> getTasksByStatus(String status);
    DesignTaskResponse assignDesigner(Long taskId, AssignDesignerRequest request);
    DesignTaskResponse updateTaskStatus(Long taskId, UpdateDesignStatusRequest request);
    DesignFileResponse uploadDesignFile(Long taskId, UploadDesignFileRequest request);
    DesignFileResponse uploadMultipartFile(Long taskId, MultipartFile file, String fileType);
    DesignFileResponse approveDesignFile(Long fileId, Long actorId);
}
