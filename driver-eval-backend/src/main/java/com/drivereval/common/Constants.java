package com.drivereval.common;

public class Constants {

    // ---- 角色常量 ----
    public static final int ROLE_PASSENGER = 1;
    public static final int ROLE_DRIVER = 2;
    public static final int ROLE_ADMIN = 3;

    // ---- 订单状态 ----
    public static final int ORDER_DISPATCHING = 0;
    public static final int ORDER_DISPATCHED = 1;
    public static final int ORDER_ACCEPTED = 2;
    public static final int ORDER_IN_PROGRESS = 3;
    public static final int ORDER_COMPLETED = 4;
    public static final int ORDER_CANCELLED_PASSENGER = 5;
    public static final int ORDER_CANCELLED_DRIVER = 6;

    // ---- 司机等级 ----
    public static final int LEVEL_NORMAL = 1;
    public static final int LEVEL_SILVER = 2;
    public static final int LEVEL_GOLD = 3;

    // ---- 在线状态 ----
    public static final int OFFLINE = 0;
    public static final int ONLINE = 1;
    public static final int PUNISHED = 2;

    // ---- 投诉/申诉状态 ----
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    // ---- 处罚状态 ----
    public static final int PUNISH_ACTIVE = 1;
    public static final int PUNISH_EXPIRED = 2;

    // ---- 计价规则 ----
    public static final double PRICE_BASE = 5.0;       // 起步价（元）
    public static final double PRICE_PER_KM = 2.5;     // 每公里费用（元）
}
