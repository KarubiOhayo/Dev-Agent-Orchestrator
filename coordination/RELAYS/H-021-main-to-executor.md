# [H-021] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-021-fallback-warning-execution-mix-recovery-tracking.md`
- 참고 result(있으면): `coordination/REPORTS/H-020-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-020-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-020-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-021-fallback-warning-execution-mix-recovery-tracking.md`
5. `coordination/REPORTS/H-020-result.md`, `coordination/REPORTS/H-020-review.md`, `coordination/RELAYS/H-020-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-021-fallback-warning-execution-mix-recovery-tracking.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-021-result.md`
- `coordination/RELAYS/H-021-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-020의 `READY/HOLD + unmetGates + executionRate` 계약을 유지하고, H-021에서는 `directRuns`/`chainRuns`/`chainShare`를 동일 문구로 문서/템플릿에 동기화한다.
  - 호출 믹스 산식(`totalActualRuns = directRuns + chainRuns`, `chainShare` 분모 0 처리, `achievementRate`, `overallExecutionRate`)을 명시하고 임의 해석 여지를 없앤다.
  - 이번 라운드는 운영 지표 추적 강화 라운드로 임계치/알림 룰 수치 변경을 포함하지 않는다.
- 알려진 리스크:
  - 최근 14일 기준 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0일`로 `HOLD`가 장기화될 가능성이 높다.
  - 최근 7일 누적 실행률 `0.45%`(`1/224`)와 `DOC`/`REVIEW` 실행 `0건`으로 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 리스크가 지속 중이다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 호출 믹스 필드/산식이 동일한지
  - 최근 7일 직접 호출 vs 체인 호출 비중이 14일 게이트 판정 및 `READY/HOLD` 근거와 일관되게 연결되는지
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지
