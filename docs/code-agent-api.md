# Code Agent API

## 개요

`/api/agents/code/generate`는 모델 라우팅 정책을 적용하고, 선택된 벤더 모델로 실제 LLM API를 호출합니다.
또한 응답 코드블록을 파싱해 `dry-run` 또는 실제 파일 쓰기(`apply=true`)까지 수행할 수 있습니다.
선택적으로 `chainToDoc=true`를 주면 Code 결과를 기반으로 DocAgent를 연쇄 실행합니다.
`chainToReview=true`를 주면 Code 결과를 기반으로 ReviewAgent를 연쇄 실행합니다.

`/api/agents/doc/generate`는 Code 산출물을 입력으로 받아 구조화된 문서(JSON 스키마)를 생성합니다.
`/api/agents/review/generate`는 Code 산출물을 입력으로 받아 구조화된 리뷰(JSON 스키마)를 생성합니다.

CLI 초안 사용법은 `/docs/cli-quickstart.md`를 참고하세요.

## 설정

`application.yml`의 아래 설정을 사용합니다.

- `devagent.model-routing`: Agent별 모델 선택/폴백 정책
- `devagent.llm`: 벤더별 base URL, API key, 생성 옵션
- `devagent.context`: rules/examples 선별 주입(top-k)
- `devagent.prompt`: 공통/에이전트/프로젝트 프롬프트 합성
- `devagent.run-state`: 실행 로그/프로젝트 메모리 저장(SQLite 우선, fallback 지원)

## API 키 주입

다음 환경 변수(또는 `.env`)를 읽습니다.

- `OPENAI_API_KEY`
- `ANTHROPIC_API_KEY`
- `GOOGLE_API_KEY` 또는 `GEMINI_API_KEY`

## 요청 예시

```bash
curl -X POST http://localhost:8080/api/agents/code/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "로그인 API 스켈레톤을 만들어줘",
    "mode": "BALANCED",
    "riskLevel": "MEDIUM",
    "largeContext": false,
    "strictJsonRequired": true,
    "apply": false,
    "overwriteExisting": false,
    "chainToDoc": true,
    "docUserRequest": "생성된 코드 기준으로 API/구조 문서를 작성해줘",
    "chainToReview": true,
    "reviewUserRequest": "보안/안정성 관점으로 우선순위 리뷰를 작성해줘",
    "chainFailurePolicy": "FAIL_FAST"
  }'
```

## 응답 필드

- `runId`: 실행 추적 ID
- `projectId`, `targetProjectRoot`: 실행 대상 프로젝트 정보
- `routeDecision`: 선택된 primary/fallback 모델 목록
- `usedProvider`, `usedModel`: 실제 성공한 호출 정보
- `output`: 모델이 생성한 텍스트
- `files`: 파싱된 생성 파일 목록
  - 형식: `[{ "path": "relative/path", "content": "string" }]`
  - 의미: 모델 출력에서 추출한 "작성 대상 파일 초안"이며, `apply=false`여도 항상 반환될 수 있음
  - `applyResult`와의 관계: `files`는 "계획/파싱 결과", `applyResult`는 해당 파일들에 대해 실제 적용(또는 dry-run 평가)한 결과
- `attempts`: 후보 모델별 시도/실패 메시지
- `referencedContextFiles`: 컨텍스트에 실제로 주입된 파일 목록
- `projectSummary`: 프로젝트 최신 요약 메모리
- `applyResult`: 파싱 파일 수/쓰기 결과(`DRY_RUN`, `WRITTEN`, `SKIPPED`, `REJECTED`, `ERROR`)
- `chainedDocResult`: `chainToDoc=true`일 때 DocAgent 실행 결과
- `chainedReviewResult`: `chainToReview=true`일 때 ReviewAgent 실행 결과
- `chainFailures`: 체인 실패 구조화 목록
  - 형식: `[{ "agent": "DOC|REVIEW", "failedStage": "CHAIN_DOC|CHAIN_REVIEW", "errorMessage": "string" }]`
  - `chainFailurePolicy=PARTIAL_SUCCESS`일 때 체인 실패가 발생하면 해당 목록에 누적됨
  - `chainFailurePolicy=FAIL_FAST`일 때는 체인 실패 즉시 요청 실패로 전파되므로 일반적으로 빈 배열

## 체인 실패 정책

- `chainFailurePolicy` 기본값: `FAIL_FAST` (하위 호환)
- 선택값:
  - `FAIL_FAST`: Doc/Review 체인 실패 시 Code 요청 전체를 실패로 반환
  - `PARTIAL_SUCCESS`: 체인 실패를 `chainFailures`에 기록하고 Code 요청은 성공으로 반환
- `PARTIAL_SUCCESS`에서도 run-state 이벤트(`CHAIN_*_TRIGGERED/DONE/FAILED`) 기록 계약은 동일하게 유지됨
- `PARTIAL_SUCCESS`를 사용하는 클라이언트는 HTTP 200이어도 `chainFailures[]`를 반드시 확인해야 함

## 출력 파싱 fallback 관측 이벤트 (run-state)

- Code: `CODE_OUTPUT_FALLBACK_WARNING`
  - 기록 조건: code 출력이 `files[]` JSON으로 직접 파싱되지 않고 markdown fallback 경로를 사용한 경우
  - 메시지 형식: `source=MARKDOWN_FALLBACK`
- Spec: `SPEC_OUTPUT_FALLBACK_WARNING`
  - 기록 조건: spec 출력 파싱 source가 `DIRECT_JSON`이 아닌 경우
    - 경고 대상 source: `JSON_CODE_BLOCK`, `FALLBACK_SCHEMA`
  - 메시지 형식: `source=<PARSE_SOURCE>`
- Doc: `DOC_OUTPUT_FALLBACK_WARNING`
  - 기록 조건: doc 출력 파싱이 fallback schema(`FALLBACK`) 경로를 사용한 경우
  - 메시지 형식: `source=FALLBACK`
- Review: `REVIEW_OUTPUT_FALLBACK_WARNING`
  - 기록 조건: review 출력 파싱이 fallback schema(`FALLBACK`) 경로를 사용한 경우
  - 메시지 형식: `source=FALLBACK`

## fallback warning run-state 집계 기준 (운영 계약)

### 대상 이벤트

- `CODE_OUTPUT_FALLBACK_WARNING`
- `SPEC_OUTPUT_FALLBACK_WARNING`
- `DOC_OUTPUT_FALLBACK_WARNING`
- `REVIEW_OUTPUT_FALLBACK_WARNING`

### 집계 단위

- 기본 집계는 `agent별 일 단위(KST 00:00~23:59)`로 수행한다.
- 동일 기간의 `전체 집계(모든 agent 합산)`를 함께 계산한다.
- 보고서에는 이벤트별 건수, `parseEligibleRunCount`, `warningRate`, 임계치 판정 결과를 모두 포함한다.

### 모수/경고율 정의

- 경고율 산식:
  - `warningRate = warningEventCount / parseEligibleRunCount`
- `warningEventCount`:
  - 해당 집계 단위(일/agent 또는 전체)에서 발생한 `*_OUTPUT_FALLBACK_WARNING` 이벤트 수
- `parseEligibleRunCount`:
  - 동일 집계 단위에서 파싱 대상 출력을 생성한 실행 수
  - Code: `POST /api/agents/code/generate` 실행 중 출력 파싱 단계에 진입한 run 수
  - Spec: `POST /api/agents/spec/generate` 실행 중 출력 파싱 단계에 진입한 run 수
  - Doc: `POST /api/agents/doc/generate` 실행 중 출력 파싱 단계에 진입한 run 수
  - Review: `POST /api/agents/review/generate` 실행 중 출력 파싱 단계에 진입한 run 수
- 최소 샘플 수 조건:
  - `parseEligibleRunCount < 20`이면 판정 등급을 `INSUFFICIENT_SAMPLE`로 표시하고 임계치 판정/알림 트리거에서 제외한다.

### 임계치

| 등급 | 조건 (`warningRate`) | 운영 해석 |
|---|---|---|
| NORMAL | `< 0.05` | 정상 범위 |
| CAUTION | `>= 0.05` and `< 0.15` | 주의, 추세 관찰 필요 |
| WARNING | `>= 0.15` | 경고, 원인 분석 및 대응 필요 |

### 알림 룰

- 연속 초과:
  - 동일 agent가 `WARNING` 등급을 2일 연속 기록하면 알림을 발생시킨다.
- 급증:
  - 전일 대비 `warningRate`가 `+0.10`p 이상 상승하고 `warningEventCount`가 5건 이상 증가하면 알림을 발생시킨다.
- 전체 집계 보호 규칙:
  - 전체 집계 `warningRate >= 0.10`이면 agent별 상태와 별개로 일괄 점검 알림을 발생시킨다.
- `INSUFFICIENT_SAMPLE`은 알림 대상에서 제외하되, 보고서에 `표본 부족` 상태를 명시한다.

## 공통 오류 응답 계약 (Routing + Agent API)

다음 엔드포인트는 입력 오류를 동일한 오류 envelope로 반환합니다.

- `POST /api/routing/resolve`
- `POST /api/agents/code/generate`
- `POST /api/agents/spec/generate`
- `POST /api/agents/doc/generate`
- `POST /api/agents/review/generate`

오류 응답 envelope:

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

- `code`: 오류 코드
- `message`: 사람이 읽을 수 있는 오류 메시지
- `path`: 요청 경로
- `timestamp`: UTC 시각(ISO-8601)
- `details[]`: 필드 단위 오류 상세(존재 시 포함)
- 복합 필수조건 누락(any-of)은 `code=MISSING_REQUIRED_ANY_OF`를 사용하고, 후보 필드 각각을 `details[]`에 `reason=any_of_required`로 기록

상태 코드/오류 코드 매핑:

- `400`: `MISSING_REQUIRED_FIELD`, `MISSING_REQUIRED_ANY_OF`, `REQUEST_BODY_REQUIRED`, `INVALID_ENUM_VALUE`, `MALFORMED_JSON`, `INVALID_JSON_VALUE`, `INVALID_JSON_REQUEST`, `INVALID_ARGUMENT`
- `500`: `INTERNAL_SERVER_ERROR`

오류 케이스 예시:

1) 필수 입력 누락/공백

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

2) 복합 필수조건(any-of) 누락

```json
{
  "code": "MISSING_REQUIRED_ANY_OF",
  "message": "userRequest or specInputPath is required",
  "path": "/api/agents/code/generate",
  "timestamp": "2026-02-19T03:17:11.300Z",
  "details": [
    {
      "field": "userRequest",
      "reason": "any_of_required",
      "rejectedValue": null
    },
    {
      "field": "specInputPath",
      "reason": "any_of_required",
      "rejectedValue": null
    }
  ]
}
```

3) enum 오류

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

4) JSON 파싱 오류

```json
{
  "code": "MALFORMED_JSON",
  "message": "Malformed JSON request body",
  "path": "/api/agents/review/generate",
  "timestamp": "2026-02-19T02:41:03.550Z",
  "details": []
}
```

5) 서버 오류

```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "Internal server error",
  "path": "/api/agents/review/generate",
  "timestamp": "2026-02-19T02:41:10.777Z",
  "details": []
}
```

## Doc Agent 요청 예시

```bash
curl -X POST http://localhost:8080/api/agents/doc/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "로그인 코드 기준 운영 문서를 생성해줘",
    "mode": "BALANCED",
    "riskLevel": "MEDIUM",
    "largeContext": false,
    "strictJsonRequired": true,
    "codeRunId": "code-run-123",
    "codeOutput": "{\"files\":[...]}",
    "codeFiles": [
      {"path":"src/main/java/me/example/AuthController.java","content":"..."}
    ]
  }'
```

## Doc Agent 응답 필드

- `runId`: Doc 실행 추적 ID
- `projectId`, `targetProjectRoot`: 실행 대상 프로젝트 정보
- `routeDecision`: 선택된 primary/fallback 모델 목록
- `usedProvider`, `usedModel`: 실제 성공한 호출 정보
- `document`: 문서 JSON 스키마 결과
  - 형식:
    - `title`: 문자열
    - `summary`: 문자열
    - `sections`: `[{ "heading": "string", "content": "string" }]`
    - `relatedFiles`: `["relative/path"]`
    - `notes`: `["string"]`
- `attempts`: 후보 모델별 시도/실패 메시지
- `referencedContextFiles`: 컨텍스트에 실제로 주입된 파일 목록
- `projectSummary`: 프로젝트 최신 요약 메모리
- `sourceCodeRunId`: 체인 입력으로 사용된 Code run ID(없으면 `null`)

## Review Agent 요청 예시

```bash
curl -X POST http://localhost:8080/api/agents/review/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "생성 코드의 잠재 버그와 개선 포인트를 점검해줘",
    "mode": "BALANCED",
    "riskLevel": "MEDIUM",
    "largeContext": false,
    "strictJsonRequired": true,
    "codeRunId": "code-run-123",
    "codeOutput": "{\"files\":[...]}",
    "codeFiles": [
      {"path":"src/main/java/me/example/AuthController.java","content":"..."}
    ]
  }'
```

## Review Agent 응답 필드

- `runId`: Review 실행 추적 ID
- `projectId`, `targetProjectRoot`: 실행 대상 프로젝트 정보
- `routeDecision`: 선택된 primary/fallback 모델 목록
- `usedProvider`, `usedModel`: 실제 성공한 호출 정보
- `review`: 리뷰 JSON 스키마 결과
  - 형식:
    - `summary`: 문자열
    - `overallRisk`: `LOW|MEDIUM|HIGH|CRITICAL`
    - `findings`: `[{ "title": "string", "severity": "LOW|MEDIUM|HIGH|CRITICAL", "file": "relative/path", "line": 0, "description": "string", "suggestion": "string" }]`
    - `strengths`: `["string"]`
    - `nextActions`: `["string"]`
- `attempts`: 후보 모델별 시도/실패 메시지
- `referencedContextFiles`: 컨텍스트에 실제로 주입된 파일 목록
- `projectSummary`: 프로젝트 최신 요약 메모리
- `sourceCodeRunId`: 체인 입력으로 사용된 Code run ID(없으면 `null`)
