package com.example.interview_agent.util;

import java.util.Map;

public class LanguageMapper {
    
    // Judge0 语言 ID 映射
    private static final Map<String, String> LANGUAGE_MAP = Map.of(
        "cpp", "54",        // C++ (GCC 9.2.0)
        "c", "50",          // C (GCC 9.2.0)
        "java", "62",       // Java (OpenJDK 13.0.1)
        "python", "71",     // Python (3.8.1)
        "javascript", "63", // JavaScript (Node.js 12.14.0)
        "python3", "71",    // Python 3 (3.8.1)
        "go", "60",         // Go (1.13.5)
        "csharp", "51",     // C# (Mono 6.6.0.161)
        "php", "68",        // PHP (7.4.1)
        "ruby", "72"        // Ruby (2.7.0)
    );
    
    /**
     * 根据语言名称获取 Judge0 语言 ID
     * @param language 语言名称
     * @return Judge0 语言 ID，默认返回 C++ 的 ID (54)
     */
    public static String getLanguageId(String language) {
        return LANGUAGE_MAP.getOrDefault(language.toLowerCase(), "54");
    }
    
    /**
     * 检查是否支持指定语言
     * @param language 语言名称
     * @return 是否支持
     */
    public static boolean isSupported(String language) {
        return LANGUAGE_MAP.containsKey(language.toLowerCase());
    }
    
    /**
     * 获取所有支持的语言
     * @return 支持的语言集合
     */
    public static java.util.Set<String> getSupportedLanguages() {
        return LANGUAGE_MAP.keySet();
    }
} 