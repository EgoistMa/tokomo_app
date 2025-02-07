package org.tokomoapp.tokomo_be.util;

import java.util.UUID;

public class CodeUtil {
    public static String generateRandomCode() {
        return UUID.randomUUID().toString().replace("-","").toUpperCase();
    }
}
