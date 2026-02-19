# [H-010] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-010-api-validation-error-contract.md`
- 참고 result(있으면): `coordination/REPORTS/H-009-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-009-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-009-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. `coordination/HANDOFFS/H-010-api-validation-error-contract.md`
5. `coordination/REPORTS/H-009-result.md`, `coordination/REPORTS/H-009-review.md`, `coordination/RELAYS/H-009-review-to-main.md`

## 작업 범위
- 수정 허용 파일:
  - `coordination/HANDOFFS/H-010-api-validation-error-contract.md`에 명시된 파일
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-010-result.md`
- `coordination/RELAYS/H-010-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
  - 오류 응답 표준화 시 기존 성공 응답 계약(runId/결과 payload/`chainFailures[]`)을 깨지 말 것
  - `PARTIAL_SUCCESS`는 성공 경로이며 실패 응답으로 변환하지 말 것
- 알려진 리스크:
  - 입력검증 로직이 서비스/컨트롤러에 분산되어 누락 가능성이 있음
  - enum/JSON 파싱 실패 응답 메시지가 엔드포인트별로 달라질 수 있음
- 리뷰 집중 포인트:
  - 400/500 오류 envelope 일관성(code/message/path/timestamp/details)과 테스트 고정 여부
  - `docs/code-agent-api.md`의 `chainFailures[]` 필수 확인 규약 반영 여부
