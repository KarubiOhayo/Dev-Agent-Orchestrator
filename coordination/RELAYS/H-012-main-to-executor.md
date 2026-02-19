# [H-012] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-012-spec-fallback-warning-observability.md`
- 참고 result(있으면): `coordination/REPORTS/H-011-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-011-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-011-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-012-spec-fallback-warning-observability.md`
5. `coordination/REPORTS/H-011-result.md`, `coordination/REPORTS/H-011-review.md`, `coordination/RELAYS/H-011-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-012-spec-fallback-warning-observability.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-012-result.md`
- `coordination/RELAYS/H-012-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - spec fallback warning 이벤트는 기존 성공 응답 계약을 바꾸지 않는 관측성 보강 범위로만 적용할 것
  - 이벤트명/메시지 형식은 `SPEC_OUTPUT_FALLBACK_WARNING`, `source=<PARSE_SOURCE>`로 고정할 것
- 알려진 리스크:
  - parse source 분기 기준이 모호하면 warning 이벤트 과다/과소 기록으로 운영 지표 해석이 왜곡될 수 있음
  - parser 반환 타입 변경 시 기존 호출부/테스트 누락으로 회귀가 발생할 수 있음
- 리뷰 집중 포인트:
  - direct JSON 경로와 fallback 경로에서 이벤트 기록 여부가 테스트로 명확히 고정되었는지
  - `docs/code-agent-api.md`의 관측성 계약이 구현 이벤트명/조건과 정확히 일치하는지
