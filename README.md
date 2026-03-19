# Dev-Agent Orchestrator

Dev-Agent Orchestrator는 LLM을 단순 챗봇이 아니라 개발 파이프라인 안에서 실행되는 에이전트로 다루기 위한 오케스트레이션 레이어입니다. Spring Boot 기반으로 모델 라우팅, 프롬프트/컨텍스트 조합, Code/Spec/Doc/Review 에이전트 실행, 체이닝, run-state 추적을 한 저장소에서 운영할 수 있도록 정리했습니다.

## Why This Exists

개발 자동화에서 가장 자주 부딪히는 문제는 "모델 호출" 자체보다 그 앞뒤의 운영 표면입니다.

- 어떤 요청에 어떤 모델을 선택할지 매번 다르게 판단해야 합니다.
- 코드 생성은 dry-run과 실제 파일 적용을 안전하게 구분해야 합니다.
- Spec -> Code -> Doc/Review 같은 연쇄 실행은 실패 전파 정책과 결과 추적이 필요합니다.
- 반복 실행과 회귀 점검을 위해 run-state와 문서화된 운영 규칙이 필요합니다.

이 프로젝트는 그 운영 표면을 한 번에 다루는 Dev-Agent orchestration foundation을 목표로 합니다.

## Capability Snapshot

- Agent별 모델 라우팅과 fallback 정책
  - OpenAI, Anthropic, Google 공급자 경로를 지원하고, `mode`, `riskLevel`, `largeContext`, `strictJsonRequired`에 따라 라우팅합니다.
- CodeAgent API
  - `POST /api/agents/code/generate`
  - `files[]` 우선 파싱, dry-run/apply, overwrite 가드, Doc/Review 체인 연계를 지원합니다.
- SpecAgent API
  - `POST /api/agents/spec/generate`
  - Spec 생성 후 Code 체인, 그리고 Code의 Doc/Review 체인까지 원샷으로 연결할 수 있습니다.
- DocAgent / ReviewAgent API
  - Code 산출물을 입력으로 받아 구조화된 문서 또는 리뷰 JSON을 생성합니다.
- CLI
  - `./devagent generate`, `./devagent spec`
  - human-readable 출력과 JSON 출력, `PARTIAL_SUCCESS` 소비 가드레일(`--fail-on-chain-failures`)을 제공합니다.
- Prompt / Context / Run-State 계층
  - PromptRegistry, ContextPolicy, SQLite 우선 RunState 저장을 통해 반복 가능한 로컬 운영을 지원합니다.

## How It Works

```text
User request
  -> routing policy resolves provider/model candidates
  -> prompt/context layers are assembled
  -> target agent executes (SPEC / CODE / DOC / REVIEW)
  -> optional chain runs
  -> parsed output + apply result + run-state are persisted
```

핵심 운영 흐름은 다음처럼 나뉩니다.

1. 라우팅 레이어가 요청 성격에 맞는 primary/fallback 모델 후보를 선택합니다.
2. 프롬프트 계층과 컨텍스트 정책이 실행 입력을 구성합니다.
3. Agent가 구조화된 산출물을 생성합니다.
4. 필요하면 Spec -> Code -> Doc/Review 체인이 이어집니다.
5. 결과, 체인 실패, 경고 이벤트가 run-state에 기록됩니다.

## Quickstart

### 1. API 키 준비

프로젝트는 아래 환경 변수를 사용합니다.

```bash
export OPENAI_API_KEY="<your-openai-key>"
export ANTHROPIC_API_KEY="<your-anthropic-key>"
export GOOGLE_API_KEY="<your-google-or-gemini-key>"
```

`GOOGLE_API_KEY` 대신 `GEMINI_API_KEY`를 사용해도 됩니다.

### 2. CLI 실행 준비

```bash
chmod +x ./devagent
```

### 3. 테스트 게이트 확인

```bash
./gradlew clean test --no-daemon
```

### 4. 최소 실행 예시: Code Generate

기본 동작은 dry-run입니다.

```bash
./devagent generate \
  -p demo-auth \
  -r . \
  -u "로그인 API 스켈레톤을 만들어줘" \
  -m BALANCED \
  -k MEDIUM
```

### 5. 원샷 체인 예시: Code -> Doc/Review

```bash
./devagent generate \
  -u "로그인 API 코드를 생성해줘" \
  --chain-to-doc true \
  --doc-user-request "생성된 코드를 기준으로 API 문서를 작성해줘" \
  --chain-to-review true \
  --review-user-request "보안/안정성 중심 리뷰를 작성해줘" \
  --chain-failure-policy PARTIAL_SUCCESS
```

자동화나 CI에서 체인 실패를 종료코드로 승격하고 싶다면 `--fail-on-chain-failures true`를 함께 사용하면 됩니다.

## Docs Map

- [Portfolio case study](docs/portfolio-case-study.md)
- [Demo / showcase walkthrough](docs/demo-showcase-walkthrough.md)
- [Evidence / report export bundle](docs/evidence-report-export-bundle.md)
- [CLI quickstart](docs/cli-quickstart.md)
- [Code Agent API](docs/code-agent-api.md)
- [Model routing policy](docs/model-routing-policy.md)
- [Codex 3-thread ops workflow](docs/codex-ops-workflow.md)
- [Project overview](docs/PROJECT_OVERVIEW.md)

## Current Limits And Next Focus

- 현재 저장소의 핵심 orchestration 기능과 외부 공개용 starter set(README entrypoint / portfolio case study / demo walkthrough / evidence bundle guide)은 정리됐고, 남은 후속 작업은 narrative polishing과 shareability/redaction 판단 같은 마지막 close-out alignment입니다.
- 출력 파싱 안전성과 체인 실패 가드레일은 보강되어 있지만, 생성 내용의 의미 품질 평가는 여전히 운영 관점에서 계속 점검해야 합니다.
- `PARTIAL_SUCCESS`를 사용하는 소비자는 성공 응답만 믿지 말고 `chainFailures[]`를 함께 확인해야 합니다.
