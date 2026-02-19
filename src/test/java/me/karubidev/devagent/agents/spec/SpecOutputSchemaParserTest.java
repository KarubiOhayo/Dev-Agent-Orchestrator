package me.karubidev.devagent.agents.spec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class SpecOutputSchemaParserTest {

  private final SpecOutputSchemaParser parser = new SpecOutputSchemaParser(new ObjectMapper());

  @Test
  void parseReturnsDirectJsonSourceWhenRawTextIsJsonObject() {
    SpecOutputSchemaParser.ParseResult result = parser.parse(
        "{\"title\":\"Login\",\"overview\":\"Auth\",\"tasks\":[]}",
        "요청"
    );

    assertThat(result.source()).isEqualTo(SpecOutputSchemaParser.ParseSource.DIRECT_JSON);
    assertThat(result.spec().path("title").asText()).isEqualTo("Login");
  }

  @Test
  void parseReturnsJsonCodeBlockSourceWhenJsonCodeBlockExists() {
    SpecOutputSchemaParser.ParseResult result = parser.parse(
        """
            설명 텍스트
            ```json
            {"title":"Block","overview":"Auth","tasks":[]}
            ```
            """,
        "요청"
    );

    assertThat(result.source()).isEqualTo(SpecOutputSchemaParser.ParseSource.JSON_CODE_BLOCK);
    assertThat(result.spec().path("title").asText()).isEqualTo("Block");
  }

  @Test
  void parseReturnsFallbackSchemaSourceWhenJsonParsingFails() {
    SpecOutputSchemaParser.ParseResult result = parser.parse(
        "일반 텍스트 출력",
        "로그인 API 명세 작성"
    );

    assertThat(result.source()).isEqualTo(SpecOutputSchemaParser.ParseSource.FALLBACK_SCHEMA);
    assertThat(result.spec().path("title").asText()).isEqualTo("로그인 API 명세 작성");
    assertThat(result.spec().path("notes").isArray()).isTrue();
  }
}
