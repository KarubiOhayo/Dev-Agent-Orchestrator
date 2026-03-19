# H-053 demo / showcase walkthrough packaging

Owner: WT-53 (`codex/h053-demo-showcase-walkthrough-packaging`)
Priority: Highest

## 목표
- README entrypoint와 `docs/portfolio-case-study.md` foundation 위에, 외부 평가자/협업자/면접관이 실제로 따라가 볼 수 있는 demo / showcase walkthrough 문서를 만든다.
- 이 저장소를 "무엇을 만들었는가"에서 끝내지 않고, "어떤 순서로 읽고 어떤 명령을 실행하며 무엇을 관찰하면 되는가"까지 한 번에 전달할 수 있게 패키징한다.
- active roadmap와 parked fallback-warning 정책을 유지하면서 portfolio package의 guided demo gap을 줄인다.

## 작업 범위
- 신규/수정 허용:
  - `docs/demo-showcase-walkthrough.md` (신규)
  - `README.md` (필요 시 docs map 또는 read-next 링크 추가 수준의 최소 수정만 허용)
  - `docs/portfolio-case-study.md` (필요 시 read-next 링크 또는 마감 문장 수준의 최소 수정만 허용)
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
  - `docs/model-routing-policy.md`
  - `docs/codex-ops-workflow.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
  - `coordination/REPORTS/H-050-result.md`
  - `coordination/REPORTS/H-051-result.md`
  - `coordination/REPORTS/H-052-result.md`
  - `coordination/REPORTS/H-052-review.md`

## 구현 지침
- walkthrough는 "문서 요약"이 아니라 "이 저장소를 어떻게 데모하면 되는가"를 설명하는 external-facing 실행 가이드여야 한다.
- 문서는 아래 질문에 답할 수 있게 구성한다.
  1. 이 walkthrough는 누구를 위한 것이며, 몇 분 안에 어떤 인상을 주려는가?
  2. 데모를 시작하기 전에 무엇을 준비해야 하는가?
  3. 어떤 순서로 `README.md`, case study, CLI/ops surface를 보여 주면 되는가?
  4. 각 단계에서 무엇을 실행하거나 읽고, 무엇을 관찰해야 하는가?
  5. live demo에서 과장/오해를 피하려면 어떤 guardrail을 지켜야 하는가?
- 권장 섹션:
  1. Demo goal / audience / timebox
  2. Before you start (API keys, repo root, 테스트 게이트, safe defaults)
  3. Walkthrough path A: entrypoint reading order
  4. Walkthrough path B: CLI demo flow (`generate` dry-run + optional chain example)
  5. What to watch for (runId, parsedFiles, chainFailures, docs/read-next 연결)
  6. Narration cues (왜 이 프로젝트가 orchestration layer 관점에서 흥미로운가)
  7. Guardrails / what not to claim
  8. Read next
- 실행 예시는 현재 저장소에서 이미 문서화된 명령만 사용한다. 필요한 경우 명령 자체는 재배열할 수 있지만, 없는 옵션/기능/외부 배포 상태/실사용 지표를 추가하지 않는다.
- 실행 결과는 꾸며 쓰지 말고, "어떤 필드/파일/문서/출력을 확인하면 되는가" 중심의 관찰 포인트로 설명한다.
- safe defaults를 분명히 적는다.
  - Code demo는 기본적으로 dry-run(`apply=false` 기본 동작) 기준으로 설명한다.
  - `PARTIAL_SUCCESS` 시에는 `chainFailures[]`와 `--fail-on-chain-failures` guardrail을 함께 설명한다.
  - fallback-warning parked 트랙은 현재 showcase의 핵심 흐름이 아니며 active roadmap처럼 전면에 두지 않는다.
- `README.md`나 case study를 수정하더라도 링크/이어 읽기 정렬 수준의 최소 수정만 허용한다. README나 case study 본문 전체를 다시 쓰지 않는다.

## 수용 기준
1. `docs/demo-showcase-walkthrough.md`가 생성되고, 외부 평가자가 저장소를 따라 읽고 시연할 수 있는 guided path를 제공한다.
2. walkthrough는 최소 1개의 dry-run CLI 흐름과 1개의 chain-aware 흐름을 포함하거나, chain 흐름을 선택형 데모로 분명히 구분한다.
3. 각 단계가 현재 문서/명령과 정합하며, 실행 결과를 꾸며 쓰지 않고 관찰 포인트(runId, `parsedFiles`, `chainFailures[]`, docs links 등) 중심으로 설명한다.
4. safe defaults(`apply=false`, guardrail opt-in, parked fallback-warning 비전면화)가 walkthrough에 드러난다.
5. `README.md` 또는 `docs/portfolio-case-study.md`가 수정될 경우 demo walkthrough로 이어지는 최소 링크 수준에 머문다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- evidence / report export bundle 제작
- README / case study 전면 재작성
- 코드/설정 변경
- fallback-warning parked 트랙 재개
- 외부 배포, hosted demo, 실제 운영 수치 추가

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- walkthrough는 현재 저장소에서 재현 가능한 명령/문서/관찰 포인트만 사용한다
- demo narrative는 portfolio 과장을 피하고, "무엇이 이미 있고 무엇이 아직 남았는가"를 분리해서 쓴다

## 보고서
- 완료 시 `coordination/REPORTS/H-053-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-053-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - walkthrough 섹션별 구성 요약
  - 각 데모 단계가 참조한 source 문서/명령 매핑
  - 관찰 포인트를 선택한 이유와 live demo guardrail
  - README / case study 링크 변경 여부와 이유
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안(`evidence export`, `polishing` 관점)
