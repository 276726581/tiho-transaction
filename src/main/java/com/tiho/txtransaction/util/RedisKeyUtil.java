package com.tiho.txtransaction.util;

public final class RedisKeyUtil {

    private RedisKeyUtil() {

    }

    public static String getRedisKey(Object... args) {
        StringBuilder builder = new StringBuilder();
        if (args.length > 0) {
            for (Object arg : args) {
                builder.append(arg).append(":");
            }
            builder.deleteCharAt(builder.length() - 1);
        }
        String str = builder.toString();
        return str;
    }

}
