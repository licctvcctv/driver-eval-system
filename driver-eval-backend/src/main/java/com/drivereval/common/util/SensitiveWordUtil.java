package com.drivereval.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensitiveWordUtil {

    private static final String END_FLAG = "isEnd";

    @SuppressWarnings("unchecked")
    private static volatile Map<String, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 初始化敏感词库，构建 DFA 前缀树
     *
     * @param words 敏感词列表
     */
    @SuppressWarnings("unchecked")
    public static void init(List<String> words) {
        Map<String, Object> newSensitiveWordMap = new HashMap<>(words.size());
        for (String word : words) {
            Map<String, Object> currentMap = newSensitiveWordMap;
            for (int i = 0; i < word.length(); i++) {
                String key = String.valueOf(word.charAt(i));
                Object obj = currentMap.get(key);
                if (obj != null) {
                    currentMap = (Map<String, Object>) obj;
                } else {
                    Map<String, Object> newMap = new HashMap<>();
                    newMap.put(END_FLAG, "0");
                    currentMap.put(key, newMap);
                    currentMap = newMap;
                }
            }
            currentMap.put(END_FLAG, "1");
        }
        sensitiveWordMap = newSensitiveWordMap;
    }

    /**
     * 检查文本中是否包含敏感词
     *
     * @param text 待检测文本
     * @return 是否包含敏感词
     */
    public static boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤文本中的敏感词，替换为 ***
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public static String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        int i = 0;
        while (i < sb.length()) {
            int length = checkSensitiveWord(sb.toString(), i);
            if (length > 0) {
                sb.replace(i, i + length, "***");
                i += 3;
            } else {
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * 从指定位置开始检查是否存在敏感词，返回敏感词长度，0 表示不存在
     */
    @SuppressWarnings("unchecked")
    private static int checkSensitiveWord(String text, int beginIndex) {
        Map<String, Object> currentMap = sensitiveWordMap;
        int matchLength = 0;
        int wordLength = 0;

        for (int i = beginIndex; i < text.length(); i++) {
            String key = String.valueOf(text.charAt(i));
            currentMap = (Map<String, Object>) currentMap.get(key);
            if (currentMap == null) {
                break;
            }
            matchLength++;
            if ("1".equals(currentMap.get(END_FLAG))) {
                wordLength = matchLength;
            }
        }
        return wordLength;
    }
}
