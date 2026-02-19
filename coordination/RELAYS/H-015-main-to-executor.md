# [H-015] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-015-fallback-warning-calibration-prep.md`
- 참고 result(있으면): `coordination/REPORTS/H-014-1-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-014-1-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-014-1-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-015-fallback-warning-calibration-prep.md`
5. `coordination/REPORTS/H-014-1-result.md`, `coordination/REPORTS/H-014-1-review.md`, `coordination/RELAYS/H-014-1-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-015-fallback-warning-calibration-prep.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-015-result.md`
- `coordination/RELAYS/H-015-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - 이번 라운드는 임계치 보정 "준비" 단계이며 임계치/알림 수치 변경을 포함하지 않는다.
  - `parseEligibleRunCount` 및 `INSUFFICIENT_SAMPLE` 계약은 H-014.1 기준을 그대로 유지한다.
- 알려진 리스크:
  - 14일 관측 구간에서 샘플 부족(`parseEligibleRunCount < 20`) 일수가 높으면 보정 근거가 약해질 수 있다.
  - 집계 불가 원인 분류가 모호하면 후속 보정 라운드의 입력 품질이 낮아질 수 있다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`에 실측 보정 준비 절차(가용성/집계 불가/후보 수집 기준)가 명확히 추가됐는지
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 14일 가용성 및 샘플 충분성 출력 항목이 반영됐는지
  - 임계치/알림 수치(`0.05`, `0.15`, `0.10`)와 이벤트/모수 계약이 변경되지 않았는지
