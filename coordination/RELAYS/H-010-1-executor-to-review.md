# [H-010-1] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-010-1-error-contract-alignment.md`
- main relay: `coordination/RELAYS/H-010-1-main-to-executor.md`
- result: `coordination/REPORTS/H-010-1-result.md`

## 구현 요약
- 핵심 변경:
  - `ApiExceptionHandler`에서 복합 필수조건(any-of) 누락을 `MISSING_REQUIRED_ANY_OF`로 분리
  - any-of 오류의 `details[]`를 필드별(`reason=any_of_required`)로 구조화
  - `"userRequest or readable specInputPath is required"` 메시지에서도 `details.field=specInputPath` 정규화
  - API 문서 400 매핑에 `INVALID_JSON_REQUEST`, `MISSING_REQUIRED_ANY_OF` 반영
- 변경 파일:
  - `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java`
  - `src/test/java/me/karubidev/devagent/api/ApiExceptionHandlerTest.java`
  - `docs/code-agent-api.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --no-daemon --tests 'me.karubidev.devagent.api.ApiExceptionHandlerTest'`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 두 명령 모두 `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 없음

## 리뷰 집중 포인트
1. `INVALID_JSON_REQUEST`가 구현 fallback(`ApiExceptionHandler.java:84`)과 문서 매핑(`docs/code-agent-api.md:124`)에 모두 반영됐는지
2. any-of 분기(`ApiExceptionHandler.java:124`, `ApiExceptionHandler.java:151`)가 단일 필수 누락 계약과 충돌 없이 동작하는지
3. 신규 회귀 테스트 2건(`ApiExceptionHandlerTest.java:103`, `ApiExceptionHandlerTest.java:120`)이 실제 계약을 충분히 고정하는지

## 알려진 리스크 / 오픈 이슈
- any-of 분류가 예외 메시지 패턴에 의존하므로 메시지 문구 변경 시 회귀 여지가 있음
- 필드명 정규화 규칙은 토큰 추출 기반이라 표현 형식이 달라질 경우 추가 보강 필요

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-010-1-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
