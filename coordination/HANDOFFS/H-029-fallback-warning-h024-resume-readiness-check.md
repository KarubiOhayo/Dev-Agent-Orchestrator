# H-029 fallback-warning H-024 동결 트랙 재개 조건 점검

Owner: WT-29 (`codex/h029-fallback-warning-h024-resume-readiness-check`)
Priority: High

## 목표
- H-024 동결(Frozen/Backlog) 유지 근거(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`)를 최신 운영 데이터로 재검증한다.
- 최신 14일/7일 실측 기준으로 H-024 재개 여부를 `RESUME_H024` 또는 `KEEP_FROZEN` 단일 판정으로 고정한다.
- 판정 결과와 근거를 운영 문서/자동화 템플릿에 동기화해 다음 라운드 분기 기준을 명확히 한다.

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
  - `coordination/REPORTS/H-023-result.md`
  - `coordination/REPORTS/H-023-review.md`
  - `coordination/RELAYS/H-023-review-to-main.md`
  - `coordination/REPORTS/H-028-result.md`
  - `coordination/REPORTS/H-028-review.md`
  - `coordination/RELAYS/H-028-review-to-main.md`
  - `docs/code-agent-api.md`의 fallback warning 운영/게이트 섹션
- 집계 구간:
  - 최신 14일(KST, `today-13 ~ today`) 게이트 판정을 유지한다.
  - 최근 7일(KST)은 실행량/체인 커버리지 추세를 별도 표로 보고한다.
- `docs/code-agent-api.md`에 H-029 재개 판단 섹션을 추가/갱신한다.
  - 기존 게이트 계약(집계 성공/`INSUFFICIENT_SAMPLE`/집계 불가/샘플 충분 일수)과 산식은 변경하지 않는다.
  - `dailyCompliance`, `weeklyComplianceRate`, `executionGapDelta`, `chainShareGapDelta` 실측을 최신값으로 갱신한다.
  - 최종 판정(`RESUME_H024` 또는 `KEEP_FROZEN`) 1개만 선택하고, 선택 근거 수치와 미충족 항목을 명시한다.
  - `KEEP_FROZEN`인 경우 재점검 트리거(필수 충족 조건 + 다음 점검 시점)와 보완 액션 우선순위를 명시한다.
  - `RESUME_H024`인 경우 즉시 재개 범위(문서/자동화 동기화 항목)와 수용 기준을 명시한다.
- `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화한다.
  - `resumeDecision(RESUME_H024|KEEP_FROZEN)`, `unmetReadinessSignals`, `nextCheckTrigger`를 필수 출력으로 추가한다.
  - 기존 fallback warning 출력 계약(`recalibrationReadiness`, `unmetGates`, `executionRecovery*`)은 유지한다.
- 유지 원칙:
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않는다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙을 유지한다.
  - run-state 이벤트/스키마 및 API/CLI 동작 코드는 변경하지 않는다.

## 수용 기준
1. `docs/code-agent-api.md`에 H-029 재개 판단 섹션(최신 실측 + 단일 판정 + 근거)이 반영된다.
2. 최종 판정이 `RESUME_H024` 또는 `KEEP_FROZEN` 중 하나로 명확히 기록된다.
3. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 `resumeDecision`/`nextCheckTrigger` 출력 계약으로 동기화된다.
4. 코드/설정(`src/main/**`, `build.gradle`, `settings.gradle`, `application.yml`) 변경이 없다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 임계치/알림 룰 수치 자체 보정
- run-state 이벤트/스키마 변경
- 자동 알림 전송 구현(웹훅/커밋/PR 자동화 포함)

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 이번 라운드는 운영 판정 정합화 라운드이며 코드 동작 변경을 포함하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-029-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-029-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 최신 14일 게이트 4개 실측 + PASS/FAIL 표
  - 최근 7일 실행량/체인 커버리지 추세 표
  - `dailyCompliance`/`weeklyComplianceRate` 최신 계산 결과
  - `executionGapDelta`/`chainShareGapDelta` 재평가 결과
  - `RESUME_H024/KEEP_FROZEN` 단일 판정 및 근거
  - 다음 점검 트리거 또는 재개 즉시 액션 목록
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
