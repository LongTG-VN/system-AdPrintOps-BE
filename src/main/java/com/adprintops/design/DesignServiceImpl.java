package com.adprintops.design;

import com.adprintops.design.domain.*;
import com.adprintops.design.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DesignServiceImpl implements DesignService {

    private final DesignTaskRepository taskRepository;
    private final DesignFileRepository fileRepository;
    private final DesignActivityLogRepository logRepository;
    private final Path designFilesPath;

    public DesignServiceImpl(DesignTaskRepository taskRepository,
                             DesignFileRepository fileRepository,
                             DesignActivityLogRepository logRepository,
                             @Value("${app.storage.design-files-path:D:/Design}") String designFilesPath) {
        this.taskRepository = taskRepository;
        this.fileRepository = fileRepository;
        this.logRepository = logRepository;
        this.designFilesPath = Paths.get(designFilesPath).toAbsolutePath().normalize();
    }

    @Override
    @Transactional
    public DesignTaskResponse createDesignTaskForOrderItem(Long orderItemId, String categoryCode, String productName) {
        String taskCode = "DSG-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        DesignTask task = DesignTask.builder()
                .taskCode(taskCode)
                .orderItemId(orderItemId)
                .status("DESIGNING")
                .priority("NORMAL")
                .designerNote("Nhiệm vụ thiết kế/dàn trang tự động sinh cho " + productName + " (" + categoryCode + ")")
                .build();

        DesignTask savedTask = taskRepository.save(task);

        DesignActivityLog log = DesignActivityLog.builder()
                .designTask(savedTask)
                .actionType("CREATED")
                .content("Nhiệm vụ thiết kế " + taskCode + " được khởi tạo tự động từ Đơn hàng.")
                .build();
        logRepository.save(log);

        return mapToTaskResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignTaskResponse getTaskById(Long id) {
        DesignTask task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ thiết kế với ID: " + id));
        return mapToTaskResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignTaskResponse getTaskByCode(String taskCode) {
        DesignTask task = taskRepository.findByTaskCode(taskCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ thiết kế với Mã: " + taskCode));
        return mapToTaskResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignTaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToTaskResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignTaskResponse> getTasksByDesigner(Long designerId) {
        return taskRepository.findByDesignerId(designerId).stream()
                .map(this::mapToTaskResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignTaskResponse> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::mapToTaskResponse)
                .toList();
    }

    @Override
    @Transactional
    public DesignTaskResponse assignDesigner(Long taskId, AssignDesignerRequest request) {
        DesignTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ thiết kế với ID: " + taskId));

        task.setDesignerId(request.designerId());
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        task.setStatus("IN_PROGRESS");
        DesignTask updatedTask = taskRepository.save(task);

        DesignActivityLog log = DesignActivityLog.builder()
                .designTask(updatedTask)
                .actorId(request.actorId())
                .actionType("ASSIGNED")
                .content("Phân công cho Designer ID: " + request.designerId() + " với độ ưu tiên: " + task.getPriority())
                .build();
        logRepository.save(log);

        return mapToTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    public DesignTaskResponse updateTaskStatus(Long taskId, UpdateDesignStatusRequest request) {
        DesignTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ thiết kế với ID: " + taskId));

        String oldStatus = task.getStatus();
        task.setStatus(request.status());
        if (request.note() != null) {
            task.setDesignerNote(request.note());
        }
        DesignTask updatedTask = taskRepository.save(task);

        DesignActivityLog log = DesignActivityLog.builder()
                .designTask(updatedTask)
                .actorId(request.actorId())
                .actionType("STATUS_CHANGED")
                .content("Cập nhật trạng thái từ " + oldStatus + " sang " + request.status() + ". Ghi chú: " + request.note())
                .build();
        logRepository.save(log);

        return mapToTaskResponse(updatedTask);
    }

    @Override
    @Transactional
    public DesignFileResponse uploadDesignFile(Long taskId, UploadDesignFileRequest request) {
        DesignTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ thiết kế với ID: " + taskId));

        List<DesignFile> existingFiles = fileRepository.findByDesignTaskIdOrderByVersionNumberDesc(taskId);
        int nextVersion = existingFiles.isEmpty() ? 1 : existingFiles.get(0).getVersionNumber() + 1;

        DesignFile file = DesignFile.builder()
                .designTask(task)
                .versionNumber(nextVersion)
                .fileType(request.fileType())
                .fileName(request.fileName())
                .filePath(request.filePath())
                .fileSizeBytes(request.fileSizeBytes() != null ? request.fileSizeBytes() : 0L)
                .uploadedBy(request.uploadedBy())
                .approved(false)
                .build();

        DesignFile savedFile = fileRepository.save(file);

        DesignActivityLog log = DesignActivityLog.builder()
                .designTask(task)
                .actorId(request.uploadedBy())
                .actionType("FILE_UPLOADED")
                .content("Đã tải lên phiên bản v" + nextVersion + " (" + request.fileName() + ") loại " + request.fileType())
                .build();
        logRepository.save(log);

        return mapToFileResponse(savedFile);
    }

    @Override
    @Transactional
    public DesignFileResponse uploadMultipartFile(Long taskId, org.springframework.web.multipart.MultipartFile file, String fileType) {
        DesignTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ thiết kế với ID: " + taskId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Tệp thiết kế không được để trống.");
        }

        String submittedFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file_corel.cdr";
        String originalFilename = Paths.get(submittedFilename.replace('\\', '/')).getFileName().toString();
        String savedFileName = System.currentTimeMillis() + "_" + originalFilename;

        try {
            java.nio.file.Files.createDirectories(designFilesPath);
            java.nio.file.Path filePath = designFilesPath.resolve(savedFileName).normalize();
            if (!filePath.startsWith(designFilesPath)) {
                throw new IllegalArgumentException("Tên tệp thiết kế không hợp lệ.");
            }
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String fullSavedPath = filePath.toAbsolutePath().toString();

            UploadDesignFileRequest req = new UploadDesignFileRequest(
                    fileType != null ? fileType : "SOURCE_COREL",
                    originalFilename,
                    fullSavedPath,
                    file.getSize(),
                    1L
            );
            return uploadDesignFile(taskId, req);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Lỗi lưu trữ tệp lên server: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public DesignFileResponse approveDesignFile(Long fileId, Long actorId) {
        DesignFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy file thiết kế với ID: " + fileId));

        file.setApproved(true);
        DesignFile approvedFile = fileRepository.save(file);

        DesignTask task = approvedFile.getDesignTask();
        task.setStatus("WAITING_FOR_PRINT");
        taskRepository.save(task);

        DesignActivityLog log = DesignActivityLog.builder()
                .designTask(task)
                .actorId(actorId)
                .actionType("APPROVED")
                .content("Phiên bản v" + approvedFile.getVersionNumber() + " (" + approvedFile.getFileName() + ") đã được DUYỆT IN!")
                .build();
        logRepository.save(log);

        return mapToFileResponse(approvedFile);
    }

    private DesignTaskResponse mapToTaskResponse(DesignTask task) {
        List<DesignFileResponse> fileResponses = fileRepository.findByDesignTaskIdOrderByVersionNumberDesc(task.getId())
                .stream().map(this::mapToFileResponse).toList();

        List<DesignActivityLogResponse> logResponses = logRepository.findByDesignTaskIdOrderByCreatedAtDesc(task.getId())
                .stream().map(this::mapToLogResponse).toList();

        return new DesignTaskResponse(
                task.getId(),
                task.getTaskCode(),
                task.getOrderItemId(),
                task.getDesignerId(),
                task.getStatus(),
                task.getPriority(),
                task.getDeadline(),
                task.getDesignerNote(),
                task.getCustomerFeedback(),
                fileResponses,
                logResponses,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private DesignFileResponse mapToFileResponse(DesignFile file) {
        return new DesignFileResponse(
                file.getId(),
                file.getDesignTask().getId(),
                file.getVersionNumber(),
                file.getFileType(),
                file.getFileName(),
                file.getFilePath(),
                file.getFileSizeBytes(),
                file.getUploadedBy(),
                file.isApproved(),
                file.getCreatedAt()
        );
    }

    private DesignActivityLogResponse mapToLogResponse(DesignActivityLog log) {
        return new DesignActivityLogResponse(
                log.getId(),
                log.getDesignTask().getId(),
                log.getActorId(),
                log.getActionType(),
                log.getContent(),
                log.getCreatedAt()
        );
    }
}
