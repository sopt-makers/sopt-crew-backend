package org.sopt.makers.crew.main.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MentionSecretStringRemover {

    private static final String PREFIX_PATTERN = "-~!@#";
    private static final String SUFFIX_PATTERN = "\\[\\d+\\]%\\^&\\*\\+";

    public static String removeSecretString(String content) {
        Pattern prefixPattern = Pattern.compile(PREFIX_PATTERN);
        Pattern suffixPattern = Pattern.compile(SUFFIX_PATTERN);

        Matcher prefixMatcher = prefixPattern.matcher(content);
        content = prefixMatcher.replaceAll("");

        Matcher suffixMatcher = suffixPattern.matcher(content);
        content = suffixMatcher.replaceAll("");

        return content;
    }
}
