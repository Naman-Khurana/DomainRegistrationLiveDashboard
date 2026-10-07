package com.project.DomainRegistrationLive.collector.certstream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;

import java.io.IOException;
import java.util.*;

public class CertStreamExtractor {

    private static final JsonFactory JSON = new JsonFactory();

    public static List<String> registeredDomains(String message) {
        List<String> raw = allDomains(message);
        if(raw.isEmpty()){
            return List.of();
        }

        Set<String> out = new HashSet<>();
        for(String name : raw){
            String domain = toRegisteredDomain(name);
            if (domain != null) {
                out.add(domain);
            }
        }

        return List.copyOf(out);
    }

    private static List<String> allDomains(String message) {
        try(JsonParser parser = JSON.createParser(message)){
            while(parser.nextToken() != null){
                if(parser.currentToken() == JsonToken.FIELD_NAME && "all_domains".equals(parser.currentName()) ) {
                    if (parser.nextToken() != JsonToken.START_ARRAY) {
                        return List.of();
                    }
                    List<String> names = new ArrayList<>();
                    while (parser.nextToken() == JsonToken.VALUE_STRING) {
                        names.add(parser.getText());
                    }
                    return names;
                }
            }
        }catch (IOException e){
            //malformed message
        }
        return List.of();
    }

    private static String toRegisteredDomain(String raw) {
        if(raw == null ){
            return null;
        }

        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith("*.")) {
            s = s.substring(2);
        }

        if (s.isEmpty() || s.length() > 253 || s.indexOf('*') >= 0 || InetAddresses.isInetAddress(s)) {
            return null;
        }

        try {
            InternetDomainName name = InternetDomainName.from(s);
            if (!name.isUnderRegistrySuffix()) {
                return null;
            }
            return name.topDomainUnderRegistrySuffix().toString();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return null;
        }
    }


}
