package com.foacraft.cloudnet.replachor.config.entry;

import java.util.List;

/**
 * CloudNet-Replachor
 * com.foacraft.cloudnet.replachor.config.entry.ReplacerGroup
 *
 * @author scorez
 * @since 7/15/25 15:49.
 */
public record ReplacerGroup(
    String id,
    List<String> servicePatterns,
    List<String> pathsMatchers,
    List<Replacement> replacements
) {

}
