# [H-016] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-016-fallback-warning-calibration-execution.md`
- main relay: `coordination/RELAYS/H-016-main-to-executor.md`
- result: `coordination/REPORTS/H-016-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 `실측 보정 실행 결과 (H-016)` 섹션 추가
    - 집계 구간 고정: `2026-02-06 ~ 2026-02-19` (KST 14일)
    - 게이트 실측값: 성공 `14일`, `INSUFFICIENT_SAMPLE` `14일/1.00`, 집계 불가 `0일`
    - 최종 판정: `보정 보류`, 임계치/알림 수치 유지(`0.05`, `0.15`, `+0.10p`, `0.10`)
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 보강
    - H-016 보정 실행 게이트(진행 조건 3개) 명시
    - 미충족 시 보류/수치 유지, 충족 시 후보값 + 전/후 비교 출력 분기 추가
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-016-result.md`
  - `coordination/RELAYS/H-016-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. H-016 실측 수치(14일 게이트/agent별 누적/보류 근거)가 `coordination/REPORTS/H-016-result.md`와 `docs/code-agent-api.md` 간 일치하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 진행/보류 분기 규칙과 보고 항목(전/후 비교 또는 보류 사유)이 handoff 요구사항을 충족하는지
3. 게이트 미충족 상황에서 임계치/알림 수치 및 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지

## 알려진 리스크 / 오픈 이슈
- 최근 14일 샘플 부족 비율이 `1.00`이라 보정 판단이 보류되었고, 동일 패턴이 지속되면 다음 라운드에서도 보류 가능성이 높습니다.
- Doc/Review run 표본이 0이라 agent 간 분포 기반 후보값 검증이 불가능합니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-016-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
