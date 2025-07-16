# CloudNet-Replachor

CloudNet-Replachor is a module for [CloudNet](https://cloudnetservice.eu) that allows you to dynamically replace placeholders in any file within your services. This is particularly useful for templating configuration files with dynamic information like database credentials, service-specific settings, or any other value you want to manage centrally.

## Features

* **Dynamic File Content Replacement**: Replace placeholders in any text-based file before a service starts.
* **Service-Specific Configuration**: Apply different replacement rules based on the service name using regular expressions.
* **Flexible File Matching**: Use glob patterns to target specific files or entire directories for replacement.
* **Multiple Replacement Types**:
    * **Single**: Replace a placeholder with a single, static value.
    * **Random**: Replace a placeholder with a randomly selected value from a list.
* **CloudNet Placeholder Support**: The content used for replacement can itself contain CloudNet's built-in placeholders, which will be resolved automatically.
* **Live Reloading**: Reload your replacement configurations without restarting the CloudNet node using a simple command.
* **Configuration Info**: View all loaded replacement groups and their settings directly from the console.
* **Robust Lifecycle Integration**: Replacements are primarily applied after a service is prepared (`CloudServicePostPrepareEvent`). The module also includes a failsafe to re-apply replacements if a service starts abnormally (`CloudServicePostProcessStartEvent`).

## Installation

1.  Download the latest version of the module from the releases page.
2.  Place the downloaded `.jar` file into the `modules` directory of your CloudNet-Node.
3.  Restart your CloudNet-Node. The module will be loaded automatically.

## Configuration

After the first start, the module will create a directory named `plugins/CloudNet-Replachor` (or a similar path depending on your setup) containing an `example.yml` file. You can create multiple `.yml` files in this directory, and the module will load all of them.

The configuration is based on "Replacer Groups". Each group defines a set of rules for replacements.

Here is a breakdown of the configuration structure from the `example.yml`:

```yaml
# This is the unique ID for the replacer group.
database-replacer:
  # A list of regular expressions. The rules in this group will apply to any service whose name matches one of these patterns.
  service-patterns:
    - '.*' # This example matches all services.
  # A list of glob patterns to find files within the service directory.
  paths-matchers:
    - "*.yml"
    - "*.conf"
    - "*.json"
    - "**/*.yml" # Matches files in subdirectories as well.
    - "**/*.conf"
    - "**/*.json"
  # A map of placeholders to their replacement values.
  replacements:
    # --- SINGLE Replacement ---
    # The placeholder '{database_mysql_host}' will be replaced with '172.24.0.24'.
    '{database_mysql_host}': '172.24.0.24'
    '{database_mysql_port}': '3306'
    '{database_mysql_username}': 'example'
    '{database_mysql_password}': 'password'
    '{database_redis_host}': '172.24.0.24'
    '{database_redis_port}': '6379'
    '{database_redis_password}': 'example'

game-type-replacer:
  service-patterns:
    - 'game-island-.*' # Matches services like 'game-island-1', 'game-island-beta', etc.
  paths-matchers:
    - "*.yml"
    - "*.conf"
    - "*.json"
    - "**/*.yml"
    - "**/*.conf"
    - "**/*.json"
  replacements:
    # --- SINGLE Replacement ---
    '{game_maxplayers}': '16'
    # --- RANDOM Replacement ---
    # The placeholder '{game_special_gadgets}' will be replaced by either 'true' or 'false' randomly.
    '{game_special_gadgets}':
      - 'true'
      - 'false'
    # The placeholder '{game_random_types}' will be replaced by 'solo', 'squad', or 'team' at random.
    '{game_random_types}':
      - 'solo'
      - 'squad'
      - 'team'
```

### Configuration Details:

* **`service-patterns`**: A list of Java-style regular expressions. If a service's name matches any of these patterns, the replacements in this group will be applied.
* **`paths-matchers`**: A list of glob patterns used to locate files. The search is performed within each service's directory. For more information on glob syntax, see the [Java PathMatcher documentation](https://www.google.com/search?q=https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html%23getPathMatcher-java.lang.String-).
* **`replacements`**: This section defines the actual placeholder-to-content mappings.
    * If the value is a single string, it's a **SINGLE** replacement.
    * If the value is a list of strings, it's a **RANDOM** replacement, and one item from the list will be chosen at random.

## Commands

The module provides a set of commands to manage it. The base command alias can be `replacer` or `replachor`.

* `replacer reload`

    * Reloads all configuration files from the module's data directory.
    * Permission: `replachor.command`.

* `replacer info`

    * Displays all loaded replacer groups, including their service patterns, path matchers, and defined replacements. This is useful for debugging your configurations.
    * Permission: `replachor.command`.