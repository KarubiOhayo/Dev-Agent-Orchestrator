# H-050 결과 보고서 (README / project positioning foundation)

## 상태
- 현재 상태: **완료 (root `README.md` 신설 + project positioning / capability snapshot / quickstart / docs map / current limits 반영 + 테스트 통과)**
- 실행일(KST): `2026-03-18`
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-050-readme-project-positioning-foundation.md`
  - main relay: `coordination/RELAYS/H-050-main-to-executor.md`
  - 참고: `docs/PROJECT_OVERVIEW.md`, `coordination/TASK_BOARD.md`, `coordination/DECISIONS.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`, `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/codex-ops-workflow.md`

## 변경 파일 목록
- `README.md`
- `coordination/REPORTS/H-050-result.md`
- `coordination/RELAYS/H-050-executor-to-review.md`

## 구현 요약
- 루트 `README.md`를 새로 작성해 저장소 첫 화면에서 프로젝트 정체성과 현재 동작 범위를 바로 이해할 수 있도록 정리했습니다.
- README 서사는 내부 round/history 중심이 아니라 외부 기여자/평가자가 궁금해할 질문 순서에 맞춰 재구성했습니다.
  - 이것은 무엇인가
  - 왜 필요한가
  - 지금 무엇이 동작하는가
  - 어떻게 실행해볼 수 있는가
  - 어디서 더 읽을 수 있는가
- active roadmap에 맞춰 `portfolio copy / case study / demo / evidence export`를 후속 과제로 남기고, parked 상태인 fallback-warning 트랙은 대표 기능이나 다음 행동처럼 보이지 않게 처리했습니다.
- quickstart는 실제 현재 CLI 표면과 문서 기준을 따르도록 `./devagent help` 출력과 `docs/cli-quickstart.md`를 함께 대조해 맞췄습니다.

## README 섹션별 구성 요약
1. 제목 + positioning
   - Dev-Agent Orchestrator를 "LLM을 개발 파이프라인에 내장하는 orchestration layer"로 정의하고, Spring Boot 기반 실행 표면을 한 문단으로 소개했습니다.
2. Why This Exists
   - 모델 선택, 안전한 apply/dry-run, 체인 실패 전파, run-state 추적이 왜 필요한지 문제 정의 관점으로 풀었습니다.
3. Capability Snapshot
   - 모델 라우팅, Code/Spec/Doc/Review agent, 체이닝, CLI, Prompt/Context/RunState를 현재 구현 기준으로 요약했습니다.
4. How It Works
   - 요청 -> 라우팅 -> 프롬프트/컨텍스트 조합 -> 에이전트 실행 -> 체인 -> run-state 기록 흐름을 간단한 다이어그램과 번호 목록으로 설명했습니다.
5. Quickstart
   - API 키 주입, `chmod +x ./devagent`, `./gradlew clean test --no-daemon`, 최소 `./devagent generate` 예시, Code -> Doc/Review 체인 예시를 넣었습니다.
6. Docs Map
   - `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/codex-ops-workflow.md`, `docs/PROJECT_OVERVIEW.md`로 이어지는 링크를 배치했습니다.
7. Current Limits And Next Focus
   - 외부 공개용 portfolio package 정리 미완료, 의미 품질 평가는 계속 필요, `PARTIAL_SUCCESS` 소비자는 `chainFailures[]` 확인 필요를 명시했습니다.

## 각 핵심 섹션의 근거 source 문서

| README 섹션 | 근거 source |
|---|---|
| 제목 + positioning | `coordination/HANDOFFS/H-050-readme-project-positioning-foundation.md`, `docs/PROJECT_OVERVIEW.md`, `coordination/TASK_BOARD.md` |
| Why This Exists | `docs/PROJECT_OVERVIEW.md`, `docs/codex-ops-workflow.md`, `coordination/DECISIONS.md` |
| Capability Snapshot | `docs/PROJECT_OVERVIEW.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/cli-quickstart.md` |
| How It Works | `docs/PROJECT_OVERVIEW.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md` |
| Quickstart | `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `./devagent help` |
| Docs Map | `coordination/RELAYS/H-050-main-to-executor.md`, 실제 문서 파일 경로 확인 |
| Current Limits And Next Focus | `docs/PROJECT_OVERVIEW.md`, `coordination/TASK_BOARD.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md` |

## quickstart에 포함한 명령 예시와 선택 이유
1. `chmod +x ./devagent`
   - 이유: `docs/cli-quickstart.md`의 가장 기본적인 CLI 진입 준비 단계라서 저장소 첫 실행 경험에 필요합니다.
2. `./gradlew clean test --no-daemon`
   - 이유: handoff 수용 기준과 executor 승인 게이트에 모두 포함된 필수 검증 명령이라 README에서도 바로 보이게 두었습니다.
3. `./devagent generate -p demo-auth -r . -u "로그인 API 스켈레톤을 만들어줘" -m BALANCED -k MEDIUM`
   - 이유: 현재 문서와 CLI 도움말에 모두 존재하는 최소 dry-run 예시라서 "가장 짧게 실행해볼 수 있는 경로"를 보여줍니다.
4. `./devagent generate ... --chain-to-doc true --chain-to-review true --chain-failure-policy PARTIAL_SUCCESS`
   - 이유: 이 저장소의 차별점인 Code -> Doc/Review 원샷 체인을 보여주되, 추가 경로 인자나 spec 산출 경로 설명 없이도 이해 가능한 예시이기 때문입니다.

## 테스트 명령/결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크 및 후속 제안
- `README.md`는 foundation 역할까지는 채웠지만, portfolio copy / case study / demo walkthrough / evidence export는 아직 별도 산출물로 정리되지 않았습니다.
- README 정확성은 현재 문서와 구현 정합성에 기대고 있으므로, 이후 CLI/API 표면이 바뀌면 README도 함께 갱신해야 합니다.
- `HELP.md`는 여전히 Spring 기본 placeholder 상태라서, 외부 첫 방문자의 두 번째 진입 문서까지 정리하려면 후속 라운드가 필요합니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `README.md`의 기능 클레임이 `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/PROJECT_OVERVIEW.md`와 과장 없이 일치하는지
2. quickstart 명령이 `docs/cli-quickstart.md`와 `./devagent help` 출력 기준으로 실제 현재 CLI 옵션명과 맞는지
3. README의 roadmap/limits 서사가 `coordination/TASK_BOARD.md`와 `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`의 active focus와 정합하고, parked fallback-warning 트랙을 전면에 내세우지 않았는지
