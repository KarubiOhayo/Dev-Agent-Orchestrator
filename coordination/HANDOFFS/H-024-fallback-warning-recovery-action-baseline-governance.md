# H-024 fallback warning 실행량 회복 액션 최소 이행률 하한선/증거 규약 고정

Owner: WT-24 (`codex/h024-fallback-warning-recovery-action-baseline-governance`)
Priority: Highest

## 목표
- H-023에서 확인된 미개선 상태(`executionGapDelta=+3`, `chainShareGapDelta=0.00%p`)를 기준으로, `HOLD` 해소 판단에 사용할 일일 최소 이행률 하한선을 고정한다.
- 최근 7일 일자별 하한선 충족 여부(PASS/FAIL)와 주간 이행률 단계를 표준화해 회복 진행 상태를 정량 판정한다.
- `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 원인별 실행 증거(runId/집계표)를 ledger 형식으로 고정해 운영 보고 재현성을 확보한다.

## 작업 범위
- 운영 문서 갱신
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-023-result.md`
  - `coordination/REPORTS/H-023-review.md`
  - `coordination/RELAYS/H-023-review-to-main.md`
  - `docs/code-agent-api.md`의 H-023 실행량 회복 이행률 추적 섹션
- 집계 구간:
  - 최신 14일(KST, `today-13 ~ today`) 게이트 판정을 유지한다.
  - 최근 7일(KST)은 일일 하한선 PASS/FAIL + 주간 이행률 단계로 별도 보고한다.
- `docs/code-agent-api.md`에 H-024 최소 이행률 하한선/증거 ledger 섹션을 추가/갱신한다.
  - 기존 출력 계약(`executionRecoveryPlan`, `executionRecoveryProgress`, `executionRecoveryTrend`, `recoveryActionStatus`)은 유지한다.
  - `dailyExecutionBaseline`(agent별 일일 최소 하한선)을 아래 기준으로 고정한다.
    - CODE: `minimumDailyDirectRuns=3`, `minimumDailyChainRuns=1`, `minimumDailyTotalRuns=4`
    - SPEC: `minimumDailyDirectRuns=1`, `minimumDailyChainRuns=0`, `minimumDailyTotalRuns=1`
    - DOC: `minimumDailyDirectRuns=0`, `minimumDailyChainRuns=2`, `minimumDailyTotalRuns=2`
    - REVIEW: `minimumDailyDirectRuns=0`, `minimumDailyChainRuns=2`, `minimumDailyTotalRuns=2`
    - 전체: `minimumDailyTotalRuns=8`, `minimumDailyChainRuns=4`
  - 최근 7일 일자별 `dailyCompliance`를 고정한다.
    - `dailyCompliance = PASS` if `actualTotalRuns >= minimumDailyTotalRuns` and `actualChainRuns >= minimumDailyChainRuns`
    - 미충족 시 `FAIL`
  - `weeklyComplianceRate = compliantDays / 7`를 산출하고 단계 분류를 고정한다.
    - `ON_TRACK`: `>= 0.70`
    - `AT_RISK`: `>= 0.40`, `< 0.70`
    - `OFF_TRACK`: `< 0.40`
  - `HOLD` 지속 시 `recoveryEvidenceLedger`를 원인별(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`, `COLLECTION_FAILURE`)로 고정한다.
    - 필수 필드: `cause`, `priority`, `status`, `evidenceType`, `evidenceRef`, `owner`, `updatedAt`
    - `evidenceType`은 `runId`, `aggregateTable` 중 하나로 고정한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화한다.
  - 기존 출력 계약은 유지한다.
  - `dailyExecutionBaseline`, `dailyCompliance`, `weeklyCompliance`, `recoveryEvidenceLedger`를 필수 출력으로 추가한다.
  - `HOLD` 시 액션 상태(`IN_PROGRESS|BLOCKED|DONE`)와 증거(runId/집계표)를 누락 없이 출력한다.
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-024 최소 이행률 하한선/증거 ledger 섹션(일일 하한선 + 7일 PASS/FAIL + 주간 단계 + `READY/HOLD` 근거)이 반영된다.
2. 일일 하한선/주간 이행률 산식(`dailyCompliance`, `weeklyComplianceRate`)과 단계 규칙(`ON_TRACK`/`AT_RISK`/`OFF_TRACK`)이 문서에 명시된다.
3. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 동일 출력 계약(`dailyExecutionBaseline`, `dailyCompliance`, `weeklyCompliance`, `recoveryEvidenceLedger`)으로 정렬된다.
4. `HOLD` 시 원인별 상태(`IN_PROGRESS|BLOCKED|DONE`)와 증거(runId/집계표)가 ledger 형태로 보고된다.
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 운영 지표/증거 규약 고정 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-024-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-024-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최신 14일 게이트 4개 실측 + PASS/FAIL 표
  - `dailyExecutionBaseline` 표(agent별 최소 하한선)
  - 최근 7일 `dailyCompliance`(일자별 PASS/FAIL + 근거값) 표
  - `weeklyComplianceRate` 및 단계(`ON_TRACK`/`AT_RISK`/`OFF_TRACK`) 계산 결과
  - 최근 7일 vs 직전 7일 `executionGapDelta`/`chainShareGapDelta` 재확인 표
  - `recoveryEvidenceLedger`(원인별 상태 + evidenceType + evidenceRef + updatedAt) 표
  - Projection 재산정 결과(`requiredSufficientDays`, `예상 착수 가능일` 또는 미산정 사유)
  - `READY/HOLD` 최종 판정 및 근거
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
