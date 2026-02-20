package me.karubidev.devagent.agents.code.apply;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class CodeOutputParserTest {

  private final CodeOutputParser parser = new CodeOutputParser(new ObjectMapper());

  @Test
  void parseFilesExtractsFromDirectJsonSchema() {
    String output = """
        {
          "files": [
            {
              "path": "src/main/java/com/example/AuthController.java",
              "content": "class AuthController {}"
            }
          ]
        }
        """;

    CodeOutputParser.ParseResult result = parser.parse(output);
    List<GeneratedFile> files = result.files();

    assertThat(files).hasSize(1);
    assertThat(files.get(0).path()).isEqualTo("src/main/java/com/example/AuthController.java");
    assertThat(files.get(0).content()).contains("class AuthController");
    assertThat(result.source()).isEqualTo(CodeOutputParser.ParseSource.JSON);
  }

  @Test
  void parseFilesExtractsFromJsonCodeBlock() {
    String output = """
        설명 텍스트
        ```json
        {
          "files": [
            {
              "path": "src/main/java/com/example/UserService.java",
              "content": "class UserService {}"
            }
          ]
        }
        ```
        """;

    CodeOutputParser.ParseResult result = parser.parse(output);
    List<GeneratedFile> files = result.files();

    assertThat(files).hasSize(1);
    assertThat(files.get(0).path()).isEqualTo("src/main/java/com/example/UserService.java");
    assertThat(files.get(0).content()).contains("class UserService");
    assertThat(result.source()).isEqualTo(CodeOutputParser.ParseSource.JSON_CODE_BLOCK);
  }

  @Test
  void parseFilesFallsBackToMarkdownWhenJsonParseFails() {
    String output = """
        { this-is-not-json }
        ## FILES
        ### `src/main/java/com/example/AuthController.java`
        ```java
        class AuthController {}
        ```
        """;

    CodeOutputParser.ParseResult result = parser.parse(output);
    List<GeneratedFile> files = result.files();

    assertThat(files).hasSize(1);
    assertThat(files.get(0).path()).isEqualTo("src/main/java/com/example/AuthController.java");
    assertThat(files.get(0).content()).contains("class AuthController");
    assertThat(result.usedMarkdownFallback()).isTrue();
    assertThat(result.source()).isEqualTo(CodeOutputParser.ParseSource.MARKDOWN_FALLBACK);
  }

  @Test
  void parseFilesExtractsFromNestedResultFiles() {
    String output = """
        {
          "result": {
            "files": [
              {
                "path": "src/main/java/com/example/Nested.java",
                "content": "class Nested {}"
              }
            ]
          }
        }
        """;

    CodeOutputParser.ParseResult result = parser.parse(output);

    assertThat(result.files()).hasSize(1);
    assertThat(result.files().get(0).path()).isEqualTo("src/main/java/com/example/Nested.java");
    assertThat(result.source()).isEqualTo(CodeOutputParser.ParseSource.JSON);
  }

  @Test
  void parseFilesExtractsEmbeddedJsonPayload() {
    String output = """
        모델 응답 요약입니다.
        {"files":[{"path":"src/main/java/com/example/Embedded.java","content":"class Embedded {}"}]}
        참고: 위 파일을 반영하세요.
        """;

    CodeOutputParser.ParseResult result = parser.parse(output);

    assertThat(result.files()).hasSize(1);
    assertThat(result.files().get(0).path()).isEqualTo("src/main/java/com/example/Embedded.java");
    assertThat(result.source()).isEqualTo(CodeOutputParser.ParseSource.JSON_EMBEDDED);
  }

  @Test
  void parseFilesExtractsFromGenericCodeFence() {
    String output = """
        결과:
        ```
        {"files":[{"path":"src/main/java/com/example/Fenced.java","content":"class Fenced {}"}]}
        ```
        """;

    CodeOutputParser.ParseResult result = parser.parse(output);

    assertThat(result.files()).hasSize(1);
    assertThat(result.files().get(0).path()).isEqualTo("src/main/java/com/example/Fenced.java");
    assertThat(result.source()).isEqualTo(CodeOutputParser.ParseSource.JSON_GENERIC_CODE_BLOCK);
  }

  @Test
  void parseFilesExtractsFromTruncatedJsonByLooseFallback() {
    String output = """
        ```json
        {
          "files": [
            {
              "path": "pyproject.toml",
              "content": "[project]\\nname = \\"focusbar\\""
            },
            {
              "path": "focusbar/__init__.py",
              "content": "__version__ = \\"0.1.0\\""
            }
        """;

    CodeOutputParser.ParseResult result = parser.parse(output);

    assertThat(result.files()).hasSize(2);
    assertThat(result.files().get(0).path()).isEqualTo("pyproject.toml");
    assertThat(result.source()).isEqualTo(CodeOutputParser.ParseSource.LOOSE_JSON_FALLBACK);
  }
}
