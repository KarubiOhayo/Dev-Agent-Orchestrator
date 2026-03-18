# [H-050] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-050-readme-project-positioning-foundation.md`
- main relay: `coordination/RELAYS/H-050-main-to-executor.md`
- result: `coordination/REPORTS/H-050-result.md`

## 구현 요약
- 핵심 변경:
  - root `README.md`를 새로 작성해 project positioning, capability snapshot, 동작 흐름, quickstart, docs map, current limits를 한 장으로 정리했습니다.
  - quickstart 명령은 `docs/cli-quickstart.md`와 `./devagent help` 출력 기준으로 현재 CLI 옵션명에 맞춰 정리했습니다.
  - README의 limitations/roadmap는 active focus에 맞췄고, parked fallback-warning 트랙이 대표 기능이나 next step처럼 보이지 않게 조정했습니다.
- 변경 파일:
  - `README.md`
  - `coordination/REPORTS/H-050-result.md`
  - `coordination/RELAYS/H-050-executor-to-review.md`

## 테스트 게이트
- 실행 명령:
  - `./gradlew clean test --no-daemon`
- 결과:
  - `BUILD SUCCESSFUL`
- 실패/제한 사항:
  - 테스트 실패 없음
  - README 내용 자체를 기계적으로 검증하는 테스트는 없음

## 리뷰 집중 포인트
1. `README.md`의 기능/아키텍처/운영 클레임이 현재 문서 근거(`docs/PROJECT_OVERVIEW.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`)와 정확히 일치하는지
2. quickstart에 적은 명령(`chmod +x ./devagent`, `./gradlew clean test --no-daemon`, `./devagent generate ...`, 체인 예시)이 `docs/cli-quickstart.md` 및 실제 `./devagent help` 표면과 맞는지
3. README의 roadmap/limits가 `coordination/TASK_BOARD.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`와 정합하고, parked fallback-warning 트랙을 현재 핵심 진행축처럼 보이게 하지 않았는지

## 알려진 리스크 / 오픈 이슈
- README는 외부 첫 화면 이해도를 크게 개선하지만, portfolio copy / case study / demo / evidence export는 아직 별도 후속 작업이 필요합니다.
- CLI/API 표면이 바뀌면 README quickstart와 capability snapshot도 함께 유지보수해야 합니다.
- `HELP.md`는 이번 범위 밖이라 여전히 placeholder 상태입니다.

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-050-review.md` 작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
