# [H-018.1] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-018-1-fallback-warning-operations-doc-alignment.md`
- 참고 result(있으면): `coordination/REPORTS/H-018-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-018-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-018-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-018-1-fallback-warning-operations-doc-alignment.md`
5. `coordination/REPORTS/H-018-result.md`, `coordination/REPORTS/H-018-review.md`, `coordination/RELAYS/H-018-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-018-1-fallback-warning-operations-doc-alignment.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-018-1-result.md`
- `coordination/RELAYS/H-018-1-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - 이번 라운드는 운영 문서 정합화 라운드이며 코드 동작/임계치 수치 변경을 포함하지 않는다.
  - 진행률 산식은 `min(1, 집계 성공 일수 / 10)` 기준으로 문서/템플릿/결과 보고를 단일화한다.
  - 재보정 착수 게이트는 4개(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`)로 통일한다.
- 알려진 리스크:
  - 산식/게이트 기준이 다시 분기되면 라운드 간 지표 비교와 착수 판정 이력이 왜곡될 수 있다.
  - H-018 결과 보고서 정합화가 누락되면 과거 기록 해석이 이중 기준으로 남을 수 있다.
- 리뷰 집중 포인트:
  - `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`가 동일 산식/게이트 계약으로 일치하는지
  - `coordination/REPORTS/H-018-result.md`의 진행률/게이트 문구가 동일 계약으로 보정됐는지
  - 임계치/알림 룰 수치와 `INSUFFICIENT_SAMPLE` 제외 규칙이 변경되지 않았는지
