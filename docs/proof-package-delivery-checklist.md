# Proof Package Delivery Checklist

## Who Uses This / When

- 대상: walkthrough 직후 실제 follow-up 메시지나 메일을 보내는 사람
- 목적: 이미 정리된 starter set / add-on을 "무엇을 먼저 보내고, 질문이 나오면 무엇을 붙일지"로 짧게 고정한다
- 범위: 새 증거를 만드는 문서가 아니라, 현재 저장소에 이미 있는 문서를 어떤 순서와 가드레일로 보낼지 정한다

## Pre-Send Gate

보내기 전에 아래 5가지만 먼저 확인한다.

| Check | What to confirm | If not true |
|---|---|---|
| shareability | 비밀값, 개인식별 정보, 로컬 절대경로, 다른 프로젝트명이 그대로 노출되지 않는가 | 가리고, 설명 범위를 줄이고, 필요한 부분만 excerpt로 보낸다 |
| redaction | shell transcript, screenshot, ad-hoc 메모, `coordination/` 문서에 불필요한 내부 정보가 섞이지 않았는가 | starter set과 필요한 add-on만 선별한다 |
| stale reference | [`coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`](../coordination/REPORTS/CURRENT_STATUS_2026-03-19.md), [`coordination/TASK_BOARD.md`](../coordination/TASK_BOARD.md), [`README.md`](../README.md)의 상태 설명이 현재 branch 현실과 어긋나지 않는가 | 날짜와 상태를 먼저 다시 맞추고 보낸다 |
| latest test evidence | 현재 설명하려는 branch 기준 테스트 근거가 `./gradlew clean test --no-daemon` 통과 상태인가 | 통과 근거를 다시 확보하거나 "문서 기준 설명"이라고 명시한다 |
| branch/worktree | `git branch --show-current`, `git status --short`로 발송 설명과 무관한 실험 변경이 없는가 | 변경 범위를 설명하고, 필요하면 excerpt만 공유한다 |

## Default Send Package (`starter set`)

첫 발송은 아래 4개만 보낸다.

1. [`README.md`](../README.md)
2. [`docs/portfolio-case-study.md`](./portfolio-case-study.md)
3. [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md)
4. [`docs/evidence-report-export-bundle.md`](./evidence-report-export-bundle.md)

보내는 순서는 `README -> case study -> walkthrough -> evidence bundle`로 유지한다. 첫 메시지의 목적은 "전체를 다 보냈다"가 아니라 "이 저장소를 어떤 읽기 순서로 이해하면 되는지"를 잡아 주는 것이다.

## Add-On Decision Matrix

| 상대 질문 | 붙일 add-on | 보내는 파일 | 왜 이것만 붙이는가 |
|---|---|---|---|
| "실제로 어떤 명령/API 표면이 있나요?" | technical deep-dive add-on | [`docs/cli-quickstart.md`](./cli-quickstart.md), [`docs/code-agent-api.md`](./code-agent-api.md) | CLI/API surface, safe default, `chainFailures[]`, guardrail 설명이 바로 나온다 |
| "이 narrative가 실제 리뷰와 승인 루프를 거쳤나요?" | audit trail add-on | `coordination/REPORTS/H-050`~`H-053` result/review + `coordination/RELAYS/H-053-review-to-main.md` | external-facing copy가 즉흥 문구가 아니라 round result/review로 다듬어진 흔적을 보여 준다 |
| "이걸 어떤 운영 모델로 통제하나요?" | governance add-on | [`docs/codex-ops-workflow.md`](./codex-ops-workflow.md)부터 시작하고, 필요 시 [`docs/PROJECT_OVERVIEW.md`](./PROJECT_OVERVIEW.md), [`coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`](../coordination/REPORTS/CURRENT_STATUS_2026-03-19.md), [`coordination/TASK_BOARD.md`](../coordination/TASK_BOARD.md), [`coordination/DECISIONS.md`](../coordination/DECISIONS.md) 순으로 추가 | 역할 분리, stateless round, 승인 게이트, active roadmap를 질문 강도에 맞춰 단계적으로 보여 줄 수 있다 |

상대가 질문을 하지 않았다면 add-on은 붙이지 않는다. starter set만으로도 기본 설명은 충분해야 한다.

## Do Not Send / Honesty Guardrail

- `coordination/` 전체를 통째로 보내지 않는다.
- parked 상태인 fallback-warning 트랙은 기본 발송 묶음에 넣지 않는다.
- 실제로 실행하지 않은 live demo, fabricated metrics, screenshot, hosted/product claim을 암시하지 않는다.
- `apply=true` 결과나 production-like 운영 상태를 기본 capability처럼 과장하지 않는다.
- 긴 appendix나 내부 운영 문서를 첫 첨부물처럼 전면에 두지 않는다.

## Maintenance / Stale Check

- 외부 발송 전에 [`docs/evidence-report-export-bundle.md`](./evidence-report-export-bundle.md)와 이 문서가 같은 package logic를 말하는지 본다.
- `CURRENT_STATUS` 날짜가 바뀌었으면 shareability 판단과 최신 테스트 근거 문구도 함께 다시 본다.
- README / walkthrough / evidence bundle 중 하나라도 바뀌면 starter set 순서와 add-on 매핑이 여전히 맞는지 같은 라운드에서 함께 확인한다.
- 빠른 발송용으로는 이 문서를 먼저 보고, 자세한 묶음 설명이 필요할 때만 evidence bundle 문서로 내려간다.

read next:

- [`docs/evidence-report-export-bundle.md`](./evidence-report-export-bundle.md)
- [`docs/demo-showcase-walkthrough.md`](./demo-showcase-walkthrough.md)
- [`coordination/REPORTS/CURRENT_STATUS_2026-03-19.md`](../coordination/REPORTS/CURRENT_STATUS_2026-03-19.md)
