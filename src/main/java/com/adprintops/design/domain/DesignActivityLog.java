package com.adprintops.design.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "design_activity_logs")
public class DesignActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "design_task_id", nullable = false)
    private DesignTask designTask;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public DesignActivityLog() {
    }

    public DesignActivityLog(Long id, DesignTask designTask, Long actorId, String actionType, String content) {
        this.id = id;
        this.designTask = designTask;
        this.actorId = actorId;
        this.actionType = actionType;
        this.content = content;
    }

    public static DesignActivityLogBuilder builder() {
        return new DesignActivityLogBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public DesignTask getDesignTask() { return designTask; }
    public void setDesignTask(DesignTask designTask) { this.designTask = designTask; }

    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getCreatedAt() { return createdAt; }

    public static class DesignActivityLogBuilder {
        private Long id;
        private DesignTask designTask;
        private Long actorId;
        private String actionType;
        private String content;

        public DesignActivityLogBuilder id(Long id) { this.id = id; return this; }
        public DesignActivityLogBuilder designTask(DesignTask designTask) { this.designTask = designTask; return this; }
        public DesignActivityLogBuilder actorId(Long actorId) { this.actorId = actorId; return this; }
        public DesignActivityLogBuilder actionType(String actionType) { this.actionType = actionType; return this; }
        public DesignActivityLogBuilder content(String content) { this.content = content; return this; }

        public DesignActivityLog build() {
            return new DesignActivityLog(id, designTask, actorId, actionType, content);
        }
    }
}
