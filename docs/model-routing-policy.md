# Model Routing Policy

이 프로젝트는 각 Agent 역할별로 모델을 다르게 라우팅합니다.

## Routing Mode

- `COST_SAVER`: 비용/속도 우선
- `BALANCED`: 기본 운영 모드
- `QUALITY`: 품질 우선
- `GEMINI3_CANARY`: Gemini 3 Preview 제한 적용 모드 (일부 Agent 전용)

## Agent 별 기본 전략

- `ROUTER`: 경량 모델 중심으로 빠른 분류
- `SPEC`: 구조화/정확도 중심
- `CODE`: 코드 생성 품질 중심
- `DOC`: 대량 문서 처리 비용 최적화
- `REVIEW`: 리뷰 강도와 리스크 기반 승격
- `REFACTOR`: 수정 안정성 중심
- `HELP`: 일반 질의 응답

정책은 `/src/main/resources/application.yml`의 `devagent.model-routing`에서 관리합니다.
`CODE`/`REFACTOR`의 `BALANCED` primary는 `openai:gpt-5.2-codex`를 사용합니다.

## Escalation Rules

- `high-risk review`: REVIEW + HIGH 리스크일 때 고성능 리뷰 모델로 승격
- `large-context`: 긴 컨텍스트 요청일 때 장문 컨텍스트 강점 모델 우선
- `strict-json`: `strictJsonRequired=true`가 명시된 요청에서만 구조화 출력 강점 모델 우선

## Canary 운영 원칙

- `GEMINI3_CANARY`는 현재 `CODE`/`DOC` Agent에서만 Preview 모델 성능 검증용으로 사용합니다.
- Canary 미지원 Agent가 `GEMINI3_CANARY`로 요청되면 기본 모드 정책으로 fallback 됩니다.
- 운영 기본 모드는 `BALANCED`를 유지하고, 실패 시 stable fallback으로 자동 전환합니다.

## Runtime API

- Endpoint: `POST /api/routing/resolve`

요청 예시:

```json
{
  "agentType": "CODE",
  "mode": "BALANCED",
  "riskLevel": "MEDIUM",
  "largeContext": false,
  "strictJsonRequired": false
}
```

응답 예시:

```json
{
  "agentType": "CODE",
  "mode": "BALANCED",
  "riskLevel": "MEDIUM",
  "primary": {
    "provider": "openai",
    "model": "gpt-5.2-codex"
  },
  "fallbacks": [
    {
      "provider": "anthropic",
      "model": "claude-sonnet-4-5-20250929"
    },
    {
      "provider": "google",
      "model": "gemini-2.5-pro"
    }
  ],
  "reasons": [
    "mode policy applied: BALANCED"
  ]
}
```
