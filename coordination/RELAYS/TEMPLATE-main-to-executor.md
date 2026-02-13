# [H-00N] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-00N-*.md`
- 참고 result(있으면): `coordination/REPORTS/H-00N-result.md`
- 참고 review(있으면): `coordination/REPORTS/H-00N-review.md`
- 참고 relay(있으면): `coordination/RELAYS/H-00N-review-to-main.md`

## 라운드 시작 입력(재로딩)
1. `docs/PROJECT_OVERVIEW.md`
2. `coordination/TASK_BOARD.md`
3. `coordination/DECISIONS.md`
4. 대상 `coordination/HANDOFFS/H-00N-*.md`
5. 참고 `coordination/REPORTS/*`, `coordination/RELAYS/*` (해당 라운드)

## 작업 범위
- 수정 허용 파일:
  - handoff에 명시된 파일만
- 수정 금지 파일:
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트 명령: `./gradlew clean test --no-daemon`
- 공통 파일 변경이 필요하면 중단 후 Main 사전 승인 요청만 남긴다.

## 완료 산출물
- `coordination/REPORTS/H-00N-result.md`
- `coordination/RELAYS/H-00N-executor-to-review.md` (템플릿 기반)

## 주의/리스크/리뷰 집중 포인트
- 주의:
- 알려진 리스크:
- 리뷰 집중 포인트:

