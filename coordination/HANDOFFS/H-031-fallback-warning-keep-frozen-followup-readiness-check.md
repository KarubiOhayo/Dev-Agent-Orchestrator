# H-031 fallback-warning `KEEP_FROZEN` 후속 점검 및 `RESUME_H024` 재개 근거 재검증

Owner: WT-31 (`codex/h031-fallback-warning-keep-frozen-followup-readiness-check`)
Priority: High

## 목표
- H-030에서 고정된 `KEEP_FROZEN` 기준선을 바탕으로 최신 실측(14일/7일)에서 H-024 재개 가능 여부를 재검증한다.
- 재개 판정은 `RESUME_H024|KEEP_FROZEN` 단일값으로 유지하고, 게이트 4종과 회복 액션 상태를 함께 근거로 고정한다.
- 운영 문서와 야간 점검 템플릿의 계약을 유지한 채 후속 점검 수치/근거를 동기화한다.

## 작업 범위
- 운영 문서 갱신
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/HANDOFFS/H-024-fallback-warning-recovery-action-baseline-governance.md`
  - `coordination/HANDOFFS/H-030-fallback-warning-keep-frozen-recovery-tracking.md`
  - `coordination/REPORTS/H-029-result.md`
  - `coordination/REPORTS/H-029-review.md`
  - `coordination/RELAYS/H-029-review-to-main.md`
  - `coordination/REPORTS/H-030-result.md`
  - `coordination/REPORTS/H-030-review.md`
  - `coordination/RELAYS/H-030-review-to-main.md`
  - `docs/code-agent-api.md`의 H-029/H-030 fallback warning 섹션
- 집계 구간:
  - 최신 14일(KST, `today-13 ~ today`) 게이트 판정을 유지한다.
  - 최근 7일(KST, `today-6 ~ today`)과 직전 7일(`today-13 ~ today-7`) 추세를 비교한다.
- `docs/code-agent-api.md`에 H-031 후속 점검 섹션을 추가/갱신한다.
  - 기존 게이트/산식/임계치(`parseEligibleRunCount`, `warningRate`, `dailyCompliance`, `weeklyComplianceRate`, `0.05/0.15/+0.10p/0.10`)는 변경하지 않는다.
  - `resumeDecision(RESUME_H024|KEEP_FROZEN)` 단일 판정 계약을 유지한다.
  - 신호별 회복 액션 추적 필드를 최신값으로 갱신한다.
    - 필수 필드: `signal`, `priority`, `status(IN_PROGRESS|BLOCKED|DONE)`, `owner`, `evidenceRef`, `nextAction`, `updatedAt`
    - 대상 신호: `LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP` (필수), 필요 시 `COLLECTION_FAILURE` (선택)
  - 이행 요약 지표를 최신값으로 갱신한다.
    - `recoveryActionCompletionRate = doneActions / totalActions`
    - `blockedActionCount`
    - `latestDecisionReason`
  - `RESUME_H024` 판정 시 즉시 재개 근거(게이트 4종 + 핵심 신호 상태 개선)를 명시한다.
  - `KEEP_FROZEN` 판정 시 미충족 신호(`unmetReadinessSignals`)와 다음 점검 트리거(`nextCheckTrigger`)를 명시한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화한다.
  - 기존 출력 계약(`recalibrationReadiness`, `unmetGates`, `resumeDecision`, `unmetReadinessSignals`, `nextCheckTrigger`, `executionRecovery*`)은 유지한다.
  - `recoveryActionTracking[]`, `recoveryActionCompletionRate`, `blockedActionCount` 출력이 누락되지 않도록 유지한다.
- 유지 원칙:
  - `INSUFFICIENT_SAMPLE` 제외 규칙(`parseEligibleRunCount < 20`)을 유지한다.
  - run-state 이벤트/스키마 및 API/CLI 동작 코드는 변경하지 않는다.
  - 코드/설정 파일 수정 없이 운영 문서/템플릿 정합화에 집중한다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-031 후속 점검 섹션(최신 실측 + 단일 판정 + 근거)이 반영된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 `recoveryActionTracking[]`/`recoveryActionCompletionRate`/`blockedActionCount` 출력 계약을 유지한 채 최신 판정 분기를 반영한다.
3. 최종 판정이 `RESUME_H024` 또는 `KEEP_FROZEN` 중 하나로 명확히 기록된다.
4. 기존 게이트/산식/임계치 및 제외 규칙이 변경되지 않는다.
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 `KEEP_FROZEN` 후속 점검/재개 근거 재검증 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-031-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-031-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최신 14일 게이트 4개 실측 + PASS/FAIL 표
  - 최근 7일/직전 7일 `executionGapDelta`/`chainShareGapDelta` 비교 표
  - 신호별 `recoveryActionTracking` 표(status/owner/evidenceRef/nextAction/updatedAt)
  - `recoveryActionCompletionRate`/`blockedActionCount` 계산 결과
  - `RESUME_H024/KEEP_FROZEN` 단일 판정 및 근거
  - 다음 점검 트리거(`nextCheckTrigger`) 또는 재개 즉시 액션
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
