package com.metron.osv.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OsvRequest {
    @JsonProperty("package")
    public PackageInfo pkg;

    public String version;

    public static class PackageInfo {
        public String name;
        public String ecosystem;
    }
}