# [H-051] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-051-portfolio-copy-case-study-foundation.md`
- 참고 close-out result: `coordination/REPORTS/H-050-result.md`
- 참고 close-out review: `coordination/REPORTS/H-050-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-050-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-051-portfolio-copy-case-study-foundation.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`
7. `README.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/codex-ops-workflow.md`

## 작업 범위
- 수정/추가 허용 파일(핸드오프 기준):
  - `docs/portfolio-case-study.md`
  - `README.md` (case study 링크 추가가 필요할 때만)
- 수정 금지(공통 파일):
  - `src/main/resources/application.yml`
  - `build.gradle`, `settings.gradle`, `gradle/wrapper/**`
  - handoff 범위 밖 파일

## 승인 게이트
- 필수 테스트: `./gradlew clean test --no-daemon`
- 공통 파일 변경 필요 시: 즉시 중단하고 Main 승인 요청만 남긴다.
- case study의 기능/운영 클레임은 현재 저장소 문서/구현 기준과 일치해야 한다.

## 완료 산출물
- `coordination/REPORTS/H-051-result.md`
- `coordination/RELAYS/H-051-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- 이번 라운드의 핵심은 README를 다시 쓰는 것이 아니라, README 위에 놓이는 second-layer narrative를 만드는 것이다. entrypoint와 case study 역할을 섞지 말 것.
- portfolio 문서라고 해서 없는 배포 이력, 성능 수치, 외부 채택 사례를 보강해서 쓰지 말 것. 모든 클레임은 현재 문서와 코드 표면으로 역추적 가능해야 한다.
- 3-thread / stateless / relay 운영 방식은 내부 프로세스 자랑이 아니라 "자동화 작업을 어떻게 통제했는가"를 보여주는 설계 선택으로 설명하라.
- fallback-warning 트랙은 parked 상태다. 운영 규율 사례로 짧게 언급할 수는 있지만, current focus나 대표 기능처럼 전면에 세우지 말 것.
- `README.md`를 건드릴 경우 docs map 또는 read-next 링크 정도의 최소 수정으로 제한하고, case study 본문은 `docs/portfolio-case-study.md`에 집중하라.
