# H-010.1 결과 보고서 (오류 계약 정합성 보강)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java`
- `src/test/java/me/karubidev/devagent/api/ApiExceptionHandlerTest.java`
- `docs/code-agent-api.md`

## 구현 요약
- `IllegalArgumentException`의 `"(... ) is required"` 패턴에서 단일 필수 누락과 복합 필수(any-of) 누락을 분리했습니다.
  - 복합 필수: `MISSING_REQUIRED_ANY_OF`
  - 단일 필수: `MISSING_REQUIRED_FIELD`(기존 유지)
- any-of 오류의 `details[]`는 후보 필드별 항목으로 생성하고 `reason=any_of_required`를 고정했습니다.
- `"userRequest or readable specInputPath is required"`처럼 수식어가 포함된 메시지에서도 `details.field`를 실제 필드명(`specInputPath`)으로 정규화했습니다.
- API 문서의 400 매핑 표를 구현과 동기화했습니다.
  - `INVALID_JSON_REQUEST` 포함
  - `MISSING_REQUIRED_ANY_OF` 포함

## 오류 코드 매핑 동기화 근거 (문서/구현 대조)
- 구현:
  - `INVALID_JSON_REQUEST` 반환 fallback 유지: `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java:84`
  - any-of 분기 코드 추가: `src/main/java/me/karubidev/devagent/api/error/ApiExceptionHandler.java:124`
- 문서:
  - 400 오류 코드 매핑에 `INVALID_JSON_REQUEST`, `MISSING_REQUIRED_ANY_OF` 반영:
    - `docs/code-agent-api.md:124`
  - any-of `details[]` 계약 명시:
    - `docs/code-agent-api.md:120`

## 복합 필수조건 오류 응답 예시 + 테스트 결과
- 계약 예시:
```json
{
  "code": "MISSING_REQUIRED_ANY_OF",
  "message": "userRequest or specInputPath is required",
  "path": "/api/agents/code/generate",
  "details": [
    {"field": "userRequest", "reason": "any_of_required", "rejectedValue": null},
    {"field": "specInputPath", "reason": "any_of_required", "rejectedValue": null}
  ]
}
```
- 회귀 테스트:
  - `returns400EnvelopeForMissingAnyOfRequiredFields`:
    - `src/test/java/me/karubidev/devagent/api/ApiExceptionHandlerTest.java:103`
  - `returns400EnvelopeForMissingAnyOfRequiredFieldsWithReadableQualifier`:
    - `src/test/java/me/karubidev/devagent/api/ApiExceptionHandlerTest.java:120`

## 수용기준 점검
1. 문서 400 매핑과 구현 코드 집합 일치(`INVALID_JSON_REQUEST` 포함): **충족**
2. 복합 필수조건 누락 시 `MISSING_REQUIRED_ANY_OF` + 다중 `details[]`: **충족**
3. 단일 필수 누락 `MISSING_REQUIRED_FIELD` 유지: **충족**
4. 성공 응답 계약(runId, payload, `chainFailures[]`) 회귀 없음: **충족**
5. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 오류 계약 테스트:
  - 명령: `./gradlew test --no-daemon --tests 'me.karubidev.devagent.api.ApiExceptionHandlerTest'`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- any-of 분류는 예외 메시지 문자열(`" or "`, `" is required"`) 패턴에 의존하므로 메시지 포맷 변경 시 분류 회귀 가능성이 있습니다.
- 필드명 정규화는 토큰 추출 규칙 기반이어서, 향후 메시지 표현이 크게 달라지면 추가 규칙 보강이 필요할 수 있습니다.

## 승인 필요 항목
- 공통 파일(`src/main/resources/application.yml`, 공용 모델, 빌드 설정) 변경: **없음**
- 사전 승인 필요 항목: **해당 없음**
