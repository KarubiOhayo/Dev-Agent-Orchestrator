# [H-023] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-023-fallback-warning-recovery-action-execution-tracking.md`
- 참고 result(있으면): `coordination/REPORTS/H-022-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-022-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-022-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-023-fallback-warning-recovery-action-execution-tracking.md`
5. `coordination/REPORTS/H-022-result.md`, `coordination/REPORTS/H-022-review.md`, `coordination/RELAYS/H-022-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-023-fallback-warning-recovery-action-execution-tracking.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-023-result.md`
- `coordination/RELAYS/H-023-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-022에서 고정한 출력 계약(`executionRecoveryPlan`, `executionRecoveryProgress`)은 유지하고 H-023에서는 이행률 추적(`executionRecoveryTrend`, `recoveryActionStatus`)만 추가한다.
  - 최근 7일 절대 gap(`executionGap`, `chainShareGap`)과 직전 7일 대비 delta(`executionGapDelta`, `chainShareGapDelta`)를 반드시 함께 보고한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- 알려진 리스크:
  - 최신 14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 `0일`로 `HOLD` 장기화 가능성이 높다.
  - 최근 7일 누적 실행률 `0.45%`(`1/224`), 전체 `executionGap=223`, `DOC`/`REVIEW` `chainRuns=0` 상태가 지속 중이다.
- 리뷰 집중 포인트:
  - `executionGapDelta`/`chainShareGapDelta` 산식과 개선/미개선 해석 규칙이 문서/템플릿에서 동일한지
  - `recoveryActionStatus`가 원인(`LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP`/`COLLECTION_FAILURE`)별 상태와 근거(runId/집계표)를 함께 제공하는지
  - 유지 원칙(임계치/알림 룰 수치, `INSUFFICIENT_SAMPLE` 제외 규칙, 이벤트/모수 정의 불변)이 보존되는지
