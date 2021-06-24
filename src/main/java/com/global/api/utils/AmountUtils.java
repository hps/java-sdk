package com.global.api.utils;

import java.math.BigDecimal;

public class AmountUtils {
    public static boolean areEqual(BigDecimal var1, BigDecimal var2) {
        int scale = var1.scale();
        if(var2.scale() > scale) {
            scale = var2.scale();
        }

        BigDecimal amount1 = var1.setScale(scale);
        BigDecimal amount2 = var2.setScale(scale);

        return amount1.equals(amount2);
    }
}
