# H-010 결과 보고서 (API 입력검증/오류 응답 계약 표준화)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `src/main/java/me/karubidev/devagent/api/error/ApiErrorDetail.java`
- `src/main/java/me/karubidev/devagent/api/error/ApiErrorResponse.java`
- `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java`
- `src/test/java/me/karubidev/devagent/api/ApiExceptionHandlerTest.java`
- `docs/code-agent-api.md`
- `docs/cli-quickstart.md`

## 구현 요약
- 전역 예외 처리기(`ApiExceptionHandler`)를 추가해 Routing + Agent API의 오류 응답을 공통 envelope로 통일했습니다.
  - 공통 필드: `code`, `message`, `path`, `timestamp`, `details[]`
- `IllegalArgumentException`을 400으로 매핑하고, `"... is required"` 패턴은 `MISSING_REQUIRED_FIELD` 코드 + 필드 상세로 정규화했습니다.
- `HttpMessageNotReadableException`을 세분화해 다음 계약으로 매핑했습니다.
  - 요청 본문 누락: `REQUEST_BODY_REQUIRED` (400)
  - malformed JSON: `MALFORMED_JSON` (400)
  - enum 역직렬화 실패: `INVALID_ENUM_VALUE` (400)
  - 기타 JSON 값/타입 문제: `INVALID_JSON_VALUE`/`INVALID_JSON_REQUEST` (400)
- 예상치 못한 예외는 `INTERNAL_SERVER_ERROR` (500)로 통일하고, 응답 본문에 내부 스택트레이스를 노출하지 않도록 고정했습니다.
- `MockMvc` 기반 컨트롤러 계약 테스트를 추가해 필수 입력 누락/enum 오류/JSON 파싱 오류/서버 오류 시나리오를 회귀 고정했습니다.
- 문서(`docs/code-agent-api.md`)에 공통 오류 envelope와 상태코드 매핑, 케이스별 예시(4종), `PARTIAL_SUCCESS`에서 `chainFailures[]` 필수 확인 규약을 반영했습니다.
- CLI 문서(`docs/cli-quickstart.md`)에도 `PARTIAL_SUCCESS` 사용 시 `chainFailures[]` 확인 주의사항을 반영했습니다.

## 오류 케이스별 응답 예시

### 1) 필수 입력 누락/공백 (400)
```json
{
  "code": "MISSING_REQUIRED_FIELD",
  "message": "userRequest is required",
  "path": "/api/agents/spec/generate",
  "timestamp": "2026-02-19T02:40:56.123Z",
  "details": [
    {
      "field": "userRequest",
      "reason": "required",
      "rejectedValue": null
    }
  ]
}
```

### 2) enum 오류 (400)
```json
{
  "code": "INVALID_ENUM_VALUE",
  "message": "Invalid enum value",
  "path": "/api/agents/code/generate",
  "timestamp": "2026-02-19T02:41:00.101Z",
  "details": [
    {
      "field": "mode",
      "reason": "must be one of [COST_SAVER, BALANCED, QUALITY, GEMINI3_CANARY]",
      "rejectedValue": "NOT_A_MODE"
    }
  ]
}
```

### 3) JSON 파싱 오류 (400)
```json
{
  "code": "MALFORMED_JSON",
  "message": "Malformed JSON request body",
  "path": "/api/agents/review/generate",
  "timestamp": "2026-02-19T02:41:03.550Z",
  "details": []
}
```

### 4) 서버 오류 (500)
```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "Internal server error",
  "path": "/api/agents/review/generate",
  "timestamp": "2026-02-19T02:41:10.777Z",
  "details": []
}
```

## 수용기준 점검
1. 필수 입력 누락/공백 요청 400 + 공통 envelope: **충족**
2. enum 역직렬화 실패/JSON 파싱 실패 공통 envelope: **충족**
3. 예상치 못한 서버 오류 500 + 내부 스택트레이스 미노출: **충족**
4. 기존 성공 응답 계약(`runId`, payload, `chainFailures[]`) 유지: **충족**
5. `docs/code-agent-api.md` 오류 응답 스키마 + `chainFailures[]` 필수 확인 규약 반영: **충족**
6. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 신규 오류 계약 테스트:
  - 명령: `./gradlew test --no-daemon --tests 'me.karubidev.devagent.api.ApiExceptionHandlerTest'`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- malformed JSON 분류는 루트 예외 클래스명/메시지 패턴 기반이므로, 프레임워크 메시지 포맷 변경 시 코드 분류 회귀 가능성이 있습니다.
- `details.field` 추출은 path reference 문자열 기반이므로 매우 복잡한 중첩 구조에서 표현 일관성 검증이 추가로 필요할 수 있습니다.

## 승인 필요 항목
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경: **없음**
- 사전 승인 필요 항목: **해당 없음**
