package com.metron.osv.model;

import java.util.List;

public class OsvResponse {
    public List<Vulnerability> vulnerabilities;

    public static class Vulnerability {
        public String id;
        public String summary;
    }
}
