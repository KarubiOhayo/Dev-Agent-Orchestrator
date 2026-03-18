# [H-052] Main -> Executor Relay Prompt

## 라운드 정보
- 대상 handoff: `coordination/HANDOFFS/H-052-readme-portfolio-status-alignment.md`
- 참고 close-out result: `coordination/REPORTS/H-051-result.md`
- 참고 close-out review: `coordination/REPORTS/H-051-review.md`
- 참고 close-out relay: `coordination/RELAYS/H-051-review-to-main.md`
- 참고 status: `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`

## 라운드 시작 입력(재로딩)
1. `AGENTS.md`
2. `docs/PROJECT_OVERVIEW.md`
3. `coordination/TASK_BOARD.md`
4. `coordination/DECISIONS.md`
5. `coordination/HANDOFFS/H-052-readme-portfolio-status-alignment.md`
6. `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`
7. `README.md`, `docs/portfolio-case-study.md`

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
- README 상태 문구는 H-051 result/review가 지적한 불일치를 해소하되, case study를 README 본문으로 흡수하거나 portfolio package 완결을 과장하지 않아야 한다.

## 완료 산출물
- `coordination/REPORTS/H-052-result.md`
- `coordination/RELAYS/H-052-executor-to-review.md`

## 주의/리스크/리뷰 집중 포인트
- 이번 라운드는 README 재작성 라운드가 아니라 status alignment 라운드다. 문제된 current-limits / next-focus 문구만 최소 수정으로 정렬하라.
- `docs/portfolio-case-study.md`가 이미 존재하므로, README는 이를 future work처럼 다시 쓰지 말고 "존재하는 second-layer narrative"로 자연스럽게 연결해야 한다.
- 남은 active roadmap는 demo / showcase walkthrough와 evidence / report export bundle이다. README의 next focus도 이 상태와 어긋나지 않아야 한다.
- fallback-warning 트랙은 parked 상태다. historical concern으로만 제한하고 active roadmap처럼 재등장시키지 말 것.
