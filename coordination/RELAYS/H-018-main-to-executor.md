# [H-018] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-018-fallback-warning-sample-plan-operations-check.md`
- 참고 result(있으면): `coordination/REPORTS/H-017-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-017-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-017-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-018-fallback-warning-sample-plan-operations-check.md`
5. `coordination/REPORTS/H-017-result.md`, `coordination/REPORTS/H-017-review.md`, `coordination/RELAYS/H-017-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-018-fallback-warning-sample-plan-operations-check.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-018-result.md`
- `coordination/RELAYS/H-018-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-018은 임계치 보정 수치 변경 라운드가 아니라 H-017 계획의 운영 적용 점검 라운드다.
  - 최근 14일 실측값, 목표 대비 진행률, Projection 오차를 같은 기준(KST/14일)으로 산출해 비교 가능성을 유지해야 한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 유지한다.
- 알려진 리스크:
  - 최근 3일 평균 기반 Projection은 트래픽 변동 시 오차가 커질 수 있어, 오차 원인 분류와 보완 액션이 누락되면 다음 라운드 판단이 불안정해진다.
  - `DOC`/`REVIEW` 체인 샘플이 낮으면 agent 간 균형 검증 신뢰도가 계속 제한될 수 있다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`에 실측/진행률/Projection 오차/착수 가능·보류 판정이 함께 명시됐는지
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 오차 항목/원인 분류/다음 액션 출력 규칙이 누락 없이 반영됐는지
  - 임계치/알림 룰 수치 및 이벤트/모수 계약이 변경되지 않았는지
