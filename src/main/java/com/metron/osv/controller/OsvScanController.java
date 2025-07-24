package com.metron.osv.controller;

import com.metron.osv.service.OsvScannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scan")
@Tag(name = "OSV Scan Controller", description = "Trigger OSV scan manually via API")
public class OsvScanController {

    @Autowired
    private OsvScannerService service;

    @PostMapping
    @Operation(summary = "Trigger vulnerability scan for all updated packages in all JSON files in folder")
    public String triggerScan() {
        service.pollAndScan(); // Reuses polling logic manually
        return "Folder scan triggered via API. Check logs or Slack for results.";
    }
}
