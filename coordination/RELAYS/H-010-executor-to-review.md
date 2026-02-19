# [H-010] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-010-api-validation-error-contract.md`
- main relay: `coordination/RELAYS/H-010-main-to-executor.md`
- result: `coordination/REPORTS/H-010-result.md`

## 구현 요약
- 핵심 변경:
  - `@RestControllerAdvice` 기반 전역 오류 처리기 추가 (`ApiExceptionHandler`)
  - 공통 오류 envelope 도입 (`code`, `message`, `path`, `timestamp`, `details[]`)
  - 입력 오류(필수값/enum/JSON 파싱) 400 표준화, 예상치 못한 오류 500 표준화
  - `PARTIAL_SUCCESS` + `chainFailures[]` 필수 확인 규약 문서 반영
  - `MockMvc` 컨트롤러 오류 계약 테스트 추가
- 변경 파일:
  - `src/main/java/me/karubidev/devagent/api/error/ApiErrorDetail.java`
  - `src/main/java/me/karubidev/devagent/api/error/ApiErrorResponse.java`
  - `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java`
  - `src/test/java/me/karubidev/devagent/api/ApiExceptionHandlerTest.java`
  - `docs/code-agent-api.md`
  - `docs/cli-quickstart.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --no-daemon --tests 'me.karubidev.devagent.api.ApiExceptionHandlerTest'`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 두 명령 모두 `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `ApiExceptionHandler`의 400/500 분기와 코드 매핑(`MISSING_REQUIRED_FIELD`, `INVALID_ENUM_VALUE`, `MALFORMED_JSON`, `INTERNAL_SERVER_ERROR`)이 handoff 계약과 일치하는지
2. enum/JSON 오류의 `details.field` 추출 로직이 다양한 payload 구조에서도 안정적인지
3. `docs/code-agent-api.md`와 `docs/cli-quickstart.md` 문서 계약이 구현/테스트와 정합한지 (`PARTIAL_SUCCESS` 시 `chainFailures[]` 필수 확인 포함)

## 알려진 리스크 / 오픈 이슈
- malformed JSON 판별이 예외 클래스명/메시지 기반이라 프레임워크 메시지 변경에 민감할 수 있음
- 복잡한 중첩 JSON path에서 `details.field` 표현의 일관성은 추가 케이스로 확장 검증 가능

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-010-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
