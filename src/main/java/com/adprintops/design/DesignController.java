package com.adprintops.design;

import com.adprintops.design.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/design-tasks")
public class DesignController {

    private final DesignService designService;

    public DesignController(DesignService designService) {
        this.designService = designService;
    }

    @GetMapping
    public ResponseEntity<List<DesignTaskResponse>> getAllTasks(
            @RequestParam(required = false) Long designerId,
            @RequestParam(required = false) String status) {
        if (designerId != null) {
            return ResponseEntity.ok(designService.getTasksByDesigner(designerId));
        }
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(designService.getTasksByStatus(status));
        }
        return ResponseEntity.ok(designService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignTaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(designService.getTaskById(id));
    }

    @GetMapping("/code/{taskCode}")
    public ResponseEntity<DesignTaskResponse> getTaskByCode(@PathVariable String taskCode) {
        return ResponseEntity.ok(designService.getTaskByCode(taskCode));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<DesignTaskResponse> assignDesigner(
            @PathVariable Long id,
            @RequestBody AssignDesignerRequest request) {
        return ResponseEntity.ok(designService.assignDesigner(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DesignTaskResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateDesignStatusRequest request) {
        return ResponseEntity.ok(designService.updateTaskStatus(id, request));
    }

    @PostMapping("/{id}/files")
    public ResponseEntity<DesignFileResponse> uploadFile(
            @PathVariable Long id,
            @RequestBody UploadDesignFileRequest request) {
        DesignFileResponse response = designService.uploadDesignFile(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{id}/upload-file", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DesignFileResponse> uploadMultipartFile(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "fileType", required = false, defaultValue = "SOURCE_COREL") String fileType) {
        DesignFileResponse response = designService.uploadMultipartFile(id, file, fileType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/files/{fileId}/approve")
    public ResponseEntity<DesignFileResponse> approveFile(
            @PathVariable Long fileId,
            @RequestParam(required = false, defaultValue = "1") Long actorId) {
        return ResponseEntity.ok(designService.approveDesignFile(fileId, actorId));
    }
}
