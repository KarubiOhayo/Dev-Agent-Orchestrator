# Demo / Showcase Walkthrough

## Demo Goal / Audience / Timebox

- 대상: 이 저장소를 처음 보는 협업자, 면접관, 외부 평가자
- 목표: "LLM을 호출하는 데모"가 아니라 "개발 작업을 통제 가능한 orchestration layer로 패키징한 저장소"라는 인상을 짧은 시간 안에 전달한다
- 권장 시간:
  - 5분 버전: `README.md` -> `docs/portfolio-case-study.md` -> dry-run CLI 1회
  - 10~15분 버전: 위 흐름 + chain-aware CLI 예시 + 운영 문서 follow-up

## Before You Start

- 시작 위치: 저장소 루트
- docs-only walkthrough는 API 키 없이 진행할 수 있다.
- live CLI walkthrough를 하려면 아래 환경 변수 중 필요한 키를 먼저 준비한다.

```bash
export OPENAI_API_KEY="<your-openai-key>"
export ANTHROPIC_API_KEY="<your-anthropic-key>"
export GOOGLE_API_KEY="<your-google-or-gemini-key>"
```

- `./devagent`가 실행 가능해야 한다.

```bash
chmod +x ./devagent
```

- 라이브 실행 전에는 테스트 게이트를 먼저 통과시켜 두는 편이 가장 안전하다.

```bash
./gradlew clean test --no-daemon
```

- safe defaults:
  - Code demo는 기본 dry-run(`apply=false`) 상태로 진행한다.
  - `PARTIAL_SUCCESS`를 보여줄 때는 `--fail-on-chain-failures true`를 함께 써서 체인 실패를 숨기지 않는다.
  - 실행 결과를 미리 꾸며 말하지 않고, 실제 출력의 필드와 문서 링크를 관찰 포인트로 사용한다.
  - parked 상태인 fallback-warning 트랙은 governance 사례로만 짧게 언급하고 showcase의 대표 줄거리로 올리지 않는다.

## Walkthrough Path A: Entrypoint Reading Order

| Step | Open | Why this step matters | What to watch for |
|---|---|---|---|
| 1 | `README.md` | 저장소가 무엇을 하는지 가장 짧게 보여주는 entrypoint다 | `Capability Snapshot`, `Quickstart`, `Docs Map`, `Current Limits And Next Focus` |
| 2 | `docs/portfolio-case-study.md` | 기능 소개를 넘어 왜 orchestration layer가 필요한지 설명한다 | 문제 정의, 3-thread 운영, guardrail, limits |
| 3 | `docs/cli-quickstart.md` | 실제 CLI 표면과 종료코드 계약을 확인한다 | `generate`, `spec`, `--json`, `--fail-on-chain-failures` |
| 4 | `docs/code-agent-api.md` | 라이브 데모에서 말하는 안전장치와 체인 계약의 근거를 보여준다 | `apply=false`, `files[]`, `chainFailures[]`, fallback warning 이벤트 |
| 5 | `docs/codex-ops-workflow.md` | 이 저장소가 운영 규율까지 포함한 패키지라는 점을 마무리한다 | Main / Executor / Review, stateless rounds, report-only automation |

시간이 짧다면 1~2단계까지만 보여줘도 충분하다. 그 경우 마지막에 `README.md`의 docs map으로 "더 읽을 경로가 이미 정리돼 있다"는 점만 짚고 넘어가면 된다.

## Walkthrough Path B: CLI Demo Flow

### 1. Safe default dry-run

라이브 실행이 가능하면 가장 먼저 아래 dry-run을 사용한다.

```bash
./devagent generate \
  -p demo-auth \
  -r . \
  -u "로그인 API 스켈레톤을 만들어줘" \
  -m BALANCED \
  -k MEDIUM
```

이 단계에서 볼 포인트:

- `runId`: 실행을 추적 가능한 단위로 남긴다는 점
- `model` 또는 route summary: 요청 성격에 따라 모델이 선택된다는 점
- `parsedFiles`: 구조화된 파일 초안을 실제로 소비했다는 점
- `applyOutcome=DRY_RUN`, `writtenFiles=0`: 기본값이 안전한 검토 모드라는 점
- file results: "바로 쓴 결과"가 아니라 "계획된 반영"을 먼저 보여준다는 점

### 2. Chain-aware generate demo

두 번째 단계에서는 체인 자체보다 "체인 실패를 어떻게 해석하는가"를 보여주는 데 집중한다.

```bash
./devagent generate \
  -u "로그인 API 코드를 생성해줘" \
  --chain-to-doc true \
  --doc-user-request "생성된 코드를 기준으로 API 문서를 작성해줘" \
  --chain-to-review true \
  --review-user-request "보안/안정성 중심 리뷰를 작성해줘" \
  --chain-failure-policy PARTIAL_SUCCESS \
  --fail-on-chain-failures true
```

이 단계에서 볼 포인트:

- Doc/Review 체인이 함께 시도된다는 점
- `chainFailures[]`: 성공처럼 보이는 응답에서도 반드시 확인해야 하는 필드라는 점
- `--fail-on-chain-failures true`: 자동화/CI에서는 exit code `3`으로 체인 실패를 승격할 수 있다는 점
- `apply=true`를 쓰지 않아도 orchestration surface와 guardrail을 충분히 보여줄 수 있다는 점

### 3. Live call 없이 CLI surface만 보여주고 싶을 때

API 키를 준비하지 않았거나 live call을 피하고 싶다면 아래 명령으로 표면만 보여주고, 실제 실행 예시는 `docs/cli-quickstart.md`로 넘긴다.

```bash
./devagent help
```

## What To Watch For

| Signal | Why it matters |
|---|---|
| `runId` | 실행이 문서 설명으로 끝나지 않고 추적 가능한 run 단위로 남는다 |
| `model` / route summary | "모델 하나 고정"이 아니라 request-aware routing을 쓴다 |
| `parsedFiles` | 모델 출력이 단순 텍스트가 아니라 파일 초안으로 구조화돼 소비된다 |
| `applyOutcome` / `writtenFiles` | dry-run과 실제 쓰기를 분리해 안전성을 확보한다 |
| `chainFailures[]` | `PARTIAL_SUCCESS`에서도 실패 신호를 숨기지 않는다 |
| docs map / read-next links | README -> case study -> walkthrough -> API/ops 문서로 이어지는 패키징이 완성돼 있다 |

## Narration Cues

- 이 저장소의 핵심은 "좋은 프롬프트"보다 "실행 표면을 어떻게 통제할 것인가"에 있다.
- 모델 라우팅, 파일 적용, 체인 실패 해석, run-state 기록이 한 시스템 안에서 연결돼 있다는 점이 차별점이다.
- live demo의 포인트는 결과물 품질 자랑보다 safe default와 guardrail을 어떻게 설계했는지 보여주는 데 있다.
- 3-thread + stateless round 운영은 코드 생성 자체보다 "누가 범위를 정하고, 누가 바꾸고, 누가 검증하는가"를 분리하는 장치다.

## Guardrails / What Not To Claim

- hosted product, production traffic, 외부 배포가 이미 준비됐다고 말하지 않는다.
- `apply=true`를 기본 흐름처럼 시연하지 않는다. 파일 쓰기는 opt-in이다.
- `PARTIAL_SUCCESS`를 "문제 없는 성공"처럼 설명하지 않는다.
- fallback-warning parked 트랙을 current roadmap의 핵심처럼 다시 전면화하지 않는다.
- 문서에 없는 옵션, 실제로 없는 지표, 꾸며낸 스크린샷이나 예시 출력은 사용하지 않는다.

## Read Next

- entrypoint overview: [`README.md`](../README.md)
- second-layer narrative: [`docs/portfolio-case-study.md`](./portfolio-case-study.md)
- sender-facing follow-up checklist: [`docs/proof-package-delivery-checklist.md`](./proof-package-delivery-checklist.md)
- post-walkthrough evidence handoff: [`docs/evidence-report-export-bundle.md`](./evidence-report-export-bundle.md)
- CLI surface: [`docs/cli-quickstart.md`](./cli-quickstart.md)
- API contract and guardrails: [`docs/code-agent-api.md`](./code-agent-api.md)
- operating model: [`docs/codex-ops-workflow.md`](./codex-ops-workflow.md)
- current project snapshot: [`docs/PROJECT_OVERVIEW.md`](./PROJECT_OVERVIEW.md)
