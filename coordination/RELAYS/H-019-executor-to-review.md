# [H-019] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-019-fallback-warning-recalibration-readiness-check.md`
- main relay: `coordination/RELAYS/H-019-main-to-executor.md`
- result: `coordination/REPORTS/H-019-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-019 최신 14일 재점검 섹션을 추가하고, 게이트 4개 판정/진행률 상한/Projection 재산정/`recalibrationReadiness`(`HOLD`)를 수치 기반으로 고정했습니다.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`를 동일 계약으로 동기화해 야간 리포트에 `recalibrationReadiness`와 `unmetGates`를 필수 출력으로 명시했습니다.
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-019-result.md`
  - `coordination/RELAYS/H-019-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 H-019 섹션이 handoff 요구사항(게이트 4개 + 진행률 상한 + 목표 초과 일수 + Projection + READY/HOLD 근거)을 모두 충족하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 출력 계약에 `recalibrationReadiness(READY/HOLD)`와 `unmetGates`가 필수 항목으로 반영됐는지
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지

## 알려진 리스크 / 오픈 이슈
- 최신 14일 실측에서 `INSUFFICIENT_SAMPLE` 비율(`1.00`) 및 샘플 충분 일수(`0일`) 미충족이 지속되어 재보정 착수는 `HOLD` 상태입니다.
- run-state 집계 실패(`집계 불가`)는 없지만, `DOC`/`REVIEW` 모수 부족으로 agent 간 비교 지표 신뢰도는 낮은 상태입니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-019-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
