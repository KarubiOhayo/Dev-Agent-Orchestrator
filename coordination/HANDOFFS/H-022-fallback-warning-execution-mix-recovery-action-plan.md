# H-022 fallback warning 실행량 회복 액션 플랜 수립/운영 점검

Owner: WT-22 (`codex/h022-fallback-warning-execution-mix-recovery-action-plan`)
Priority: Highest

## 목표
- H-021에서 분리한 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 근거를 실행 가능한 일일 액션 플랜으로 전환한다.
- 최근 7일 기준 agent별 목표-실적 gap(`directRuns`, `chainRuns`, `totalActualRuns`, `chainShare`)을 고정해 회복 진행률을 추적한다.
- 최신 14일 게이트 판정과 실행량 회복 진행률을 함께 보고해 다음 라운드 `READY/HOLD` 판단 입력을 강화한다.

## 작업 범위
- 운영 문서 갱신
  - `docs/code-agent-api.md`
- 야간 점검 템플릿 정합화
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
- 상태 문서 최소 동기화(필요 시)
  - `docs/PROJECT_OVERVIEW.md`

## 구현 지침
- 기준 입력:
  - `coordination/REPORTS/H-021-result.md`
  - `coordination/REPORTS/H-021-review.md`
  - `coordination/RELAYS/H-021-review-to-main.md`
  - `docs/code-agent-api.md`의 H-021 호출 믹스 추적 섹션
- 집계 구간:
  - 최신 14일(KST, `today-13 ~ today`) 게이트 판정을 유지한다.
  - 최근 7일(KST)은 실행량 회복 계획 대비 실적(gap)으로 별도 보고한다.
- `docs/code-agent-api.md`에 H-022 실행량 회복 액션 플랜 섹션을 추가/갱신한다.
  - 기존 4개 게이트(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`) 판정은 유지한다.
  - 최근 7일 agent별로 `targetDirectRuns`, `targetChainRuns`, `targetTotalRuns`, `targetChainShare`와 실적 대비 `gap`을 표로 보고한다.
    - `targetTotalRuns = targetDirectRuns + targetChainRuns`
    - `actualTotalRuns = directRuns + chainRuns`
    - `executionGap = targetTotalRuns - actualTotalRuns`
    - `chainShareGap = targetChainShare - actualChainShare`
  - `LOW_TRAFFIC`는 `executionGap` 중심으로, `CHAIN_COVERAGE_GAP`는 `chainShareGap`과 `DOC/REVIEW chainRuns` 중심으로 분리 판정한다.
  - `HOLD`가 지속될 경우 일일 우선 액션(직접 호출 증량, 체인 호출 증량, 점검 시각/담당)을 명시한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화한다.
  - 기존 출력(`READY/HOLD`, `unmetGates`, `agentExecution`, `executionMix`, `overallExecutionRate`)을 유지한다.
  - `executionRecoveryPlan`(`targetDirectRuns`, `targetChainRuns`, `targetChainShare`)과 `executionRecoveryProgress`(`actualDirectRuns`, `actualChainRuns`, `executionGap`, `chainShareGap`)를 필수 출력으로 추가한다.
  - `HOLD`일 때는 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 원인별 보완 액션을 목표-실적 gap 근거와 함께 제시한다.
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
  - 이벤트 정의(`*_OUTPUT_FALLBACK_WARNING`)와 모수 정의(직접 호출 + 체인 호출 포함)는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-022 실행량 회복 액션 플랜 섹션(최근 7일 목표-실적 gap + 14일 게이트 판정 + `READY/HOLD` 근거)이 반영된다.
2. 실행량 회복 산식(`targetTotalRuns`, `actualTotalRuns`, `executionGap`, `chainShareGap`)이 문서에 명시된다.
3. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 동일 출력 계약(`executionMix`, `executionRecoveryPlan`, `executionRecoveryProgress`)으로 정렬된다.
4. `HOLD` 시 원인 분류/다음 액션이 목표-실적 gap 근거와 함께 보고된다.
5. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
6. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 실행량 회복 액션 플랜 고정 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-022-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-022-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최신 14일 게이트 4개 실측 + PASS/FAIL 표
  - 최근 7일 agent별 목표-실적(`targetDirectRuns`, `targetChainRuns`, `directRuns`, `chainRuns`, `executionGap`, `chainShareGap`) 표
  - `overallExecutionRate` 추세 + `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 근거
  - Projection 재산정 결과(`requiredSufficientDays`, `예상 착수 가능일` 또는 미산정 사유)
  - `READY/HOLD` 최종 판정 및 근거
  - `HOLD` 시 원인 분류/보완 액션 우선순위
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
