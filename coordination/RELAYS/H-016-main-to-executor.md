# [H-016] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-016-fallback-warning-calibration-execution.md`
- 참고 result(있으면): `coordination/REPORTS/H-015-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-015-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-015-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-016-fallback-warning-calibration-execution.md`
5. `coordination/REPORTS/H-015-result.md`, `coordination/REPORTS/H-015-review.md`, `coordination/RELAYS/H-015-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-016-fallback-warning-calibration-execution.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-016-result.md`
- `coordination/RELAYS/H-016-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-016은 보정 `진행/보류`를 먼저 판정하는 라운드다. 게이트 미충족 시 수치 변경 없이 보류 근거를 확정한다.
  - `parseEligibleRunCount < 20`의 `INSUFFICIENT_SAMPLE` 제외 규칙과 이벤트/모수 계약은 유지한다.
- 알려진 리스크:
  - 최근 14일에서 `집계 불가`/`INSUFFICIENT_SAMPLE` 비중이 높으면 보정 결론이 보류될 수 있다.
  - 단기 구간에 맞춘 과도한 임계치 조정은 오탐을 줄이면서 미탐을 키울 수 있다.
- 리뷰 집중 포인트:
  - 가용성 게이트(성공일수/샘플 부족 비율/집계 불가 일수)가 문서화된 기준대로 적용됐는지
  - `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 보정 진행/보류 결과가 일관되게 동기화됐는지
  - 보정 진행 시 후보값과 적용 전/후 영향 비교 근거가 충분한지, 보류 시 근거가 수치로 제시됐는지
