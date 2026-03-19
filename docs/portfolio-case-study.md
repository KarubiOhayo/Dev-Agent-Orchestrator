# Portfolio Case Study: Dev-Agent Orchestration With Operational Guardrails

## TL;DR

Dev-Agent Orchestrator는 "LLM을 한 번 호출해서 결과를 받는 도구"보다 한 단계 위의 문제를 다루기 위해 만든 저장소입니다. 이 프로젝트의 초점은 모델 라우팅, 프롬프트/컨텍스트 조합, Code/Spec/Doc/Review 에이전트 실행, 체이닝, 파일 적용 제어, run-state 기록을 한 운영 표면으로 묶는 데 있습니다. 특히 흥미로운 지점은 기능 나열보다도, 자동화된 개발 작업을 어떻게 통제 가능한 시스템으로 만들었는가에 있습니다.

## The Problem

개발 자동화에 LLM을 붙일 때 가장 먼저 보이는 것은 프롬프트와 모델 선택이지만, 실제로 더 오래 남는 문제는 그 주변의 운영 표면입니다.

- 같은 작업이라도 요청 성격에 따라 다른 모델 선택 기준이 필요합니다.
- 코드 생성은 텍스트 출력으로 끝나는 것이 아니라 dry-run, 파일 쓰기, overwrite 방지, 경계 검증까지 연결되어야 합니다.
- Spec -> Code -> Doc/Review처럼 여러 단계를 이어 붙이면 실패를 어디서 어떻게 해석할지 계약이 필요합니다.
- 반복 실행과 회귀 점검을 하려면 결과뿐 아니라 시도, 체인 이벤트, 경고 신호까지 추적 가능해야 합니다.

이 프로젝트는 이런 문제를 "모델 호출 래퍼"가 아니라 orchestration layer 관점에서 정리하려는 시도입니다.

## Why An Orchestration Layer Matters

단일 모델 호출 데모는 빠르게 만들 수 있지만, 반복 가능한 개발 워크플로우로 가려면 몇 가지 층이 더 필요합니다.

### 1. Routing should be request-aware

이 저장소는 모든 요청을 한 모델에 고정하지 않습니다. `mode`, `riskLevel`, `largeContext`, `strictJsonRequired` 같은 입력을 바탕으로 agent별 primary/fallback 후보를 고릅니다. 이 덕분에 "코드 생성 품질", "구조화 출력 안정성", "긴 컨텍스트 대응", "비용/속도" 같은 요구를 같은 API 표면 안에서 분기할 수 있습니다.

### 2. Prompting is not enough without context assembly

모델 응답 품질은 프롬프트 한 장으로만 결정되지 않습니다. 이 프로젝트는 global -> agent -> project 계층의 프롬프트 합성과 ContextPolicy를 통해, 요청마다 어떤 규칙과 예시를 붙일지 운영 가능한 구조로 다룹니다. 중요한 점은 "무엇을 말할까"보다 "어떤 입력을 어떤 규칙으로 조립할까"를 시스템 레벨에서 다룬다는 것입니다.

### 3. Agent workflows need explicit contracts

이 저장소의 핵심 흐름은 단일 Code 호출에서 끝나지 않습니다. Spec 생성 뒤 Code를 연쇄 실행할 수 있고, Code 결과를 기반으로 Doc/Review까지 이어 붙일 수 있습니다. 여기서 중요한 것은 체이닝 자체보다 체인 실패를 어떻게 해석할지 명시했다는 점입니다. `FAIL_FAST`와 `PARTIAL_SUCCESS`를 분리하고, 후자의 경우 `chainFailures[]`를 응답 계약으로 고정해 자동화 소비자가 실패를 놓치지 않도록 했습니다.

### 4. Runs should be inspectable after the fact

운영 중에는 "성공했는가"만으로 충분하지 않습니다. 어떤 모델 후보를 시도했는지, 어떤 체인이 실행됐는지, 출력 파싱에서 fallback warning이 났는지, 실제 파일 적용은 어떻게 평가됐는지가 남아야 합니다. 이 프로젝트는 SQLite 우선 run-state와 이벤트 기록을 통해 그 흔적을 보존합니다.

## System Overview

현재 시스템은 아래 흐름으로 동작합니다.

```text
Request
  -> routing policy resolves provider/model candidates
  -> prompt/context layers assemble execution input
  -> target agent runs (SPEC / CODE / DOC / REVIEW)
  -> optional chain continues
  -> parsed output, apply result, chain events, warnings are persisted
```

구성 요소를 조금 더 풀어보면 다음과 같습니다.

- Routing
  - Agent 역할과 요청 조건에 따라 primary/fallback 모델 후보를 정합니다.
- Prompt / Context
  - 공통 규칙, agent별 규칙, 프로젝트 규칙과 선택된 context 파일을 합성합니다.
- Agents
  - `SpecAgent`, `CodeAgent`, `DocAgent`, `ReviewAgent`가 각각 구조화된 산출물을 담당합니다.
- Apply surface
  - Code 결과는 `apply=false` dry-run과 `apply=true` 실제 반영을 구분합니다.
- Run-state
  - 실행 결과, 경고, 체인 이벤트, 프로젝트 메모리를 남겨 후속 점검 기반을 만듭니다.

이 구조 덕분에 저장소는 "프롬프트 몇 개"가 아니라, 개발 작업 자동화를 위한 실행 표면 전체를 다루는 형태가 됩니다.

## Operating Model: How Complexity Is Controlled

이 프로젝트에서 portfolio 관점으로 가장 강조하고 싶은 부분은 기능 목록보다 운영 모델입니다. 자동화된 개발 작업은 쉽게 강력해지지만, 동시에 쉽게 통제 불가능해집니다. 이 저장소는 그 리스크를 3-thread + stateless round 모델로 다룹니다.

### 3-thread separation

- Main-Control
  - 라운드 계획, handoff 확정, 승인/보류 판단을 담당합니다.
- Executor
  - handoff 범위 안에서만 구현하고 테스트를 통과시킨 뒤 결과를 제출합니다.
- Review-Control
  - 구현 변경을 읽기 전용으로 검토하고 P1/P2/P3 관점에서 위험을 식별합니다.

이 분리는 단순한 역할 놀이가 아니라, "누가 범위를 정하고, 누가 바꾸고, 누가 검증하는가"를 나눠서 자동화 작업의 책임 경계를 선명하게 만드는 장치입니다.

### Stateless rounds and relay discipline

각 라운드는 세션 기억에 의존하지 않고 핵심 문서를 다시 읽는 것부터 시작합니다. 그 위에서 `Main -> Executor -> Review -> Main` 릴레이를 고정 포맷으로 남깁니다. 이 방식은 두 가지 문제를 줄여 줍니다.

- 긴 컨텍스트 대화에 기대다가 생기는 scope drift
- 사람이 중간에 바뀌거나 세션이 끊겼을 때 생기는 운영 누락

즉, 이 저장소는 "에이전트를 잘 쓰는 법"보다 "에이전트가 개입된 작업을 어떻게 추적 가능하게 나눌 것인가"를 설계 대상으로 삼습니다.

### Report-only automation policy

Automations도 무제한으로 열어두지 않았습니다. 이 저장소의 자동화 정책은 report-only를 기본으로 잡고, 자동 파일 수정/커밋/PR 생성은 금지합니다. 이는 자동화 범위를 의도적으로 좁혀 신뢰를 먼저 확보하려는 선택입니다.

## Quality, Safety, And Observability Guardrails

이 프로젝트의 guardrail은 추상적인 "조심하자"가 아니라 실제 API/CLI/운영 계약으로 연결됩니다.

### Safe file application

- 기본값은 `apply=false`입니다.
- 파일 쓰기는 `apply=true`일 때만 수행합니다.
- target root 내부 상대경로만 허용하는 경계 정책을 둡니다.
- 심볼릭 링크 경계 우회 방지도 별도 보강했습니다.

이 선택은 코드 생성 결과를 바로 파일 시스템에 반영할 때 생길 수 있는 위험을 줄입니다.

### Structured output first, controlled fallback second

- Code 출력은 `files[]` JSON 파싱을 우선합니다.
- 직접 JSON 파싱이 실패하면 markdown fallback을 허용하되 warning 이벤트를 남깁니다.
- parser safety guard는 loose fallback이 설명문이나 메타 JSON을 파일로 오인하지 않도록 객체 경계를 제한합니다.
- `parsedFiles=0`인 `apply=true` 요청은 "아무것도 안 썼지만 성공"처럼 보이지 않도록 실패로 처리합니다.

이 부분은 생성형 시스템에서 자주 무시되는 영역인데, 실제 적용 단계로 갈수록 매우 중요해집니다.

### Chain failure visibility

- 기본 정책은 `FAIL_FAST`입니다.
- 필요하면 `PARTIAL_SUCCESS`를 선택할 수 있습니다.
- 이 경우에도 소비자는 `chainFailures[]`를 반드시 확인해야 하며, CLI는 `--fail-on-chain-failures` 가드레일로 종료코드 `3`을 강제할 수 있습니다.

즉, "부분 성공"을 허용하더라도 실패를 숨기지 않도록 설계했습니다.

### Run-state and warning signals

run-state는 단순 로그 저장이 아니라 운영 해석을 위한 장치입니다. 체인 이벤트(`CHAIN_*`)와 출력 파싱 warning 이벤트(`*_OUTPUT_FALLBACK_WARNING`)를 남겨, 나중에 어떤 문제가 어디서 발생했는지 추적할 수 있게 했습니다. 이 관측성 트랙 중 fallback warning follow-up은 현재 parked 상태로 관리되며, 이는 "모든 운영 관심사를 계속 앞단에 둘 필요는 없다"는 판단도 함께 보여줍니다.

### Test gate as an execution requirement

Executor 라운드의 승인 게이트는 항상 `./gradlew clean test --no-daemon`입니다. 이 프로젝트에서 문서와 릴레이가 중요한 이유는, 자동화 실험을 추상적인 아이디어로 남기지 않고 테스트 통과와 함께 라운드 단위 결과로 고정하려는 운영 방식과 연결되기 때문입니다.

## Why This Is Interesting As Portfolio Work

이 프로젝트가 portfolio로 흥미로운 이유는, LLM 애플리케이션을 "모델 성능 시연"이 아니라 "운영 가능한 개발 시스템"으로 다룬다는 점입니다.

- 프롬프트만이 아니라 라우팅, 체이닝, 파일 적용, run-state, 리뷰/승인 흐름까지 설계합니다.
- 기능 추가보다 경계 설정과 실패 해석을 먼저 문서화합니다.
- 자동화 범위를 일부러 제한하는 선택까지 포함해, 시스템 신뢰를 어떻게 쌓는지 보여줍니다.
- 아직 미완성인 영역을 숨기지 않고 roadmap와 limits로 분리해 설명합니다.

결과적으로 이 저장소는 "LLM을 어디에 붙였는가"보다 "LLM이 들어간 개발 작업을 어떻게 통제 가능한 제품 표면으로 만들었는가"를 보여주는 사례에 가깝습니다.

## Current Limits And Next Steps

현재 이 저장소는 orchestration foundation과 운영 규율, 그리고 기본 외부 공유용 starter set은 정리되어 있지만, narrative 밀도 조정과 shareability/redaction 판단 같은 마지막 close-out 단계는 남아 있습니다.

- guided demo path는 [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md)로, post-walkthrough evidence handoff path는 [`docs/evidence-report-export-bundle.md`](./evidence-report-export-bundle.md)로 정리되어 starter set은 바로 공유할 수 있지만, 문서 사이 설명 밀도와 전달 맥락별 polishing은 계속 다듬어야 합니다.
- evidence bundle은 기존 source 문서를 큐레이션해 건네는 가이드이지, 별도 export 폴더나 새로운 metrics를 만들어 주는 산출물이 아닙니다. 공유 전에는 shareability/redaction 판단이 여전히 필요합니다.
- parser safety와 체인 failure guardrail은 강화됐지만, 생성 결과의 의미 품질 평가는 계속 운영 이슈로 남아 있습니다.

다음 packaging 우선순위는 아래 세 가지입니다.

1. README -> case study -> demo/evidence로 이어지는 portfolio narrative 연결을 더 매끈하게 다듬기
2. walkthrough와 evidence bundle 묶음을 바탕으로 starter set과 add-on 설명을 더 매끈하게 다듬기
3. 생성 결과의 의미 품질 평가를 계속 운영 관점에서 점검하기

## Read Together

- entrypoint overview: [`README.md`](../README.md)
- guided demo path: [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md)
- post-walkthrough evidence handoff: [`docs/evidence-report-export-bundle.md`](./evidence-report-export-bundle.md)
- API and execution surface: [`docs/code-agent-api.md`](./code-agent-api.md)
- routing policy: [`docs/model-routing-policy.md`](./model-routing-policy.md)
- operating model: [`docs/codex-ops-workflow.md`](./codex-ops-workflow.md)
- current project status: [`docs/PROJECT_OVERVIEW.md`](./PROJECT_OVERVIEW.md)
