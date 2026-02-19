# [H-017] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-017-fallback-warning-sample-acquisition-plan.md`
- main relay: `coordination/RELAYS/H-017-main-to-executor.md`
- result: `coordination/REPORTS/H-017-result.md`

## 구현 요약
- 핵심 변경:
  - `docs/code-agent-api.md`에 H-017 샘플 확보 계획 섹션 추가(기준선/정량목표/실행안/Projection/분기 규칙)
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 H-017 추적 출력(추세/진행률/Projection/다음 액션) 반영
- 변경 파일:
  - `docs/code-agent-api.md`
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`
  - `coordination/REPORTS/H-017-result.md`
  - `coordination/RELAYS/H-017-executor-to-review.md`

## 테스트 게이트
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: `BUILD SUCCESSFUL`
- 실패/제한 사항: 없음

## 리뷰 집중 포인트
1. `docs/code-agent-api.md`의 H-017 섹션이 handoff 요구(기준선 수치, 정량 목표, 분기 규칙)를 모두 충족하는지
2. `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 목표 대비 진행률/Projection/다음 액션 출력 항목이 명시적으로 반영되었는지
3. 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지

## 알려진 리스크 / 오픈 이슈
- 샘플 확보 계획은 문서화 단계이며, 실제 실행량이 목표에 미달하면 재보정 착수는 계속 보류될 수 있음
- Projection은 최근 모수 추세 유지 가정에 의존하므로 운영 변동 시 착수 예상일이 달라질 수 있음

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-017-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
