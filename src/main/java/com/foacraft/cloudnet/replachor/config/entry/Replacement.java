package com.foacraft.cloudnet.replachor.config.entry;

import java.util.List;

/**
 * CloudNet-Replachor
 * com.foacraft.cloudnet.replachor.config.entry.Replacement
 *
 * @author scorez
 * @since 7/15/25 15:48.
 */
public record Replacement(String placeholder, Type type, List<String> content) {
    public enum Type {
        SINGLE,
        RANDOM
    }
}