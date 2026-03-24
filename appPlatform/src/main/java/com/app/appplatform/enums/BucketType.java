package com.app.appplatform.enums;

public enum BucketType {
    DEFAULT("configfile"),
    LOGS("applogs"),
    APPS("apps"),
    DYNAMIC_CONFIG("dynamic-configs"),
    MEDIA("media");

    private final String bucketName;

    BucketType(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}