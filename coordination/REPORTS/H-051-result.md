# H-051 결과 보고서 (portfolio copy / case study foundation)

## 상태
- 현재 상태: **완료 (`docs/portfolio-case-study.md` 신설 + `README.md` case study 링크 최소 추가 + 테스트 통과)**
- 실행일(KST): `2026-03-18`
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-051-portfolio-copy-case-study-foundation.md`
  - main relay: `coordination/RELAYS/H-051-main-to-executor.md`
  - 참고: `docs/PROJECT_OVERVIEW.md`, `coordination/TASK_BOARD.md`, `coordination/DECISIONS.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`, `coordination/REPORTS/H-050-result.md`, `coordination/REPORTS/H-050-review.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/codex-ops-workflow.md`, `README.md`

## 변경 파일 목록
- `docs/portfolio-case-study.md`
- `README.md`
- `coordination/REPORTS/H-051-result.md`
- `coordination/RELAYS/H-051-executor-to-review.md`

## 구현 요약
- `docs/portfolio-case-study.md`를 새로 작성해 README의 entrypoint 설명을 반복하지 않고, 외부 평가자/협업자 관점에서 "왜 orchestration layer가 필요한가", "이 저장소가 어떤 운영 모델로 복잡성을 통제하는가", "어떤 guardrail을 실제 계약으로 두고 있는가"를 한 단계 더 깊게 설명했습니다.
- case study는 기능 목록보다 문제 정의, 시스템 구조, 3-thread/stateless/relay 운영, 품질/안전/관측성 guardrail, portfolio 관점 의미, 남은 한계를 중심으로 서사를 구성했습니다.
- `README.md`는 전체 구조를 바꾸지 않고 Docs Map에 case study 링크 1줄만 추가해 entrypoint -> second-layer narrative 연결만 보강했습니다.
- parked 상태인 fallback-warning 트랙은 current focus로 부각하지 않고, observability/운영 규율 사례로만 제한해 handoff 지침을 따랐습니다.

## case study 섹션별 구성 요약
1. `TL;DR`
   - Dev-Agent Orchestrator를 단일 모델 호출 도구가 아니라, 라우팅/컨텍스트/체이닝/파일 적용/run-state를 묶는 orchestration layer로 정의했습니다.
2. `The Problem`
   - 모델 선택, 파일 반영, 체인 실패 해석, 실행 추적 같은 운영 표면이 왜 별도 문제인지 설명했습니다.
3. `Why An Orchestration Layer Matters`
   - request-aware routing, context assembly, explicit chain contracts, inspectable run-state가 왜 필요한지 4개 축으로 정리했습니다.
4. `System Overview`
   - Request -> routing -> prompt/context -> agent -> optional chain -> persisted result 흐름과 구성 요소 역할을 설명했습니다.
5. `Operating Model: How Complexity Is Controlled`
   - 3-thread 분리, stateless rounds, relay discipline, report-only automation policy를 "자동화 작업 통제 방식" 관점으로 서술했습니다.
6. `Quality, Safety, And Observability Guardrails`
   - apply 기본값, 경계 검증, parser safety, chain failure policy, run-state 이벤트, 테스트 게이트를 실제 계약과 연결했습니다.
7. `Why This Is Interesting As Portfolio Work`
   - 모델 데모가 아니라 운영 가능한 개발 시스템 설계 사례라는 점을 강조했습니다.
8. `Current Limits And Next Steps`
   - demo/showcase walkthrough, evidence export bundle, 의미 품질 평가 지속 필요를 남은 과제로 분리했습니다.
9. `Read Together`
   - README 및 세부 근거 문서로 이어지는 read-next 링크를 정리했습니다.

## 핵심 클레임과 근거 source 문서 매핑

| case study 클레임 | 근거 source |
|---|---|
| 프로젝트의 핵심은 모델 호출 자체보다 orchestration surface를 다루는 데 있다 | `docs/PROJECT_OVERVIEW.md`, `README.md`, `coordination/TASK_BOARD.md` |
| agent별 routing과 request-aware 모델 선택이 존재한다 | `docs/model-routing-policy.md`, `docs/PROJECT_OVERVIEW.md`, `coordination/DECISIONS.md` |
| Spec -> Code -> Doc/Review 체이닝과 chain failure contract가 존재한다 | `docs/code-agent-api.md`, `docs/PROJECT_OVERVIEW.md`, `coordination/DECISIONS.md` |
| dry-run/apply 구분, 경계 검증, parser safety, empty apply fail 처리 같은 안전장치가 있다 | `docs/code-agent-api.md`, `docs/PROJECT_OVERVIEW.md`, `coordination/DECISIONS.md` |
| 3-thread / stateless / relay discipline가 운영 모델의 핵심이다 | `docs/codex-ops-workflow.md`, `coordination/TASK_BOARD.md`, `coordination/DECISIONS.md`, `AGENTS.md` |
| Automations는 report-only 정책으로 제한된다 | `docs/codex-ops-workflow.md`, `coordination/DECISIONS.md`, `AGENTS.md` |
| fallback-warning 트랙은 current focus가 아니라 parked historical concern이다 | `coordination/PARKING_LOT.md`, `coordination/TASK_BOARD.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md` |
| 다음 packaging 우선순위는 demo/showcase, evidence export 정리다 | `docs/PROJECT_OVERVIEW.md`, `coordination/TASK_BOARD.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md` |

## README 링크 변경 여부와 이유
- 변경 여부: **예**
- 변경 내용:
  - `README.md`의 `Docs Map`에 `Portfolio case study` 링크 1줄 추가
- 이유:
  - handoff 지침대로 README 전체 구조는 유지하고, entrypoint에서 second-layer narrative로 자연스럽게 이어지는 최소 연결만 제공하기 위해서입니다.
  - case study 본문은 전부 `docs/portfolio-case-study.md`에 두고 README 역할 혼선을 피했습니다.

## 테스트 명령/결과
- 실행 명령: `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크 및 후속 제안
- case study foundation은 준비됐지만, 외부 데모 흐름을 바로 따라갈 수 있는 showcase walkthrough는 아직 없습니다.
- evidence / report export bundle이 아직 정리되지 않아, 외부 공유용 proof package는 후속 라운드가 필요합니다.
- 문서성 산출물인 만큼 자동 검증 범위는 제한적이며, 클레임 정합성은 Review에서 source 문서 대조가 중요합니다.
- 향후 README나 API 표면이 바뀌면 case study도 함께 유지보수해야 합니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `docs/portfolio-case-study.md`가 README 반복 요약이 아니라, 한 단계 더 깊은 external-facing narrative 역할을 실제로 수행하는지
2. routing, 체이닝, guardrail, 3-thread/stateless/relay 운영 관련 클레임이 `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/codex-ops-workflow.md`, `docs/PROJECT_OVERVIEW.md`와 과장 없이 정합한지
3. fallback-warning parked 정책을 current roadmap처럼 보이게 만들지 않았는지
4. `README.md` 수정이 docs map 링크 추가 수준에 머물러 entrypoint 역할을 유지하는지
