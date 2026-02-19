# [H-019] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-019-fallback-warning-recalibration-readiness-check.md`
- 참고 result(있으면): `coordination/REPORTS/H-018-1-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-018-1-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-018-1-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-019-fallback-warning-recalibration-readiness-check.md`
5. `coordination/REPORTS/H-018-1-result.md`, `coordination/REPORTS/H-018-1-review.md`, `coordination/RELAYS/H-018-1-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-019-fallback-warning-recalibration-readiness-check.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-019-result.md`
- `coordination/RELAYS/H-019-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - H-018.1에서 확정된 진행률 산식(`min(1, 집계 성공 일수 / 10)`, `0~100%`)과 `목표 초과 일수` 분리 표기를 반드시 유지한다.
  - 재보정 착수 판정은 4개 게이트(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`)를 단일 기준으로 사용한다.
  - 이번 라운드는 재점검/판정 라운드이며 임계치/알림 룰 수치 변경을 포함하지 않는다.
- 알려진 리스크:
  - 최근 14일 실측에서 `INSUFFICIENT_SAMPLE` 및 샘플 충분 일수 게이트 미충족이 지속될 가능성이 높다.
  - `DOC`/`REVIEW` 모수 부족이 이어지면 agent 간 비교 지표 신뢰도가 낮게 유지될 수 있다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 `READY/HOLD` 판정 계약이 동일한지
  - H-019 결과 보고에서 게이트 4개 PASS/FAIL, 진행률 상한, 목표 초과 일수, Projection 근거가 일관되게 보고됐는지
  - 임계치/알림 룰 수치와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지
