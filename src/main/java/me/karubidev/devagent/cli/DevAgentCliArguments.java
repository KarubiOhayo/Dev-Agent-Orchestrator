package me.karubidev.devagent.cli;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class DevAgentCliArguments {

  private static final Map<String, String> LONG_OPTION_ALIASES = new HashMap<>();
  private static final Map<String, String> SHORT_OPTION_ALIASES = new HashMap<>();
  private static final Set<String> DASH_PREFIX_VALUE_ALLOWED_LONG_KEYS = Set.of(
      "project-id",
      "target-root",
      "user-request",
      "mode",
      "risk-level",
      "spec-input-path",
      "code-user-request",
      "spec-output-path"
  );
  private static final Set<String> NORMALIZED_EQ_ENUM_KEYS = Set.of(
      "mode",
      "risk-level"
  );
  private static final Set<String> NORMALIZED_EQ_BOOLEAN_KEYS = Set.of(
      "json",
      "apply",
      "chain-to-code",
      "large-context",
      "strict-json-required",
      "overwrite-existing",
      "code-apply",
      "code-overwrite-existing"
  );

  static {
    LONG_OPTION_ALIASES.put("project", "project-id");
    LONG_OPTION_ALIASES.put("root", "target-root");
    LONG_OPTION_ALIASES.put("request", "user-request");
    LONG_OPTION_ALIASES.put("risk", "risk-level");

    SHORT_OPTION_ALIASES.put("p", "project-id");
    SHORT_OPTION_ALIASES.put("r", "target-root");
    SHORT_OPTION_ALIASES.put("u", "user-request");
    SHORT_OPTION_ALIASES.put("m", "mode");
    SHORT_OPTION_ALIASES.put("k", "risk-level");
    SHORT_OPTION_ALIASES.put("j", "json");
    SHORT_OPTION_ALIASES.put("a", "apply");
    SHORT_OPTION_ALIASES.put("c", "chain-to-code");
  }

  private final boolean cliMode;
  private final String command;
  private final Map<String, String> options;

  private DevAgentCliArguments(boolean cliMode, String command, Map<String, String> options) {
    this.cliMode = cliMode;
    this.command = command;
    this.options = options;
  }

  static DevAgentCliArguments parse(String[] rawArgs) {
    if (rawArgs == null || rawArgs.length == 0) {
      return new DevAgentCliArguments(false, "", Map.of());
    }

    int index = 0;
    String first = normalize(rawArgs[0]);
    if ("devagent".equals(first)) {
      index++;
      if (index >= rawArgs.length) {
        return new DevAgentCliArguments(true, "help", Map.of());
      }
    }

    if (index >= rawArgs.length) {
      return new DevAgentCliArguments(false, "", Map.of());
    }

    String command = normalize(rawArgs[index]);
    if (!"generate".equals(command) && !"spec".equals(command) && !"help".equals(command)) {
      if ("devagent".equals(first)) {
        throw new CliCommandException("지원하지 않는 명령어입니다: " + rawArgs[index], 2);
      }
      return new DevAgentCliArguments(false, "", Map.of());
    }

    Map<String, String> options = parseOptions(rawArgs, index + 1);
    if (options.containsKey("help")) {
      return new DevAgentCliArguments(true, "help", Map.of());
    }
    return new DevAgentCliArguments(true, command, options);
  }

  private static Map<String, String> parseOptions(String[] rawArgs, int startIndex) {
    Map<String, String> options = new LinkedHashMap<>();
    for (int i = startIndex; i < rawArgs.length; i++) {
      String token = rawArgs[i];
      if ("--help".equals(token) || "-h".equals(token)) {
        options.put("help", "true");
        continue;
      }

      boolean longOption = token.startsWith("--");
      boolean shortOption = token.startsWith("-") && !longOption;
      if (!longOption && !shortOption) {
        throw new CliCommandException(
            "옵션 형식이 올바르지 않습니다: " + token + " (예: --user-request=\"...\")",
            2
        );
      }

      String body = longOption ? token.substring(2) : token.substring(1);
      if (body.isBlank()) {
        throw new CliCommandException("빈 옵션 키는 사용할 수 없습니다.", 2);
      }

      int split = body.indexOf('=');
      String key;
      String value;
      if (split >= 0) {
        key = normalizeKey(body.substring(0, split), shortOption);
        value = body.substring(split + 1);
      } else {
        key = normalizeKey(body, shortOption);
        if (shouldConsumeSeparatedValue(rawArgs, i, longOption, key)) {
          value = rawArgs[++i];
        } else {
          value = "true";
        }
      }
      putOption(options, key, value);
    }
    return options;
  }

  private static boolean shouldConsumeSeparatedValue(
      String[] rawArgs,
      int currentIndex,
      boolean longOption,
      String key
  ) {
    if (currentIndex + 1 >= rawArgs.length) {
      return false;
    }

    String nextToken = rawArgs[currentIndex + 1];
    if (!nextToken.startsWith("-")) {
      return true;
    }
    if (!longOption) {
      return false;
    }
    if (nextToken.startsWith("--")) {
      return false;
    }
    return DASH_PREFIX_VALUE_ALLOWED_LONG_KEYS.contains(key);
  }

  private static String normalizeKey(String rawKey, boolean shortOption) {
    String key = normalize(rawKey);
    if (key.isBlank()) {
      throw new CliCommandException("빈 옵션 키는 사용할 수 없습니다.", 2);
    }

    if (shortOption && SHORT_OPTION_ALIASES.containsKey(key)) {
      return SHORT_OPTION_ALIASES.get(key);
    }
    if (!shortOption && LONG_OPTION_ALIASES.containsKey(key)) {
      return LONG_OPTION_ALIASES.get(key);
    }
    return key;
  }

  private static void putOption(Map<String, String> options, String key, String value) {
    String existing = options.get(key);
    if (existing != null && !isEquivalentValue(key, existing, value)) {
      throw new CliCommandException(
          "옵션 값이 충돌합니다: --" + key + " (기존=" + existing + ", 신규=" + value + ")",
          2
      );
    }
    if (existing == null) {
      options.put(key, value);
    }
  }

  private static boolean isEquivalentValue(String key, String existing, String incoming) {
    if (existing.equals(incoming)) {
      return true;
    }

    if (NORMALIZED_EQ_ENUM_KEYS.contains(key)) {
      return normalize(existing).equals(normalize(incoming));
    }

    if (NORMALIZED_EQ_BOOLEAN_KEYS.contains(key)) {
      Boolean existingBool = parseBoolean(existing);
      Boolean incomingBool = parseBoolean(incoming);
      if (existingBool != null && incomingBool != null) {
        return existingBool.equals(incomingBool);
      }
      return normalize(existing).equals(normalize(incoming));
    }
    return false;
  }

  private static Boolean parseBoolean(String value) {
    String normalized = normalize(value);
    if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)) {
      return true;
    }
    if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
      return false;
    }
    return null;
  }

  private static String normalize(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  boolean cliMode() {
    return cliMode;
  }

  String command() {
    return command;
  }

  boolean hasOption(String key) {
    return options.containsKey(normalize(key));
  }

  String option(String key) {
    return options.get(normalize(key));
  }

  String optionOrDefault(String key, String defaultValue) {
    return options.getOrDefault(normalize(key), defaultValue);
  }

  boolean optionAsBoolean(String key, boolean defaultValue) {
    String raw = option(key);
    if (raw == null) {
      return defaultValue;
    }
    String normalized = normalize(raw);
    if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)) {
      return true;
    }
    if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized)) {
      return false;
    }
    throw new CliCommandException("불리언 옵션 값이 올바르지 않습니다: --" + key + "=" + raw, 2);
  }

  <E extends Enum<E>> E optionAsEnum(String key, Class<E> enumType, E defaultValue) {
    String raw = option(key);
    if (raw == null || raw.isBlank()) {
      return defaultValue;
    }
    String candidate = raw.trim().toUpperCase(Locale.ROOT);
    try {
      return Enum.valueOf(enumType, candidate);
    } catch (IllegalArgumentException e) {
      String available = String.join(", ", enumNames(enumType));
      throw new CliCommandException(
          "열거형 옵션 값이 올바르지 않습니다: --" + key + "=" + raw + " (허용값: " + available + ")",
          2
      );
    }
  }

  void assertOnly(Set<String> allowedKeys) {
    Set<String> normalizedAllowed = allowedKeys.stream()
        .map(DevAgentCliArguments::normalize)
        .collect(Collectors.toSet());
    for (String key : options.keySet()) {
      if (!normalizedAllowed.contains(key)) {
        throw new CliCommandException("지원하지 않는 옵션입니다: --" + key, 2);
      }
    }
  }

  private static <E extends Enum<E>> String[] enumNames(Class<E> enumType) {
    E[] constants = enumType.getEnumConstants();
    String[] names = new String[constants.length];
    for (int i = 0; i < constants.length; i++) {
      names[i] = constants[i].name();
    }
    return names;
  }
}
