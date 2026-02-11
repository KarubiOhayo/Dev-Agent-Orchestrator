# Code Agent API

## 개요

`/api/agents/code/generate`는 모델 라우팅 정책을 적용하고, 선택된 벤더 모델로 실제 LLM API를 호출합니다.
또한 응답 코드블록을 파싱해 `dry-run` 또는 실제 파일 쓰기(`apply=true`)까지 수행할 수 있습니다.

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
    "overwriteExisting": false
  }'
```

## 응답 필드

- `runId`: 실행 추적 ID
- `projectId`, `targetProjectRoot`: 실행 대상 프로젝트 정보
- `routeDecision`: 선택된 primary/fallback 모델 목록
- `usedProvider`, `usedModel`: 실제 성공한 호출 정보
- `output`: 모델이 생성한 텍스트
- `attempts`: 후보 모델별 시도/실패 메시지
- `referencedContextFiles`: 컨텍스트에 실제로 주입된 파일 목록
- `projectSummary`: 프로젝트 최신 요약 메모리
- `applyResult`: 파싱 파일 수/쓰기 결과(`DRY_RUN`, `WRITTEN`, `SKIPPED`, `REJECTED`, `ERROR`)
