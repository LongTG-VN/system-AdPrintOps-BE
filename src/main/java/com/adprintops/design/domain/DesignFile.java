package com.adprintops.design.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "design_files")
public class DesignFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_task_id", nullable = false)
    private DesignTask designTask;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Column(name = "file_type", nullable = false, length = 30)
    private String fileType = "PREVIEW_IMAGE";

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes = 0L;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "is_approved", nullable = false)
    private boolean approved = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public DesignFile() {
    }

    public DesignFile(Long id, DesignTask designTask, Integer versionNumber, String fileType, String fileName, String filePath, Long fileSizeBytes, Long uploadedBy, boolean approved) {
        this.id = id;
        this.designTask = designTask;
        this.versionNumber = versionNumber;
        this.fileType = fileType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.uploadedBy = uploadedBy;
        this.approved = approved;
    }

    public static DesignFileBuilder builder() {
        return new DesignFileBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DesignTask getDesignTask() { return designTask; }
    public void setDesignTask(DesignTask designTask) { this.designTask = designTask; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public Instant getCreatedAt() { return createdAt; }

    public static class DesignFileBuilder {
        private Long id;
        private DesignTask designTask;
        private Integer versionNumber;
        private String fileType;
        private String fileName;
        private String filePath;
        private Long fileSizeBytes;
        private Long uploadedBy;
        private boolean approved;

        public DesignFileBuilder id(Long id) { this.id = id; return this; }
        public DesignFileBuilder designTask(DesignTask designTask) { this.designTask = designTask; return this; }
        public DesignFileBuilder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public DesignFileBuilder fileType(String fileType) { this.fileType = fileType; return this; }
        public DesignFileBuilder fileName(String fileName) { this.fileName = fileName; return this; }
        public DesignFileBuilder filePath(String filePath) { this.filePath = filePath; return this; }
        public DesignFileBuilder fileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; return this; }
        public DesignFileBuilder uploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; return this; }
        public DesignFileBuilder approved(boolean approved) { this.approved = approved; return this; }

        public DesignFile build() {
            return new DesignFile(id, designTask, versionNumber, fileType, fileName, filePath, fileSizeBytes, uploadedBy, approved);
        }
    }
}
