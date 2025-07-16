# CloudNet-Replachor

CloudNet-Replachor is a module for [CloudNet](https://cloudnetservice.eu) that allows you to dynamically replace placeholders in any file within your services. This is particularly useful for templating configuration files with dynamic information like database credentials, service-specific settings, or any other value you want to manage centrally.

## Features ✨

* **Dynamic File Content Replacement**: Replace placeholders in any text-based file before a service starts.
* **Service-Specific Configuration**: Apply different replacement rules based on the service name using regular expressions.
* **Flexible File Matching**: Use glob patterns to target specific files or entire directories for replacement.
* **Multiple Replacement Types**:
  * **Single**: Replace a placeholder with a single, static value.
  * **Random**: Replace a placeholder with a randomly selected value from a list.
* **CloudNet Placeholder Support**: The content used for replacement can itself contain CloudNet's built-in placeholders, which will be resolved automatically before replacement.
* **Live Reloading**: Reload your replacement configurations without restarting the CloudNet node using a simple command.
* **Configuration Info**: View all loaded replacement groups and their settings directly from the console.
* **Robust Lifecycle Integration**: Replacements are primarily applied after a service is prepared (`CloudServicePostPrepareEvent`). The module also includes a failsafe to re-apply replacements if a service starts abnormally (`CloudServicePostProcessStartEvent`).

-----

## Configuration ⚙️

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

# This group demonstrates using CloudNet's built-in placeholders.
dynamic-motd-replacer:
  service-patterns:
    - 'Lobby-.*' # Matches all lobby services.
  paths-matchers:
    - "server.properties" # For Minecraft servers
    - "config.yml"      # For BungeeCord/Velocity proxies
  replacements:
    # The value for 'motd' will be processed by CloudNet's BridgeServiceHelper,
    # replacing variables like %name%, %port%, etc., with actual service data.
    '{server_motd}': 'Welcome to %name%! We are running on port %port%.'
    '{server_state_message}': 'Current server state is %state% with %online_players%/%max_players% players.'
```

### Configuration Details:

* **`service-patterns`**: A list of Java-style regular expressions. If a service's name matches any of these patterns, the replacements in this group will be applied.
* **`paths-matchers`**: A list of glob patterns used to locate files. The search is performed within each service's directory. For more information on glob syntax, see the [Java PathMatcher documentation](https://www.google.com/search?q=https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html%23getPathMatcher-java.lang.String-).
* **`replacements`**: This section defines the actual placeholder-to-content mappings.
  * If the value is a single string, it's a **SINGLE** replacement.
  * If the value is a list of strings, it's a **RANDOM** replacement, and one item from the list will be chosen at random.

### Using CloudNet Placeholders

A powerful feature of this module is its ability to use CloudNet's own placeholders within your replacement content. The replacement logic is handled by `BridgeServiceHelper.fillCommonPlaceholders`, which means you have access to a wide range of dynamic variables related to the service.

When you define a replacement, the `content` value is first passed through CloudNet's placeholder system. The result of that is then used to replace your custom placeholder in the target file.

**Available Placeholders include:**

* `%name%`: The name of the service (e.g., `Lobby-1`).
* `%task%`: The name of the service's task.
* `%node%`: The unique ID of the node the service is running on.
* `%unique_id%`: The full unique ID of the service.
* `%uid%`: The short unique ID of the service.
* `%port%`: The port of the service.
* `%online_players%`: The current number of online players.
* `%max_players%`: The maximum number of players.
* `%motd%`: The MOTD of the service.
* `%state%`: The current state of the service (e.g., LOBBY, INGAME).
* ...and many more from [BridgeServiceHelper](https://github.com/CloudNetService/CloudNet/blob/nightly/modules/bridge/api/src/main/java/eu/cloudnetservice/modules/bridge/BridgeServiceHelper.java).

This allows for highly dynamic configurations managed centrally.

-----

## Commands 💻

The module provides a set of commands to manage it. The base command alias can be **`replacer`** or **`replachor`**.

* `replacer reload`

  * Reloads all configuration files from the module's data directory.
  * **Permission**: `replachor.command`.

* `replacer info`

  * Displays all loaded replacer groups, including their service patterns, path matchers, and defined replacements. This is useful for debugging your configurations.
  * **Permission**: `replachor.command`.