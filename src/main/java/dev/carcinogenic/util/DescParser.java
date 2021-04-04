package dev.carcinogenic.util;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author amy
 * @since 3/30/21.
 */
public final class DescParser {
    private static final Pattern ALL_PARAMS_PATTERN = Pattern.compile("(\\(.*?\\))");
    private static final Pattern PARAMS_PATTERN = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|(:?L[^;]+;))");

    private DescParser() {
    }

    public static int getMethodParamCount(final String desc) {
        return getMethodParams(desc).size();
    }

    public static List<String> getMethodParams(final String desc) {
        final var m = ALL_PARAMS_PATTERN.matcher(desc);
        if(!m.find()) {
            throw new IllegalArgumentException("Method signature does not contain parameters");
        }
        final var paramsDescriptor = m.group(1);
        final var mParam = PARAMS_PATTERN.matcher(paramsDescriptor);

        return mParam.results().map(MatchResult::group).collect(Collectors.toList());
    }
}
