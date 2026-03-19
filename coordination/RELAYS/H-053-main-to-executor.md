# [H-053] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-053-demo-showcase-walkthrough-packaging.md`
- 참고 close-out result: `coordination/REPORTS/H-052-result.md`
- 참고 close-out review: `coordination/REPORTS/H-052-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-052-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-053-demo-showcase-walkthrough-packaging.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
7. `README.md`, `docs/portfolio-case-study.md`, `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `docs/codex-ops-workflow.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `docs/demo-showcase-walkthrough.md`
  - `README.md` (필요 시 최소 링크 수정만)
  - `docs/portfolio-case-study.md` (필요 시 최소 링크 수정만)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.
- walkthrough는 현재 저장소의 실제 문서/명령/운영 규칙만 사용하고, 실행 결과를 꾸며 쓰지 않는다.

## 완료 산출물
- `coordination/REPORTS/H-053-result.md`
- `coordination/RELAYS/H-053-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- 이번 라운드의 핵심은 새 기능을 추가하는 것이 아니라 "어떤 순서로 보여 주면 되는가"를 문서로 고정하는 것이다. README나 case study를 다시 쓰는 작업으로 번지지 않게 범위를 지켜라.
- CLI 예시는 README / quickstart에 이미 있는 명령을 우선 재사용하고, dry-run 기본값과 `--fail-on-chain-failures` guardrail을 live demo safety 관점에서 분명히 드러내라.
- walkthrough는 결과 스크린샷이나 fabricated sample output보다 "무엇을 확인하면 되는가"를 설명하는 데 집중하라. 예: `runId`, `parsedFiles`, `chainFailures[]`, docs-map/read-next 연결.
- fallback-warning 트랙은 parked 상태다. 운영 규율 사례로 짧게 언급할 수는 있지만, showcase의 대표 흐름이나 next-step처럼 다시 전면화하지 말 것.
