package com.adprintops.order.domain;

public enum OrderStatus {
    PENDING("Chờ xác nhận"),
    DESIGNING("Thiết kế"),
    WAITING_FOR_PRINT("Đợi in"),
    PRINTING("Đang in"),
    WAITING_FOR_DELIVERY("Chờ Giao"),
    COMPLETED("Xong đơn");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static OrderStatus fromCode(String code) {
        if (code == null) return PENDING;
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
