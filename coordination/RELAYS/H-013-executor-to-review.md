# [H-013] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-013-fallback-warning-runstate-baseline.md`
- main relay: `coordination/RELAYS/H-013-main-to-executor.md`
- result: `coordination/REPORTS/H-013-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 fallback warning run-state 집계 기준(이벤트/모수/경고율/임계치/알림 룰) 명시
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 동일 기준 기반 점검/보고 항목 추가
  - `docs/PROJECT_OVERVIEW.md`에 H-013 반영(운영 리스크/다음 우선순위 정합화)
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/REPORTS/H-013-result.md`
  - `coordination/RELAYS/H-013-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 `parseEligibleRunCount` 정의/최소 샘플/임계치/알림 룰이 이벤트 계약(`*_OUTPUT_FALLBACK_WARNING`)과 충돌 없이 해석 가능한지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 Plan A 제약을 유지하면서도 필수 출력(기간/이벤트별 건수/agent별 경고율/임계치 판정/후속조치)을 빠짐없이 요구하는지
3. `docs/PROJECT_OVERVIEW.md`의 운영 리스크/다음 우선순위가 H-013 문서화 결과와 정합한지

## 알려진 리스크 / 오픈 이슈
- 임계치/알림 룰은 초기 기준값이므로 운영 데이터 축적 후 재보정 필요
- run-state 데이터 접근 경로가 환경별로 다를 수 있어 자동 점검 실행 환경 표준화 필요

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-013-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
