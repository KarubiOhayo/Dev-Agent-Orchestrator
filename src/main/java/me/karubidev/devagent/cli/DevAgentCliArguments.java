package me.karubidev.devagent.cli;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

final class DevAgentCliArguments {

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
      if (!token.startsWith("--")) {
        throw new CliCommandException(
            "옵션 형식이 올바르지 않습니다: " + token + " (예: --user-request=\"...\")",
            2
        );
      }

      String body = token.substring(2);
      if (body.isBlank()) {
        throw new CliCommandException("빈 옵션 키는 사용할 수 없습니다.", 2);
      }

      int split = body.indexOf('=');
      String key;
      String value;
      if (split >= 0) {
        key = normalize(body.substring(0, split));
        value = body.substring(split + 1);
      } else {
        key = normalize(body);
        if (i + 1 < rawArgs.length && !rawArgs[i + 1].startsWith("--")) {
          value = rawArgs[++i];
        } else {
          value = "true";
        }
      }
      options.put(key, value);
    }
    return options;
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
