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
