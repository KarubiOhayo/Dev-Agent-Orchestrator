# [H-017] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-017-fallback-warning-sample-acquisition-plan.md`
- 참고 result(있으면): `coordination/REPORTS/H-016-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-016-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-016-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-017-fallback-warning-sample-acquisition-plan.md`
5. `coordination/REPORTS/H-016-result.md`, `coordination/REPORTS/H-016-review.md`, `coordination/RELAYS/H-016-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-017-fallback-warning-sample-acquisition-plan.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-017-result.md`
- `coordination/RELAYS/H-017-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-017은 임계치 보정 라운드가 아니라 재보정 착수용 샘플 확보 계획을 고정하는 라운드다.
  - H-016 기준선(`INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 0일)을 출발점으로 정량 목표/추적 지표를 명확히 제시해야 한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙은 유지한다.
- 알려진 리스크:
  - 샘플 확보 계획이 정량 목표 없이 서술형으로만 작성되면 다음 보정 라운드도 보류될 가능성이 높다.
  - Doc/Review run 표본 부족이 지속되면 agent 간 분포 기반 비교 근거가 약해질 수 있다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`에 기준선 수치, 목표값, 재보정 착수/보류 분기가 함께 명시됐는지
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 목표 대비 진행률/예상치/다음 액션 출력이 누락 없이 반영됐는지
  - 임계치/알림 룰 수치 및 이벤트/모수 계약이 변경되지 않았는지
