package com.express.addressbook.common;

/**
 * 关键词 LIKE 字面量转义。顺序固定：! → !!、% → !%、_ → !_，每个字符只处理一次。
 * 返回 null 表示不过滤。
 */
public final class LikeEscaper {

    private LikeEscaper() {
    }

    public static String escape(String keyword) {
        if (keyword == null) {
            return null;
        }
        String value = keyword.strip();
        if (value.isEmpty()) {
            return null;
        }
        return value.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}
