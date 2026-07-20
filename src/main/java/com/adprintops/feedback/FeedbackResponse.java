package com.adprintops.feedback;

import java.time.Instant;

public record FeedbackResponse(
        boolean success,
        String message,
        Instant timestamp
) {}
