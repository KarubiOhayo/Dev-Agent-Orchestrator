# [H-050] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-050-readme-project-positioning-foundation.md`
- 참고 close-out result: `coordination/REPORTS/H-048-result.md`
- 참고 close-out review: `coordination/REPORTS/H-048-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-048-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-050-readme-project-positioning-foundation.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`
7. `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/codex-ops-workflow.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `README.md`
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.
- README의 기능/예시/링크는 현재 구현/문서 기준과 일치해야 한다.

## 완료 산출물
- `coordination/REPORTS/H-050-result.md`
- `coordination/RELAYS/H-050-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- 루트 `README.md`가 현재 비어 있으므로, 이번 라운드의 핵심은 "첫 화면 이해도"다. 기능 목록 나열보다 positioning, 사용 흐름, 문서 맵을 먼저 잡아라.
- README의 모든 기능 클레임은 현재 저장소 문서/구현으로 역추적 가능해야 한다. 없는 배포/운영 자동화/외부 사례를 추정해 쓰지 말 것.
- `fallback-warning` 트랙은 parked 상태다. historical/운영 리스크로 짧게 언급할 수는 있지만, active roadmap나 대표 기능처럼 전면에 배치하지 말 것.
- quickstart 예시는 실제 현재 CLI 옵션/문서와 동일해야 한다. `docs/cli-quickstart.md`와 어긋나는 명령을 새로 invent하지 말 것.
- `HELP.md`는 Spring 기본 placeholder지만 이번 라운드 범위는 아니다. README 작성에 집중하고 `HELP.md` 정리는 후속 제안으로만 남겨라.
