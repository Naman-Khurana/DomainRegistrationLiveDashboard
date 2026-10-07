package com.project.DomainRegistrationLive.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RegistrarDictionary {

    private final Map<Long, String> registrarNames = new HashMap<>();
    
    // Splits on commas that are outside of double quotes
    private static final Pattern CSV_PATTERN = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    @PostConstruct
    public void init() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getResourceAsStream("/iana-registrars.csv"))))) {
            
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue; // Skip header
                }
                
                String[] columns = CSV_PATTERN.split(line);
                if (columns.length >= 2) {
                    try {
                        long id = Long.parseLong(columns[0].trim());
                        // Remove surrounding quotes if present
                        String name = columns[1].trim();
                        if (name.startsWith("\"") && name.endsWith("\"")) {
                            name = name.substring(1, name.length() - 1);
                        }
                        registrarNames.put(id, name);
                    } catch (NumberFormatException ignored) {
                        // Ignore lines where ID is not a number
                    }
                }
            }
            log.info("Loaded {} registrar names from iana-registrars.csv", registrarNames.size());
        } catch (Exception e) {
            log.warn("Could not load iana-registrars.csv. Registrar names will fall back to IANA IDs.", e);
        }
    }

    public String getName(Long id) {
        if (id == null) return null;
        return registrarNames.getOrDefault(id, "IANA #" + id);
    }
}
