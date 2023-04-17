package com.dgs.springbootlibrary.utils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ExtractJWT {

    public static String payloadJwtExtraction(String token, String extraction) {
        token = token.replace("Bearer ", "");
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        String[] entries = payload.split(",");
        Map<String, String> map = new HashMap<>();
        for (String entry : entries) {
            String[] entryKey = entry.split(":");
            if (entryKey[0].equals(extraction)) {
                int remove = 1;
                if (entryKey[1].endsWith("}")) {
                    remove = 2;
                }
                entryKey[1] = entryKey[1].substring(1, entryKey[1].length() - remove);
                map.put(entryKey[0], entryKey[1]);
            }
        }

        if (map.containsKey(extraction)) {
            return map.get(extraction);
        }

        return null;
    }
}