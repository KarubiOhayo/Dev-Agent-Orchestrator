# H-009 결과 보고서 (체인 실패 전파 정책/API 계약 확정)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateResponse.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
- `docs/code-agent-api.md`
- `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`

## 구현 요약
- `CodeGenerateRequest`에 `chainFailurePolicy`를 추가했습니다.
  - 기본값: `FAIL_FAST`
  - 선택값: `PARTIAL_SUCCESS`
- `CodeGenerateResponse`에 `chainFailures`를 추가했습니다.
  - 구조: `agent`, `failedStage`, `errorMessage`
  - 하위 호환을 위해 기존 시그니처 보조 생성자를 유지했습니다.
- `CodeAgentService`에 정책 분기를 추가했습니다.
  - `FAIL_FAST`: 체인 예외를 즉시 상위 전파(기존 동작 유지)
  - `PARTIAL_SUCCESS`: 체인 예외를 `chainFailures`에 누적하고 Code 응답은 성공 반환
- run-state 이벤트 계약(`CHAIN_*_TRIGGERED/DONE/FAILED`)은 체인 서비스 로직 그대로 유지되어 기존 관측성 계약을 보존했습니다.
- `docs/code-agent-api.md`에 정책 필드/응답 필드 및 정책 동작을 문서화했습니다.

## 정책 모드별 동작 비교
| 항목 | `FAIL_FAST` (기본) | `PARTIAL_SUCCESS` |
|---|---|---|
| 체인 실패 시 Code 요청 | 실패(예외 전파) | 성공 반환 |
| 체인 실패 정보 | 예외/실패 응답으로 전달 | `chainFailures[]` 구조화 전달 |
| `chainedDocResult`/`chainedReviewResult` | 실패 지점 이후 미생성 | 성공한 체인 결과는 유지, 실패 체인은 `null` |
| run-state 이벤트 | `CHAIN_*_FAILED` 기록 | 동일하게 `CHAIN_*_FAILED` 기록 |
| 하위 호환성 | 기존과 동일 | 명시 요청 시에만 동작 |

## API 예시

### 1) 성공 예시 (체인 성공)
요청:
```json
{
  "projectId": "demo-auth",
  "targetProjectRoot": ".",
  "userRequest": "로그인 API 스켈레톤을 만들어줘",
  "chainToDoc": true,
  "chainToReview": true,
  "chainFailurePolicy": "PARTIAL_SUCCESS"
}
```

응답(요약):
```json
{
  "runId": "code-run-100",
  "chainedDocResult": { "runId": "doc-run-10" },
  "chainedReviewResult": { "runId": "review-run-10" },
  "chainFailures": []
}
```

### 2) 체인 실패 예시 (`PARTIAL_SUCCESS`)
요청:
```json
{
  "projectId": "demo-auth",
  "targetProjectRoot": ".",
  "userRequest": "로그인 API 스켈레톤을 만들어줘",
  "chainToDoc": true,
  "chainToReview": true,
  "chainFailurePolicy": "PARTIAL_SUCCESS"
}
```

응답(요약):
```json
{
  "runId": "code-run-101",
  "chainedDocResult": null,
  "chainedReviewResult": { "runId": "review-run-11" },
  "chainFailures": [
    {
      "agent": "DOC",
      "failedStage": "CHAIN_DOC",
      "errorMessage": "doc failure"
    }
  ]
}
```

## 수용기준 점검
1. 기본 요청(정책 미지정) fail-fast 유지: **충족**
2. `PARTIAL_SUCCESS`에서 체인 실패 시 Code 성공 + 실패 정보 포함: **충족**
3. `PARTIAL_SUCCESS`에서 체인 성공 시 기존 필드 공존: **충족**
4. run-state 이벤트 계약 유지(`CHAIN_*_TRIGGERED/DONE/FAILED`): **충족**
5. 문서 계약 반영(`docs/code-agent-api.md`): **충족**
6. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 정책/회귀 중심 테스트:
  - 명령: `./gradlew test --no-daemon --tests 'me.karubidev.devagent.agents.code.CodeAgentServiceTest' --tests 'me.karubidev.devagent.agents.doc.CodeDocChainServiceTest' --tests 'me.karubidev.devagent.agents.review.CodeReviewChainServiceTest'`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- `PARTIAL_SUCCESS`에서 체인 실패가 발생해도 HTTP 레벨은 성공이므로, 클라이언트가 `chainFailures`를 반드시 확인하지 않으면 보조 체인 실패를 놓칠 수 있습니다.
- 현재 실패 단계 값은 `CHAIN_DOC`/`CHAIN_REVIEW` 고정이며, 체인 내부 세부 단계(예: 파싱/검증/저장) 구분은 후속 확장이 필요할 수 있습니다.

## 승인 필요 항목
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경: **없음**
- 사전 승인 필요 항목: **해당 없음**
