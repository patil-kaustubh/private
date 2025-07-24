package com.metron.osv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metron.osv.config.AppConfig;
import com.metron.osv.model.OsvRequest;
import com.metron.osv.model.OsvRequest.PackageInfo;
import com.metron.osv.model.OsvResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OsvScannerService {

    private static final Logger logger = LoggerFactory.getLogger(OsvScannerService.class);

    @Autowired
    private AppConfig config;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Map<String, String>> lastSeenPackagesByFile = new HashMap<>();

    // @PostConstruct
    // public void initialScanAtStartup() {
    //     logger.info("Running initial scan at application startup...");
    //     pollAndScan(); // This triggers the scan once when app boots
    // }
   
    @Scheduled(fixedDelayString = "${polling.interval.ms}")
    public void pollAndScan() {
        logger.info("Polling started at {}", LocalDateTime.now());

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(config.packageJsonPath), "*.json")) {
            for (Path filePath : stream) {
                scanFile(filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to read JSON files in folder {}", config.packageJsonPath, e);
        }
    }

    private void scanFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        logger.info("Scanning file: {}", fileName);
        Map<String, String> lastSeen = lastSeenPackagesByFile.computeIfAbsent(fileName, k -> new HashMap<>());

        try {
            String content = new String(Files.readAllBytes(filePath));
            JSONObject json = new JSONObject(content);
            if (!json.has("dependencies")) {
                logger.warn("File {} has no 'dependencies' section", fileName);
                return;
            }

            JSONObject dependencies = json.getJSONObject("dependencies");
            for (String key : dependencies.keySet()) {
                String version = dependencies.getString(key);
                if (!version.equals(lastSeen.get(key))) {
                    logger.info("Detected new or updated package: {}@{} in {}", key, version, fileName);
                    lastSeen.put(key, version);
                    scanAndNotify(key, version, fileName);
                }
            }
        } catch (IOException | JSONException e) {
            logger.error("Error reading or parsing file {}", fileName, e);
        }
    }

    private void scanAndNotify(String packageName, String version, String fileName) {
        try {
            OsvRequest request = new OsvRequest();
            PackageInfo info = new PackageInfo();
            info.name = packageName;
            info.ecosystem = "npm";
            request.pkg = info;
            request.version = version;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(request), headers);

            ResponseEntity<OsvResponse> response = restTemplate.postForEntity(config.osvApiUrl, entity, OsvResponse.class);

            StringBuilder message = new StringBuilder();
            message.append("\n📦 *").append(packageName).append("@").append(version).append("*");
            message.append(" (from file `").append(fileName).append("`)");

            if (response.getBody() != null && response.getBody().vulnerabilities != null &&
                !response.getBody().vulnerabilities.isEmpty()) {
                for (OsvResponse.Vulnerability v : response.getBody().vulnerabilities) {
                    message.append("\n❗ *").append(v.id).append("*: ").append(v.summary);
                }
            } else {
                message.append("\n🛡️ No vulnerabilities found");
            }

            message.append("\n🕒 ").append(LocalDateTime.now());
            sendSlackMessage(message.toString());

        } catch (RestClientException e) {
            logger.error("Error calling OSV API for package {}: {}", packageName, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error scanning and notifying for package {}", packageName, e);
        }
    }

    private void sendSlackMessage(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(config.slackBotToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("channel", config.slackUserId);
            payload.put("text", text);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity("https://slack.com/api/chat.postMessage", entity, String.class);

            logger.info("Slack message sent: {}", response.getBody());

        } catch (RestClientException e) {
            logger.error("Error sending Slack message: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending Slack message", e);
        }
    }
}
