# H-033 fallback-warning `KEEP_FROZEN` 실행 증거 누적 점검 및 재개 준비도 추적

Owner: WT-33 (`codex/h033-fallback-warning-keep-frozen-evidence-accumulation-check`)
Priority: High

## 목표
- H-032에서 고정한 `signalRecoveryEvidenceLedger[]` 계약을 유지한 채, 신호별 실행 증거 누적 상태를 정량 점검한다.
- 재개 판정은 `RESUME_H024|KEEP_FROZEN` 단일값으로 유지하고, 게이트 4종 + 신호별 증거 누적 커버리지를 함께 근거로 남긴다.
- H-024 동결 해제 전제조건(게이트 충족 + 신호 개선 증거)의 누락 없는 누적/보고 형식을 강화한다.

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
  - `coordination/HANDOFFS/H-032-fallback-warning-keep-frozen-signal-recovery-evidence-acquisition.md`
  - `coordination/REPORTS/H-032-result.md`
  - `coordination/REPORTS/H-032-review.md`
  - `coordination/RELAYS/H-032-review-to-main.md`
  - `docs/code-agent-api.md`의 H-031/H-032 fallback warning 섹션
- 집계 구간:
  - 최신 14일(KST, `today-13 ~ today`) 게이트 판정을 유지한다.
  - 최근 7일(KST, `today-6 ~ today`)과 직전 7일(`today-13 ~ today-7`) 추세를 비교한다.
- `docs/code-agent-api.md`에 H-033 증거 누적 점검 섹션을 추가/갱신한다.
  - 기존 게이트/산식/임계치(`parseEligibleRunCount`, `warningRate`, `dailyCompliance`, `weeklyComplianceRate`, `0.05/0.15/+0.10p/0.10`)는 변경하지 않는다.
  - `resumeDecision(RESUME_H024|KEEP_FROZEN)` 단일 판정 계약을 유지한다.
  - `signalRecoveryEvidenceLedger[]` 필수 필드 계약을 유지한다.
  - 신호별 실행 증거 누적 요약(`evidenceAccumulationSummary[]`)을 추가/갱신한다.
    - 필수 필드: `signal`, `requiredEvidenceCount`, `observedEvidenceCount`, `coverageRate`, `staleEvidenceCount`, `freshEvidenceCount`, `status`, `lastObservedAt`
    - 계산 규칙:
      - `coverageRate = min(1, observedEvidenceCount / requiredEvidenceCount)` (`requiredEvidenceCount=0`이면 `0`)
      - `staleEvidenceCount`: `updatedAt`이 최신 점검 시점 대비 48시간 초과인 증거 개수
      - `freshEvidenceCount = observedEvidenceCount - staleEvidenceCount`
  - 기존 요약 지표(`recoveryActionCompletionRate`, `blockedActionCount`, `latestDecisionReason`)는 유지한다.
  - `KEEP_FROZEN` 판정 시 `unmetReadinessSignals`/`nextCheckTrigger`를, `RESUME_H024` 판정 시 즉시 재개 액션을 명시한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화한다.
  - 기존 출력 계약(`recalibrationReadiness`, `unmetGates`, `resumeDecision`, `unmetReadinessSignals`, `nextCheckTrigger`, `executionRecovery*`)은 유지한다.
  - 신규/보강 출력:
    - `evidenceAccumulationSummary[]` (신호별 required/observed/coverage/stale/fresh/status/lastObservedAt)
  - `KEEP_FROZEN`일 때 `signalRecoveryEvidenceLedger[]`, `evidenceAccumulationSummary[]`, `recoveryActionCompletionRate`, `blockedActionCount` 출력 누락 금지 조건을 유지한다.
- 유지 원칙:
  - `INSUFFICIENT_SAMPLE` 제외 규칙(`parseEligibleRunCount < 20`)을 유지한다.
  - run-state 이벤트/스키마 및 API/CLI 동작 코드는 변경하지 않는다.
  - 코드/설정 파일 수정 없이 운영 문서/템플릿 정합화에 집중한다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-033 증거 누적 점검 섹션과 `evidenceAccumulationSummary[]` 필수 필드/산식이 반영된다.
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 동일 필드 계약으로 동기화된다.
3. 결과 보고에 신호별 `signalRecoveryEvidenceLedger` + `evidenceAccumulationSummary` 근거 표가 포함된다.
4. 최종 판정이 `RESUME_H024` 또는 `KEEP_FROZEN` 중 하나로 명확히 기록된다.
5. 기존 게이트/산식/임계치 및 제외 규칙이 변경되지 않는다.
6. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
7. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 `KEEP_FROZEN` 상태의 증거 누적 점검 계약 정합화 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-033-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-033-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최신 14일 게이트 4개 실측 + PASS/FAIL 표
  - 최근 7일/직전 7일 `executionGapDelta`/`chainShareGapDelta` 비교 표
  - 신호별 `signalRecoveryEvidenceLedger` 표(required/observed/evidenceRefs/status/gapSummary/nextAction/updatedAt)
  - 신호별 `evidenceAccumulationSummary` 표(required/observed/coverage/stale/fresh/status/lastObservedAt)
  - `recoveryActionCompletionRate`/`blockedActionCount` 계산 결과
  - `RESUME_H024/KEEP_FROZEN` 단일 판정 및 근거
  - 다음 점검 트리거(`nextCheckTrigger`) 또는 재개 즉시 액션
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
