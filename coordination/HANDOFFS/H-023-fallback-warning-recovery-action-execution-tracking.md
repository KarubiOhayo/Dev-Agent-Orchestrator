# H-023 fallback warning 실행량 회복 액션 이행률 추적/검증

Owner: WT-23 (`codex/h023-fallback-warning-recovery-action-execution-tracking`)
Priority: Highest

## 목표
- H-022에서 수립한 실행량 회복 액션(`LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP`)의 실제 이행률을 최근 7일 실측으로 검증한다.
- 최근 7일 `executionGap`/`chainShareGap` 절대값과 직전 7일 대비 delta를 함께 고정해 회복 추세를 판단 가능하게 만든다.
- 최신 14일 게이트 판정과 액션 이행 상태를 결합해 다음 라운드 `READY/HOLD` 판단 입력을 강화한다.

## 작업 범위
- 운영 문서 갱신
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-022-result.md`
  - `coordination/REPORTS/H-022-review.md`
  - `coordination/RELAYS/H-022-review-to-main.md`
  - `docs/code-agent-api.md`의 H-022 실행량 회복 액션 플랜 섹션
- 집계 구간:
  - 최신 14일(KST, `today-13 ~ today`) 게이트 판정을 유지한다.
  - 최근 7일(KST)은 실행량 회복 이행률(절대 gap + delta)로 별도 보고한다.
- `docs/code-agent-api.md`에 H-023 실행량 회복 이행률 추적 섹션을 추가/갱신한다.
  - 기존 4개 게이트(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`) 판정은 유지한다.
  - 최근 7일 agent별로 아래 항목을 고정한다.
    - `targetDirectRuns`, `targetChainRuns`, `targetTotalRuns`, `targetChainShare`
    - `actualDirectRuns`, `actualChainRuns`, `actualTotalRuns`, `actualChainShare`
    - `executionGap`, `chainShareGap`
    - `executionGapDelta = executionGap(최근7일) - executionGap(직전7일)`
    - `chainShareGapDelta = chainShareGap(최근7일) - chainShareGap(직전7일)`
  - `executionGapDelta < 0` 또는 `chainShareGapDelta < 0`를 개선 신호로 해석하고, 0 이상이면 미개선으로 분류한다.
  - `HOLD` 지속 시 액션 우선순위별 상태를 `IN_PROGRESS|BLOCKED|DONE`으로 표준화해 근거(runId/집계표)와 함께 보고한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화한다.
  - 기존 출력(`READY/HOLD`, `unmetGates`, `agentExecution`, `executionMix`, `executionRecoveryPlan`, `executionRecoveryProgress`)을 유지한다.
  - `executionRecoveryTrend`(`executionGap`, `executionGapDelta`, `chainShareGap`, `chainShareGapDelta`)를 필수 출력으로 추가한다.
  - `recoveryActionStatus`(`cause`, `priority`, `status`, `evidence`)를 필수 출력으로 추가한다.
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-023 실행량 회복 이행률 추적 섹션(최근 7일 절대 gap + delta + 14일 게이트 판정 + `READY/HOLD` 근거)이 반영된다.
2. delta 산식(`executionGapDelta`, `chainShareGapDelta`)과 해석 규칙(개선/미개선 분기)이 문서에 명시된다.
3. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 동일 출력 계약(`executionRecoveryTrend`, `recoveryActionStatus`)으로 정렬된다.
4. `HOLD` 시 액션 우선순위별 상태(`IN_PROGRESS|BLOCKED|DONE`)와 근거(runId/집계표)가 함께 보고된다.
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 실행량 회복 액션 이행률 검증 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-023-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-023-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최신 14일 게이트 4개 실측 + PASS/FAIL 표
  - 최근 7일 agent별 목표-실적(`target*`, `actual*`, `executionGap`, `chainShareGap`) 표
  - 최근 7일 vs 직전 7일 `executionGapDelta`/`chainShareGapDelta` 표
  - `overallExecutionRate` 추세 + `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 근거
  - Projection 재산정 결과(`requiredSufficientDays`, `예상 착수 가능일` 또는 미산정 사유)
  - `READY/HOLD` 최종 판정 및 근거
  - `HOLD` 시 원인 분류/보완 액션 우선순위 + 상태(`IN_PROGRESS|BLOCKED|DONE`) + 근거
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
