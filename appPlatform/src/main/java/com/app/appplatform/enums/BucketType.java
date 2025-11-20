package com.app.appplatform.enums;

public enum BucketType {
    DEFAULT("configfile"),
    IMAGES("images"),
    DOCUMENTS("documents"),
    APPS("apps");

    private final String bucketName;

    BucketType(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}