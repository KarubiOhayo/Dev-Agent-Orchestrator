# H-010 리뷰 보고서

## 대상
- handoff: `coordination/HANDOFFS/H-010-api-validation-error-contract.md`
- result: `coordination/REPORTS/H-010-result.md`
- relay: `coordination/RELAYS/H-010-executor-to-review.md`

## Findings (P1 > P2 > P3)
### [P2] 문서화된 400 오류 코드 목록이 구현과 불일치
- 근거:
  - 구현은 `INVALID_JSON_REQUEST`를 실제 반환함.
    - `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java:82`
    - `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java:84`
  - API 문서의 400 매핑 목록에는 `INVALID_JSON_REQUEST`가 누락됨.
    - `docs/code-agent-api.md:123`
- 영향:
  - 문서 기반으로 오류 코드를 분기하는 클라이언트에서 미정의 코드가 발생할 수 있음.
  - handoff 수용기준 5번(오류 응답 스키마/매핑 문서화)의 정합성이 부분적으로 깨짐.
- 권고:
  - `docs/code-agent-api.md`의 400 매핑 목록에 `INVALID_JSON_REQUEST`를 추가.
  - 가능하면 해당 분기 회귀 테스트 1건을 추가해 문서/구현 동기화를 고정.

### [P3] 복합 필수조건 오류에서 `details.field`가 실제 필드명이 아닌 문구로 노출
- 근거:
  - Code 요청의 필수조건 누락 시 서비스는 `"userRequest or specInputPath is required"` 메시지를 던짐.
    - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java:76`
    - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java:103`
  - 예외 핸들러는 `"(.+) is required"` 패턴 매칭 결과 전체를 `details.field`에 그대로 기록함.
    - `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java:21`
    - `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java:130`
    - `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java:135`
- 영향:
  - `details.field`를 단일 필드 식별자로 사용하는 클라이언트에서 파싱 일관성이 떨어질 수 있음.
- 권고:
  - 복합 조건은 별도 코드(예: `MISSING_REQUIRED_ANY_OF`) + 다중 details 구조로 분리하거나,
  - 단일 필드 계약 유지가 필요하면 메시지/매핑 규칙을 명시적으로 제한.

## 심각도 집계
- P1: 0
- P2: 1
- P3: 1

## 수용기준 검증
1. 필수 입력 누락/공백 400 + 공통 envelope: **충족**
2. enum 역직렬화 실패/JSON 파싱 실패 공통 envelope: **충족**
3. 예상치 못한 서버 오류 500 + 내부 스택트레이스 미노출: **충족**
4. 기존 성공 응답 계약(`runId`, payload, `chainFailures[]`) 유지: **충족**
5. 문서 오류 응답 매핑 정합성: **부분 충족** (`INVALID_JSON_REQUEST` 누락)
6. `./gradlew clean test --no-daemon` 통과: **충족** (Executor 보고 인용)

## 승인 게이트 체크
- 테스트 게이트 상태:
  - `./gradlew test --no-daemon --tests 'me.karubidev.devagent.api.ApiExceptionHandlerTest'`: 통과 (Executor 보고)
  - `./gradlew clean test --no-daemon`: 통과, `BUILD SUCCESSFUL` (Executor 보고)
- 공통 파일 변경 승인 절차 준수 여부:
  - `src/main/resources/application.yml` 변경 없음
  - 공용 모델/빌드 설정 변경 없음
  - 사전 승인 절차 위반 없음

## 리뷰 결론
- 리스크 수준: `MEDIUM`
- 최종 권고: `Conditional Go`
  - 조건: API 문서의 오류 코드 매핑(`INVALID_JSON_REQUEST` 누락) 정합화 후 Main 승인 진행 권고
