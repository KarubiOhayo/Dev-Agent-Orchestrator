# H-013 리뷰 보고서

## 대상
- handoff: `coordination/HANDOFFS/H-013-fallback-warning-runstate-baseline.md`
- result: `coordination/REPORTS/H-013-result.md`
- relay: `coordination/RELAYS/H-013-executor-to-review.md`

## Findings (P1 > P2 > P3)
### P2. `parseEligibleRunCount` 정의가 체인 실행 경로를 누락하는 표현
- `docs/code-agent-api.md`는 `parseEligibleRunCount`를 agent별로 설명하면서 `POST /api/agents/{agent}/generate` 실행 기준으로 고정하고 있습니다.
  - `docs/code-agent-api.md:126`
  - `docs/code-agent-api.md:127`
  - `docs/code-agent-api.md:128`
  - `docs/code-agent-api.md:129`
- 하지만 DOC/REVIEW는 Code 체인 경로에서도 서비스가 직접 호출되어 run-state run/event가 기록됩니다.
  - `src/main/java/me/karubidev/devagent/agents/doc/CodeDocChainService.java:49`
  - `src/main/java/me/karubidev/devagent/agents/review/CodeReviewChainService.java:49`
  - `src/main/java/me/karubidev/devagent/agents/doc/DocAgentService.java:62`
  - `src/main/java/me/karubidev/devagent/agents/doc/DocAgentService.java:89`
  - `src/main/java/me/karubidev/devagent/agents/review/ReviewAgentService.java:62`
  - `src/main/java/me/karubidev/devagent/agents/review/ReviewAgentService.java:89`
- 영향: 운영자가 분모를 API 엔드포인트 호출 수로 해석할 경우 체인 실행분이 제외되어 `warningRate`가 과대 계산될 수 있습니다.
- 권고: `parseEligibleRunCount`를 "agent 서비스 run 기준(직접 호출 + 체인 호출 포함)"으로 명시 보정 필요.

### P3. `INSUFFICIENT_SAMPLE` 제외 규칙이 야간 템플릿에 명시되지 않음
- 기준 문서에는 `parseEligibleRunCount < 20`이면 임계치 판정/알림 트리거에서 제외한다고 명시되어 있습니다.
  - `docs/code-agent-api.md:131`
- 야간 템플릿에는 `INSUFFICIENT_SAMPLE` 표기 규칙은 있으나, 임계치/알림 제외 규칙이 직접적으로 적혀 있지 않습니다.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md:26`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md:54`
- 영향: 자동 점검 보고에서 샘플 부족 집단을 임계치/알림 판단에 포함할지 운영자마다 다르게 해석될 수 있습니다.
- 권고: 템플릿에 "샘플 부족은 임계치 판정/알림 계산 제외" 문구를 추가해 기준 문서와 동기화 필요.

## 검증 근거 (파일/라인)
1. H-013 산출물의 변경 범위 및 테스트 게이트 보고 확인
- `coordination/REPORTS/H-013-result.md:7`
- `coordination/REPORTS/H-013-result.md:59`
- `coordination/REPORTS/H-013-result.md:63`
- `coordination/RELAYS/H-013-executor-to-review.md:21`
- `coordination/RELAYS/H-013-executor-to-review.md:22`

2. fallback warning 집계 기준 문서화 반영 여부 확인
- 대상 이벤트/집계 단위/경고율/임계치/알림 룰 명시 확인
  - `docs/code-agent-api.md:107`
  - `docs/code-agent-api.md:114`
  - `docs/code-agent-api.md:121`
  - `docs/code-agent-api.md:135`
  - `docs/code-agent-api.md:143`

3. 야간 자동 점검 템플릿의 보고 항목/Plan A 제약 확인
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:17`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:39`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md:49`

4. PROJECT_OVERVIEW 정합화 확인
- `docs/PROJECT_OVERVIEW.md:36`
- `docs/PROJECT_OVERVIEW.md:102`
- `docs/PROJECT_OVERVIEW.md:108`

## 심각도 집계
- P1: 0
- P2: 1
- P3: 1

## 수용기준 검증
1. `docs/code-agent-api.md`에 집계 기준 반영: **부분 충족** (체인 실행 포함 모수 정의 문구 보정 필요)
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 동일 기준 반영: **부분 충족** (`INSUFFICIENT_SAMPLE` 제외 규칙 명시 보완 필요)
3. `docs/PROJECT_OVERVIEW.md` 리스크/우선순위 정합화: **충족**
4. 코드/설정 변경 없음: **충족** (Executor 결과 보고 인용 기준)
5. `./gradlew clean test --no-daemon` 통과: **충족** (Executor 결과 보고 인용)

## 승인 게이트 체크
- 테스트 게이트 상태(Executor 보고 인용):
  - `./gradlew clean test --no-daemon` -> `BUILD SUCCESSFUL`
  - 근거: `coordination/REPORTS/H-013-result.md:62`, `coordination/REPORTS/H-013-result.md:63`
- Review-Control 제약상 테스트를 재실행하지 않고 결과 리포트/문서/코드 경로를 대조 검증함.
- 공통 파일 변경 승인 절차 준수 여부:
  - 공통 승인 대상(`application.yml`, 공용 모델/계약, 빌드 설정) 변경 없음(Executor 보고 인용)
  - 근거: `coordination/REPORTS/H-013-result.md:70`

## 리뷰 결론
- 리스크 수준: `MEDIUM`
- 최종 권고: `Conditional Go`
- 조건:
  1. `docs/code-agent-api.md`의 `parseEligibleRunCount` 정의를 체인 실행 포함 기준으로 명시 보정
  2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 `INSUFFICIENT_SAMPLE`의 임계치/알림 제외 규칙 명시
