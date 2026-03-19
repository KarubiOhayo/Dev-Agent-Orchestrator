# Evidence / Report Export Bundle

## Bundle Goal / Audience / When To Send

- 대상:
  - `README.md` -> case study -> walkthrough를 읽었거나 live demo를 본 뒤, "무엇을 더 보면 이 저장소의 실제 증거가 되는가?"를 확인하려는 협업자, 면접관, 외부 평가자
- 보내는 시점:
  - docs-only walkthrough 직후
  - live demo 직후 follow-up 메일/메시지
  - 비동기 공유에서 저장소 전체를 읽기 전에 curated proof package가 필요할 때
- 목표:
  - 저장소 전체 history를 덤프하는 것이 아니라, 현재 narrative를 뒷받침하는 source 문서/결과 보고/리뷰/릴레이를 어떤 순서와 기준으로 건네야 하는지 고정한다
  - 존재하지 않는 실적, 외부 배포 상태, fabricated output, 새 metrics 없이 현재 저장소에 이미 있는 근거만 사용한다

## Bundle Contents Overview

이 bundle은 아래 4개 층으로 나눈다.

| Layer | 기본 포함 항목 | 이 층이 증명하는 것 |
|---|---|---|
| narrative docs | [`README.md`](../README.md), [`docs/portfolio-case-study.md`](./portfolio-case-study.md), 이 문서 | 저장소가 무엇이고 왜 orchestration layer 관점에서 의미가 있는지 |
| demo companion | [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md), [`docs/cli-quickstart.md`](./cli-quickstart.md), [`docs/code-agent-api.md`](./code-agent-api.md) | 어떤 순서로 읽고 어떤 명령/계약/guardrail을 확인하면 되는지 |
| proof artifacts | `H-050`~`H-053` result/review/relay 일부 | README, case study, walkthrough가 즉흥 copy가 아니라 실제 source 대조와 리뷰를 거쳐 만들어졌는지 |
| ops/governance evidence | [`docs/codex-ops-workflow.md`](./codex-ops-workflow.md), [`docs/PROJECT_OVERVIEW.md`](./PROJECT_OVERVIEW.md), `coordination` SoT 일부 | 3-thread, stateless round, report-only automation, active roadmap/limits가 어떻게 관리되는지 |

기본 외부 공유 세트는 보통 아래 4개면 충분하다.

1. 이 문서
2. [`README.md`](../README.md)
3. [`docs/portfolio-case-study.md`](./portfolio-case-study.md)
4. [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md)

기술 검토가 더 깊어지면 demo companion을 추가하고, "이 서사가 실제 검토/운영 근거를 거쳤는가?"라는 질문이 나오면 proof artifacts와 ops/governance evidence를 선택 첨부한다.

## Source-Of-Truth Mapping

shareability note는 아래처럼 해석한다.

- `External default`: 기본 외부 공유 세트에 포함해도 된다. 그래도 비밀값/개인식별 정보/로컬 경로는 마지막으로 한 번 더 점검한다.
- `External selective`: 기술 평가자가 명령/API/운영 표면을 더 보고 싶을 때 추가한다.
- `Internal-first / excerpt`: 먼저 요약해서 설명하고, 상대가 audit trail을 원할 때만 전체 파일이나 필요한 일부를 보낸다.
- `Internal only unless governance review`: 거버넌스 검토나 프로세스 질문이 있을 때만 제한적으로 꺼낸다.

| Bundle item | Source file | What it proves | Shareability note |
|---|---|---|---|
| Project entrypoint | [`README.md`](../README.md) | 이 저장소가 무엇을 하는지, 현재 capability/quickstart/docs map이 무엇인지 | External default |
| Second-layer narrative | [`docs/portfolio-case-study.md`](./portfolio-case-study.md) | 왜 orchestration layer가 필요한지, 복잡성을 어떻게 통제하는지, 어떤 limits가 남아 있는지 | External default |
| Guided demo path | [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md) | demo를 어떤 순서로 진행하고 무엇을 관찰해야 하는지, 과장 없이 무엇을 말해야 하는지 | External default |
| CLI surface and guardrail examples | [`docs/cli-quickstart.md`](./cli-quickstart.md) | dry-run/apply, `PARTIAL_SUCCESS`, `--fail-on-chain-failures`, JSON output 같은 실제 CLI 표면이 존재함 | External selective; 기본은 `generate/spec`, guardrail, JSON 출력 섹션 위주로 공유하고, historical seeding appendix는 요청이 있을 때만 보여 준다 |
| API contract and safety surface | [`docs/code-agent-api.md`](./code-agent-api.md) | `apply=false`, `files[]`, `chainFailures[]`, fallback warning 이벤트, 공통 오류 계약 같은 API 레벨 guardrail이 문서화돼 있음 | External selective; overview, response fields, chain failure policy 위주로 안내하고 parked fallback-warning history를 대표 proof처럼 전면화하지 않는다 |
| Operating model | [`docs/codex-ops-workflow.md`](./codex-ops-workflow.md) | Main/Executor/Review, stateless rounds, relay discipline, report-only automation 정책이 존재함 | External selective |
| Current implementation/risk snapshot | [`docs/PROJECT_OVERVIEW.md`](./PROJECT_OVERVIEW.md), [`coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`](../coordination/REPORTS/CURRENT_STATUS_2026-03-19.md) | 현재 구현 범위, active focus, 남은 리스크가 무엇인지 | Internal-first / excerpt; 날짜를 함께 보여 주고 stale 여부를 확인한다 |
| Roadmap and decision discipline | [`coordination/TASK_BOARD.md`](../coordination/TASK_BOARD.md), [`coordination/DECISIONS.md`](../coordination/DECISIONS.md) | active roadmap, parked policy, 중요한 결정이 SoT 문서로 관리된다는 점 | Internal only unless governance review |
| README packaging foundation proof | [`coordination/REPORTS/H-050-result.md`](../coordination/REPORTS/H-050-result.md) | README positioning/quickstart/docs map이 어떤 source 문서를 근거로 정리됐는지 | Internal-first / excerpt |
| Case study packaging proof | [`coordination/REPORTS/H-051-result.md`](../coordination/REPORTS/H-051-result.md), [`coordination/REPORTS/H-051-review.md`](../coordination/REPORTS/H-051-review.md) | case study narrative가 source 문서에 맞춰 작성됐고, overclaim/정합성 관점 리뷰를 거쳤는지 | Internal-first / excerpt |
| README status alignment proof | [`coordination/REPORTS/H-052-result.md`](../coordination/REPORTS/H-052-result.md), [`coordination/REPORTS/H-052-review.md`](../coordination/REPORTS/H-052-review.md) | external-facing entrypoint의 현재 상태 문구가 실제 active roadmap와 맞춰졌는지 | Internal-first / excerpt |
| Walkthrough packaging proof | [`coordination/REPORTS/H-053-result.md`](../coordination/REPORTS/H-053-result.md), [`coordination/REPORTS/H-053-review.md`](../coordination/REPORTS/H-053-review.md) | walkthrough의 명령/guardrail/관찰 포인트가 실제 README/CLI/API surface와 맞는지 | Internal-first / excerpt |
| Review-to-approval handoff proof | [`coordination/RELAYS/H-053-review-to-main.md`](../coordination/RELAYS/H-053-review-to-main.md) | review 결과가 Main 판단 입력으로 전달되는 governance 루프가 실제로 동작함 | Internal only unless governance review |

## Suggested Export Order / Folder Layout

실제 repo에 export 폴더를 새로 만들 필요는 없다. 아래는 외부 전달용 묶음을 준비할 때의 권장 순서다.

1. `starter set`
   - 이 문서
   - `README.md`
   - `docs/portfolio-case-study.md`
   - `docs/demo-showcase-walkthrough.md`
2. `technical deep-dive add-on`
   - `docs/cli-quickstart.md`
   - `docs/code-agent-api.md`
   - `docs/codex-ops-workflow.md`
3. `audit trail add-on`
   - `coordination/REPORTS/H-050-result.md`
   - `coordination/REPORTS/H-051-result.md`
   - `coordination/REPORTS/H-051-review.md`
   - `coordination/REPORTS/H-052-result.md`
   - `coordination/REPORTS/H-052-review.md`
   - `coordination/REPORTS/H-053-result.md`
   - `coordination/REPORTS/H-053-review.md`
   - `coordination/RELAYS/H-053-review-to-main.md`
4. `governance add-on`
   - `docs/PROJECT_OVERVIEW.md`
   - `coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`
   - `coordination/TASK_BOARD.md`
   - `coordination/DECISIONS.md`

폴더 레이아웃으로 옮겨 담는다면 아래 정도의 구조면 충분하다.

```text
evidence-bundle/
  00-cover/
    README.md
    docs/evidence-report-export-bundle.md
  01-narrative/
    docs/portfolio-case-study.md
  02-demo-companion/
    docs/demo-showcase-walkthrough.md
    docs/cli-quickstart.md
    docs/code-agent-api.md
  03-proof-artifacts/
    coordination/REPORTS/H-050-result.md
    coordination/REPORTS/H-051-result.md
    coordination/REPORTS/H-051-review.md
    coordination/REPORTS/H-052-result.md
    coordination/REPORTS/H-052-review.md
    coordination/REPORTS/H-053-result.md
    coordination/REPORTS/H-053-review.md
    coordination/RELAYS/H-053-review-to-main.md
  04-governance/
    docs/codex-ops-workflow.md
    docs/PROJECT_OVERVIEW.md
    coordination/REPORTS/CURRENT_STATUS_2026-03-19.md
    coordination/TASK_BOARD.md
    coordination/DECISIONS.md
```

핵심은 "파일을 많이 보내는 것"이 아니라, starter set으로 설명을 끝내고 필요한 질문에 맞춰 deep-dive와 audit trail을 덧붙이는 순서를 지키는 것이다.

## Guardrails / Redaction / What Not To Include

- 비밀값, 토큰, `.env` 내용, 개인 이메일/전화번호 같은 민감 정보는 당연히 제외한다.
- ad-hoc 메모, shell transcript, screenshot를 함께 보낼 때는 로컬 사용자명, 절대경로, 다른 프로젝트 이름이 섞이지 않았는지 확인한다.
- `coordination/` 전체를 통째로 보내지 않는다. 이 문서의 mapping table에 올라온 파일만 선별한다.
- live demo를 실제로 실행하지 않았다면, "문서 기반 walkthrough와 계약 문서를 공유한다"라고 말하지 "방금 실행해 확인했다"라고 말하지 않는다.
- hosted product, production traffic, 외부 사용자 수, 배포 완료 상태, fabricated benchmark, 꾸며낸 screenshot/output은 포함하지 않는다.
- parked 상태인 fallback-warning 트랙은 default bundle content가 아니다. 필요하면 "historical observability governance example"로만 짧게 언급하고 대표 줄거리로 올리지 않는다.
- [`docs/cli-quickstart.md`](./cli-quickstart.md)와 [`docs/code-agent-api.md`](./code-agent-api.md)의 긴 historical appendix를 첫 첨부물처럼 전면에 두지 않는다. 기본은 current CLI/API surface와 guardrail 섹션 위주다.
- result/review 문서는 날짜와 라운드 번호를 숨기지 않는다. 특정 시점의 증빙이라는 사실을 그대로 보여 주는 편이 정직하다.

## How To Use After The Walkthrough

1. walkthrough 중에는 [`README.md`](../README.md) -> [`docs/portfolio-case-study.md`](./portfolio-case-study.md) -> [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md) 흐름으로 설명한다.
2. 세션 직후에는 starter set만 먼저 보낸다. 이때 이 문서를 cover note처럼 사용해 "무엇을 먼저 읽고, 어떤 문서는 요청 시 추가되는지"를 알려 준다.
3. 상대가 재현성/표면 계약을 묻기 시작하면 [`docs/cli-quickstart.md`](./cli-quickstart.md)와 [`docs/code-agent-api.md`](./code-agent-api.md)를 추가한다.
4. 상대가 "이 narrative가 실제 리뷰와 운영 근거를 거쳤는가?"를 묻는다면 `H-050`~`H-053` result/review 세트를 audit trail로 추가한다.
5. 상대가 운영 방식이나 책임 분리를 묻는다면 [`docs/codex-ops-workflow.md`](./codex-ops-workflow.md), [`docs/PROJECT_OVERVIEW.md`](./PROJECT_OVERVIEW.md), `CURRENT_STATUS`, `TASK_BOARD`, `DECISIONS`를 순서대로 선택 첨부한다.
6. fallback-warning historical track은 직접 질문이 나오기 전에는 꺼내지 않는다. 꺼낼 때도 current roadmap가 아니라 parked governance example로 위치를 고정한다.

## Maintenance Checklist / Read Next

- README, case study, walkthrough의 핵심 클레임이 바뀌면 이 문서의 mapping table과 starter set 설명을 같은 라운드에서 함께 갱신한다.
- 새로운 portfolio packaging 라운드가 닫히면 proof artifacts의 최신 라운드 묶음을 교체하거나 보강한다.
- `CURRENT_STATUS` 날짜가 바뀌면 이 문서의 참조 날짜와 shareability 판단도 같이 확인한다.
- `TASK_BOARD`나 parked 정책이 바뀌면 fallback-warning 비전면화 규칙이 여전히 맞는지 다시 본다.
- 외부 공유 전 마지막으로 테스트 근거가 최신인지, 문서가 현재 branch 상태와 어긋나지 않는지 확인한다.

read next:

- [`README.md`](../README.md)
- [`docs/portfolio-case-study.md`](./portfolio-case-study.md)
- [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md)
- [`docs/codex-ops-workflow.md`](./codex-ops-workflow.md)
- [`coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`](../coordination/REPORTS/CURRENT_STATUS_2026-03-19.md)
