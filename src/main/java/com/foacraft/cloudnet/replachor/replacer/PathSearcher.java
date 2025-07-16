package com.foacraft.cloudnet.replachor.replacer;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * CloudNet-Replachor
 * com.foacraft.cloudnet.replachor.replacer.PathSearcher
 *
 * @author scorez
 * @since 7/16/25 14:35.
 */
@Singleton
public class PathSearcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathSearcher.class);

    public List<Path> search(Path basePath, List<String> pathRules) {
        Set<Path> filteredPaths = new HashSet<>();
        for (String pathPattern : pathRules) {
            if (pathPattern.contains("*") || pathPattern.contains("?")) {
                PathMatcher matcher;
                try {
                    matcher = FileSystems.getDefault().getPathMatcher("glob:" + pathPattern);
                } catch (IllegalArgumentException | UnsupportedOperationException e) {
                    LOGGER.warn("Skipped! Invalid glob pattern: {}", pathPattern);
                    continue;
                }

                try (Stream<Path> walkedPaths = Files.walk(basePath)) {
                    walkedPaths
                            .filter(p -> {
                                Path relativePath = basePath.relativize(p);
                                return matcher.matches(relativePath);
                            })
                            .forEach(path -> filteredPaths.addAll(deepSearch(path)));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidPathException ipe) {
                    LOGGER.warn("Skipped! An invalid path during glob: " + basePath + " / " + pathPattern + " - " + ipe.getMessage());
                }
            } else {
                Path resolvedPath = basePath.resolve(pathPattern);
                filteredPaths.addAll(deepSearch(resolvedPath));
            }
        }

        return new ArrayList<>(filteredPaths);
    }

    private List<Path> deepSearch(Path basePath) {
        List<Path> result = new ArrayList<>();
        if (!Files.exists(basePath)) {
            return result;
        }
        try (Stream<Path> paths = Files.walk(basePath)) {
            paths.filter(Files::isRegularFile).forEach(result::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
