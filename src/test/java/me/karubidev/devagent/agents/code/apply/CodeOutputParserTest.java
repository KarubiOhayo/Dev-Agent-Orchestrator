package me.karubidev.devagent.agents.code.apply;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CodeOutputParserTest {

  private final CodeOutputParser parser = new CodeOutputParser();

  @Test
  void parseFilesExtractsHeadingAndCodeBlock() {
    String output = """
        ## FILES
        ### `src/main/java/com/example/AuthController.java`
        ```java
        class AuthController {}
        ```
        """;

    List<GeneratedFile> files = parser.parseFiles(output);

    assertThat(files).hasSize(1);
    assertThat(files.get(0).path()).isEqualTo("src/main/java/com/example/AuthController.java");
    assertThat(files.get(0).content()).contains("class AuthController");
  }
}
