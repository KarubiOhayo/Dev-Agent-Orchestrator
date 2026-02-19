# H-030 fallback-warning `KEEP_FROZEN` 상태 실행량/체인 커버리지 회복 액션 이행 추적

Owner: WT-30 (`codex/h030-fallback-warning-keep-frozen-recovery-tracking`)
Priority: High

## 목표
- H-029에서 확정된 `KEEP_FROZEN` 판정을 유지한 상태에서 `LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP` 회복 액션 이행 증거를 누락 없이 추적한다.
- 최신 14일/7일 실측을 기준으로 재개 신호 변화 여부를 보고하되, 판정은 `RESUME_H024|KEEP_FROZEN` 단일값으로 유지한다.
- 운영 문서와 야간 점검 템플릿의 이행 추적 출력 계약을 동기화해 다음 라운드 승인 근거를 고정한다.

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
  - `coordination/HANDOFFS/H-029-fallback-warning-h024-resume-readiness-check.md`
  - `coordination/REPORTS/H-029-result.md`
  - `coordination/REPORTS/H-029-review.md`
  - `coordination/RELAYS/H-029-review-to-main.md`
  - `docs/code-agent-api.md`의 H-029 재개 판단 섹션
- 집계 구간:
  - 최신 14일(KST, `today-13 ~ today`) 게이트 판정을 유지한다.
  - 최근 7일(KST, `today-6 ~ today`)과 직전 7일(`today-13 ~ today-7`) 추세를 비교한다.
- `docs/code-agent-api.md`에 H-030 이행 추적 섹션을 추가/갱신한다.
  - 기존 게이트/산식/임계치(`parseEligibleRunCount`, `warningRate`, `dailyCompliance`, `weeklyComplianceRate`, `0.05/0.15/+0.10p/0.10`)는 변경하지 않는다.
  - `resumeDecision(RESUME_H024|KEEP_FROZEN)` 단일 판정 계약을 유지한다.
  - 신호별 회복 액션 추적 표를 추가/갱신한다.
    - 필수 필드: `signal`, `priority`, `status(IN_PROGRESS|BLOCKED|DONE)`, `owner`, `evidenceRef`, `nextAction`, `updatedAt`
    - 대상 신호: `LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP` (필수), 필요 시 `COLLECTION_FAILURE` (선택)
  - 이행 요약 지표를 추가/갱신한다.
    - `recoveryActionCompletionRate = doneActions / totalActions`
    - `blockedActionCount`
    - `latestDecisionReason`
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화한다.
  - 기존 출력 계약(`recalibrationReadiness`, `unmetGates`, `resumeDecision`, `unmetReadinessSignals`, `nextCheckTrigger`, `executionRecovery*`)은 유지한다.
  - 신규/보강 출력:
    - `recoveryActionTracking[]` (신호별 status/owner/evidenceRef/nextAction/updatedAt)
    - `recoveryActionCompletionRate`
    - `blockedActionCount`
  - `KEEP_FROZEN`일 때 위 3개 출력이 누락되지 않도록 명시한다.
- 유지 원칙:
  - `INSUFFICIENT_SAMPLE` 제외 규칙(`parseEligibleRunCount < 20`)을 유지한다.
  - run-state 이벤트/스키마 및 API/CLI 동작 코드는 변경하지 않는다.
  - 코드/설정 파일 수정 없이 운영 문서/템플릿 정합화에 집중한다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-030 이행 추적 섹션(신호별 status/evidence + 이행 요약 지표 + 단일 판정)이 반영된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 `recoveryActionTracking[]`/`recoveryActionCompletionRate`/`blockedActionCount` 출력 계약으로 동기화된다.
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
- 이번 라운드는 `KEEP_FROZEN` 상태의 이행 추적 계약 정합화 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-030-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-030-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최신 14일 게이트 4개 실측 + PASS/FAIL 표
  - 최근 7일/직전 7일 `executionGapDelta`/`chainShareGapDelta` 비교 표
  - 신호별 `recoveryActionTracking` 표(status/owner/evidenceRef/nextAction/updatedAt)
  - `recoveryActionCompletionRate`/`blockedActionCount` 계산 결과
  - `RESUME_H024/KEEP_FROZEN` 단일 판정 및 근거
  - 다음 점검 트리거(`nextCheckTrigger`)와 우선순위 액션
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
