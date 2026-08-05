package com.adprintops.design.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "design_tasks")
public class DesignTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", nullable = false, unique = true, length = 50)
    private String taskCode;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "designer_id")
    private Long designerId;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING_ASSIGNMENT";

    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "NORMAL";

    @Column(name = "deadline")
    private Instant deadline;

    @Column(name = "designer_note", columnDefinition = "TEXT")
    private String designerNote;

    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    @OneToMany(mappedBy = "designTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DesignFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "designTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DesignActivityLog> activityLogs = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public DesignTask() {
    }

    public DesignTask(Long id, String taskCode, Long orderItemId, Long designerId, String status, String priority, Instant deadline, String designerNote, String customerFeedback, List<DesignFile> files, List<DesignActivityLog> activityLogs) {
        this.id = id;
        this.taskCode = taskCode;
        this.orderItemId = orderItemId;
        this.designerId = designerId;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
        this.designerNote = designerNote;
        this.customerFeedback = customerFeedback;
        if (files != null) this.files = files;
        if (activityLogs != null) this.activityLogs = activityLogs;
    }

    public static DesignTaskBuilder builder() {
        return new DesignTaskBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getDesignerId() { return designerId; }
    public void setDesignerId(Long designerId) { this.designerId = designerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Instant getDeadline() { return deadline; }
    public void setDeadline(Instant deadline) { this.deadline = deadline; }

    public String getDesignerNote() { return designerNote; }
    public void setDesignerNote(String designerNote) { this.designerNote = designerNote; }

    public String getCustomerFeedback() { return customerFeedback; }
    public void setCustomerFeedback(String customerFeedback) { this.customerFeedback = customerFeedback; }

    public List<DesignFile> getFiles() { return files; }
    public void setFiles(List<DesignFile> files) { this.files = files; }

    public List<DesignActivityLog> getActivityLogs() { return activityLogs; }
    public void setActivityLogs(List<DesignActivityLog> activityLogs) { this.activityLogs = activityLogs; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class DesignTaskBuilder {
        private Long id;
        private String taskCode;
        private Long orderItemId;
        private Long designerId;
        private String status;
        private String priority;
        private Instant deadline;
        private String designerNote;
        private String customerFeedback;
        private List<DesignFile> files = new ArrayList<>();
        private List<DesignActivityLog> activityLogs = new ArrayList<>();

        public DesignTaskBuilder id(Long id) { this.id = id; return this; }
        public DesignTaskBuilder taskCode(String taskCode) { this.taskCode = taskCode; return this; }
        public DesignTaskBuilder orderItemId(Long orderItemId) { this.orderItemId = orderItemId; return this; }
        public DesignTaskBuilder designerId(Long designerId) { this.designerId = designerId; return this; }
        public DesignTaskBuilder status(String status) { this.status = status; return this; }
        public DesignTaskBuilder priority(String priority) { this.priority = priority; return this; }
        public DesignTaskBuilder deadline(Instant deadline) { this.deadline = deadline; return this; }
        public DesignTaskBuilder designerNote(String designerNote) { this.designerNote = designerNote; return this; }
        public DesignTaskBuilder customerFeedback(String customerFeedback) { this.customerFeedback = customerFeedback; return this; }
        public DesignTaskBuilder files(List<DesignFile> files) { this.files = files; return this; }
        public DesignTaskBuilder activityLogs(List<DesignActivityLog> activityLogs) { this.activityLogs = activityLogs; return this; }

        public DesignTask build() {
            return new DesignTask(id, taskCode, orderItemId, designerId, status, priority, deadline, designerNote, customerFeedback, files, activityLogs);
        }
    }
}
