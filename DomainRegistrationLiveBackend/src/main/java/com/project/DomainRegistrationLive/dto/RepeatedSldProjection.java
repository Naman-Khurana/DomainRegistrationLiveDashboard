package com.project.DomainRegistrationLive.dto;


import java.util.List;

public record RepeatedSldProjection(
        String sld,
        Long tlds,
        List<String> tldList
) {}