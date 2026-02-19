# [H-013] Review -> Main Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-013-fallback-warning-runstate-baseline.md`
- result: `coordination/REPORTS/H-013-result.md`
- review: `coordination/REPORTS/H-013-review.md`

## 리뷰 결과 요약
- 리스크 수준: `MEDIUM`
- P1 개수: `0`
- P2 개수: `1`
- P3 개수: `1`

## 핵심 Findings
1. `docs/code-agent-api.md`의 `parseEligibleRunCount` 정의가 API 엔드포인트 호출 기준으로 읽혀, Code 체인으로 수행되는 DOC/REVIEW run을 분모에서 누락 해석할 여지가 있습니다. (`docs/code-agent-api.md:126`, `src/main/java/me/karubidev/devagent/agents/doc/CodeDocChainService.java:49`, `src/main/java/me/karubidev/devagent/agents/review/CodeReviewChainService.java:49`)
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`는 `INSUFFICIENT_SAMPLE` 표기 규칙은 있으나 임계치/알림 계산 제외 규칙이 직접 명시되지 않아 운영 보고 해석 불일치 가능성이 있습니다. (`coordination/AUTOMATIONS/A-001-nightly-test-report.md:26`, `docs/code-agent-api.md:131`)
3. 나머지 handoff 요구사항(이벤트/산식/임계치/알림 룰 문서화, PROJECT_OVERVIEW 정합화, 테스트 게이트 보고)은 확인되었습니다. (`docs/code-agent-api.md:107`, `docs/PROJECT_OVERVIEW.md:108`, `coordination/REPORTS/H-013-result.md:63`)

## 승인 게이트 체크
- 수용기준 충족 여부: **부분 충족** (문구 보정 2건 필요)
- `./gradlew clean test --no-daemon` 통과 여부: 통과 (`BUILD SUCCESSFUL`, Executor 보고 인용)
- 공통 파일 변경 승인 절차 준수 여부: 준수 (`application.yml`, 공용 모델/빌드 설정 변경 없음, Executor 보고 인용)

## Main-Control 요청
- 권고 판단: `Conditional Go`
- 다음 라운드 제안 1건:
  - fallback warning 집계 기준 문구 정합화(H-014): `parseEligibleRunCount`의 체인 실행 포함 기준 명시 + `INSUFFICIENT_SAMPLE` 임계치/알림 제외 규칙을 야간 템플릿에 동기화
