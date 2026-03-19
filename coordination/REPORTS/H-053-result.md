# H-053 결과 보고서 (demo / showcase walkthrough packaging)

## 상태
- 현재 상태: **완료 (`docs/demo-showcase-walkthrough.md` 신설 + README / case study 최소 연결 및 상태 정렬 + 테스트 통과)**
- 실행일(KST): `2026-03-19`
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-053-demo-showcase-walkthrough-packaging.md`
  - main relay: `coordination/RELAYS/H-053-main-to-executor.md`
  - 참고: `docs/PROJECT_OVERVIEW.md`, `coordination/TASK_BOARD.md`, `coordination/DECISIONS.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`, `README.md`, `docs/portfolio-case-study.md`, `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `docs/codex-ops-workflow.md`, `coordination/REPORTS/H-050-result.md`, `coordination/REPORTS/H-051-result.md`, `coordination/REPORTS/H-052-result.md`, `coordination/REPORTS/H-052-review.md`

## 변경 파일 목록
- `docs/demo-showcase-walkthrough.md`
- `README.md`
- `docs/portfolio-case-study.md`
- `coordination/REPORTS/H-053-result.md`
- `coordination/RELAYS/H-053-executor-to-review.md`

## 구현 요약
- `docs/demo-showcase-walkthrough.md`를 새로 작성해 외부 평가자/협업자/면접관이 어떤 순서로 문서를 읽고 어떤 CLI 명령을 실행하며 무엇을 관찰해야 하는지 한 번에 따라갈 수 있는 guided demo path를 고정했습니다.
- walkthrough는 docs-only 경로와 live CLI 경로를 함께 지원하되, 기본 시연은 dry-run과 guardrail 설명에 두고 `apply=true`나 꾸며낸 결과 예시에 의존하지 않도록 구성했습니다.
- `README.md`에는 walkthrough 링크를 docs map에 추가하고, current-limits 문구를 walkthrough 존재 상태에 맞춰 최소 수정했습니다.
- `docs/portfolio-case-study.md`에는 guided demo path 링크와 남은 packaging 우선순위를 최소 수정으로 반영해 README -> case study -> walkthrough 연결을 만들었습니다.

## walkthrough 섹션별 구성 요약
1. `Demo Goal / Audience / Timebox`
   - 5분 버전과 10~15분 버전으로 나눠, external-facing demo가 무엇을 전달해야 하는지 먼저 고정했습니다.
2. `Before You Start`
   - API 키, repo root, `chmod +x ./devagent`, `./gradlew clean test --no-daemon`, dry-run 기본값, `--fail-on-chain-failures true`, parked fallback-warning 비전면화 규칙을 정리했습니다.
3. `Walkthrough Path A: Entrypoint Reading Order`
   - `README.md` -> `docs/portfolio-case-study.md` -> `docs/cli-quickstart.md` -> `docs/code-agent-api.md` -> `docs/codex-ops-workflow.md` 순서로 읽을 이유와 관찰 포인트를 표로 묶었습니다.
4. `Walkthrough Path B: CLI Demo Flow`
   - dry-run `./devagent generate ...`와 chain-aware `./devagent generate ... --chain-failure-policy PARTIAL_SUCCESS --fail-on-chain-failures true` 예시를 배치하고, API 키가 없을 때는 `./devagent help`로 CLI surface만 보여주는 우회 경로를 넣었습니다.
5. `What To Watch For`
   - `runId`, `model`/route summary, `parsedFiles`, `applyOutcome`, `writtenFiles`, `chainFailures[]`, docs map/read-next 연결을 데모 핵심 신호로 고정했습니다.
6. `Narration Cues`
   - orchestration layer의 의미, request-aware routing, safe defaults, 3-thread/stateless 운영 분리 같은 설명 포인트를 짧게 정리했습니다.
7. `Guardrails / What Not To Claim`
   - hosted product/production traffic 과장 금지, `apply=true` 비기본화, `PARTIAL_SUCCESS` 오해 방지, parked fallback-warning 비전면화, fabricated output 금지를 명시했습니다.
8. `Read Next`
   - walkthrough에서 다시 README/case study/API/ops 문서로 이어지는 follow-up 링크를 정리했습니다.

## 데모 단계별 source 문서 / 명령 매핑

| walkthrough 단계 | source 문서 / 명령 | 선택 이유 |
|---|---|---|
| Demo goal / timebox | `coordination/HANDOFFS/H-053-demo-showcase-walkthrough-packaging.md`, `coordination/TASK_BOARD.md`, `docs/PROJECT_OVERVIEW.md` | external-facing showcase 목적과 현재 active roadmap를 그대로 반영하기 위해 |
| Before you start | `README.md`, `docs/cli-quickstart.md`, `docs/code-agent-api.md` | API 키, CLI 준비, 테스트 게이트, safe defaults가 이미 문서화된 표면이기 때문에 |
| Path A step 1 | `README.md` | 저장소 entrypoint와 docs map을 가장 짧게 보여주기 위해 |
| Path A step 2 | `docs/portfolio-case-study.md` | README보다 깊은 second-layer narrative를 제공하기 위해 |
| Path A step 3 | `docs/cli-quickstart.md` | 실제 CLI 옵션/exit code surface를 보여주기 위해 |
| Path A step 4 | `docs/code-agent-api.md` | `apply=false`, `files[]`, `chainFailures[]`, fallback warning 같은 계약의 근거를 보여주기 위해 |
| Path A step 5 | `docs/codex-ops-workflow.md` | 3-thread/stateless/report-only automation 운영 모델을 마무리 설명하기 위해 |
| Path B dry-run | `README.md`, `docs/cli-quickstart.md`의 `./devagent generate -p demo-auth -r . -u "로그인 API 스켈레톤을 만들어줘" -m BALANCED -k MEDIUM` | 현재 저장소에서 이미 제시된 최소 dry-run 예시라서 |
| Path B chain-aware | `README.md`, `docs/cli-quickstart.md`, `docs/code-agent-api.md`, `./devagent help`의 `--chain-to-doc`, `--chain-to-review`, `--chain-failure-policy`, `--fail-on-chain-failures` | 이 저장소 차별점인 chain contract와 guardrail을 실제 표면 기준으로 보여주기 위해 |
| Path B no-live fallback | `./devagent help` | API 키 없이도 실제 CLI 옵션 표면을 검증 가능한 상태로 보여주기 위해 |

## 관찰 포인트를 선택한 이유와 live demo guardrail
- `runId`
  - 이유: 데모가 일회성 설명이 아니라 추적 가능한 실행 단위라는 점을 바로 보여준다.
- `model` / route summary
  - 이유: request-aware routing이 실제 출력 신호로 드러나는 지점을 보여준다.
- `parsedFiles`
  - 이유: 모델 출력이 단순 텍스트가 아니라 구조화된 파일 초안으로 소비된다는 점을 설명할 수 있다.
- `applyOutcome` / `writtenFiles`
  - 이유: dry-run 기본값과 실제 파일 쓰기 분리가 live demo safety의 핵심이기 때문이다.
- `chainFailures[]`
  - 이유: `PARTIAL_SUCCESS`가 "성공처럼 보이는 실패 은닉"이 아니라 명시적 실패 신호를 남기는 계약이라는 점을 보여준다.
- docs map / read-next links
  - 이유: README -> case study -> walkthrough -> API/ops로 이어지는 portfolio package 완성도를 확인할 수 있다.
- live demo guardrail:
  - `apply=true`를 기본 시연으로 사용하지 않는다.
  - `PARTIAL_SUCCESS` 시연에는 `--fail-on-chain-failures true`를 함께 사용한다.
  - live call이 불안정하면 `./devagent help` + docs-only path로 전환한다.
  - parked fallback-warning 트랙은 governance 사례로만 짧게 언급한다.
  - 출력 값은 미리 꾸며서 설명하지 않고, 실제 필드 존재와 의미만 짚는다.

## README / case study 링크 변경 여부와 이유
- `README.md`
  - 변경 여부: **예**
  - 변경 내용:
    - `Docs Map`에 `Demo / showcase walkthrough` 링크 추가
    - `Current Limits And Next Focus` 첫 bullet을 walkthrough 존재 상태에 맞춰 최소 정렬
  - 이유:
    - entrypoint에서 새 walkthrough를 바로 발견할 수 있어야 하고, walkthrough가 추가된 뒤에도 README 상태 문구가 stale해지지 않아야 했기 때문입니다.
- `docs/portfolio-case-study.md`
  - 변경 여부: **예**
  - 변경 내용:
    - `Current Limits And Next Steps`에 walkthrough 존재 반영
    - `Read Together`에 guided demo path 링크 추가
  - 이유:
    - README보다 깊은 narrative에서 실제 guided demo 문서로 자연스럽게 이어져야 하고, 남은 packaging focus도 evidence export 중심으로 재정렬돼야 했기 때문입니다.

## 테스트 명령/결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크 및 후속 제안
- `evidence / report export bundle`은 아직 정리되지 않아, walkthrough 이후 바로 보여줄 proof package는 후속 라운드가 필요합니다.
- walkthrough는 문서/표면 패키징이므로, 라이브 데모 성공 여부는 API 키 상태와 공급자 응답 안정성에 여전히 영향을 받습니다.
- README, case study, walkthrough는 현재 CLI/API/ops 문서와 정합하지만, 이후 표면이 바뀌면 세 문서를 함께 유지보수해야 합니다.
- 문서 산출물 특성상 narrative 설득력과 과장 여부는 Review 스레드의 source 대조 검증이 중요합니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/demo-showcase-walkthrough.md`의 각 단계가 현재 문서/명령/CLI help와 정합하고, 실행 결과를 꾸며 쓰지 않았는지
2. dry-run 기본값, `--fail-on-chain-failures true`, parked fallback-warning 비전면화 같은 live demo guardrail이 분명히 드러나는지
3. `README.md`와 `docs/portfolio-case-study.md` 수정이 walkthrough 발견성과 상태 정렬을 위한 최소 범위에 머무는지
