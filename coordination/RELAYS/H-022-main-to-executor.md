# [H-022] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-022-fallback-warning-execution-mix-recovery-action-plan.md`
- 참고 result(있으면): `coordination/REPORTS/H-021-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-021-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-021-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-022-fallback-warning-execution-mix-recovery-action-plan.md`
5. `coordination/REPORTS/H-021-result.md`, `coordination/REPORTS/H-021-review.md`, `coordination/RELAYS/H-021-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-022-fallback-warning-execution-mix-recovery-action-plan.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-022-result.md`
- `coordination/RELAYS/H-022-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-021의 호출 믹스 계약(`directRuns`, `chainRuns`, `chainShare`)을 유지한 상태에서 H-022에서는 목표-실적 gap 기반 실행량 회복 액션 플랜을 추가한다.
  - `executionRecoveryPlan`/`executionRecoveryProgress` 필드는 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 동일 키로 동기화한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- 알려진 리스크:
  - 최신 14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0일`로 `HOLD` 장기화 가능성이 높다.
  - 최근 7일 누적 실행률 `0.45%`(`1/224`), `DOC`/`REVIEW` `chainRuns=0` 상태로 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 리스크가 지속 중이다.
- 리뷰 집중 포인트:
  - 목표-실적 gap 산식(`targetTotalRuns`, `actualTotalRuns`, `executionGap`, `chainShareGap`)이 문서/템플릿에서 동일하게 정의되는지
  - `HOLD` 원인 분류가 `executionGap`/`chainShareGap` 근거와 일관되게 연결되는지
  - 유지 원칙(임계치/알림 룰 수치, `INSUFFICIENT_SAMPLE` 제외 규칙, 이벤트/모수 정의 불변)이 보존되는지
