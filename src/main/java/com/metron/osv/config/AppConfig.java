package com.metron.osv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${slack.bot.token}")
    public String slackBotToken;

    @Value("${slack.user.id}")
    public String slackUserId;

    @Value("${osv.api.url}")
    public String osvApiUrl;

    @Value("${package.json.path}")
    public String packageJsonPath;

    @Value("${polling.interval.ms}")
    public long pollingInterval;
}
