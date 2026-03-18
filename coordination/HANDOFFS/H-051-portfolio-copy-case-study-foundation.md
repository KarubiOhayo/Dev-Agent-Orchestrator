# H-051 portfolio copy / case study foundation

Owner: WT-51 (`codex/h051-portfolio-copy-case-study-foundation`)
Priority: Highest

## 목표
- H-050에서 만든 root `README.md` foundation 위에, 외부 평가자/협업자/채용 관점에서 읽을 수 있는 두 번째 레이어 문서인 case study 초안을 만든다.
- 이 저장소의 핵심 가치가 "무슨 기능이 있나"를 넘어서 "왜 이런 orchestration 구조가 필요한가, 어떤 운영 선택을 했는가, 무엇이 차별점인가"까지 설명되도록 정리한다.
- active roadmap와 parked fallback-warning 정책을 유지하면서 portfolio package의 서사 공백을 줄인다.

## 작업 범위
- 신규/수정 허용:
  - `docs/portfolio-case-study.md` (신규)
  - `README.md` (필요 시 case study 링크 추가 수준의 최소 수정만 허용)
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `docs/code-agent-api.md`
  - `docs/model-routing-policy.md`
  - `docs/codex-ops-workflow.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`
  - `coordination/REPORTS/H-050-result.md`
  - `coordination/REPORTS/H-050-review.md`

## 구현 지침
- case study는 README를 반복 요약하는 문서가 아니라, README보다 한 단계 깊은 external-facing narrative여야 한다.
- 문서는 아래 질문에 답할 수 있게 구성한다.
  1. 어떤 문제를 풀기 위해 이 프로젝트를 만들었는가?
  2. 단순 "모델 호출"이 아니라 orchestration layer가 왜 중요한가?
  3. 현재 시스템은 어떤 흐름과 운영 규칙으로 동작하는가?
  4. 어떤 설계 선택이 품질/안전/운영성에 기여하는가?
  5. 지금 남아 있는 한계와 다음 packaging 우선순위는 무엇인가?
- 권장 섹션:
  1. TL;DR 또는 1문단 positioning
  2. Problem / Why this project exists
  3. System overview (routing, prompt/context, agents, chaining, run-state)
  4. Operating model (3-thread, stateless rounds, relay discipline)
  5. Quality / safety / observability guardrails
  6. Why this is interesting as portfolio work
  7. Current limits and next steps
- 클레임은 현재 저장소의 구현/문서/운영 규칙으로 역추적 가능한 것만 사용한다. 실제 운영 규모, 외부 사용자 수, 성능 수치, 배포 상태를 추정해서 쓰지 않는다.
- 3-thread 운영 방식은 내부 round history 나열이 아니라 "복잡한 자동화 작업을 어떻게 통제했는가" 관점으로 설명한다.
- fallback-warning 트랙은 parked 상태이므로, historical observability concern 또는 운영 정책 사례로 짧게 다룰 수는 있지만 현재 핵심 스토리의 전면에 두지 않는다.
- `README.md`를 수정한다면 docs map 또는 "read next" 링크 추가 수준으로 제한한다. README 전체 구조를 다시 뒤집지 않는다.

## 수용 기준
1. `docs/portfolio-case-study.md`가 생성되고, README보다 더 깊은 external-facing narrative를 제공한다.
2. case study의 기능/설계/운영 클레임이 `docs/PROJECT_OVERVIEW.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/codex-ops-workflow.md`와 정합하다.
3. 문서가 3-thread / stateless / relay 기반 운영 모델을 이 저장소의 차별점으로 설명하되, 내부 라운드 이력 나열로 흐르지 않는다.
4. 품질/안전/관측성 항목이 테스트 게이트, guardrail, run-state, parser safety, chain failure policy 같은 실제 근거와 연결된다.
5. `README.md`가 수정될 경우 case study로 자연스럽게 이어지는 링크 수준의 최소 변경만 포함한다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- demo / showcase walkthrough 패키징
- evidence / report export bundle 제작
- `HELP.md` 정리
- 코드/설정 변경
- fallback-warning parked 트랙 재개

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- README와 case study의 역할을 섞지 말고, README는 entrypoint / case study는 second-layer narrative로 분리한다

## 보고서
- 완료 시 `coordination/REPORTS/H-051-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-051-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - case study 섹션별 구성 요약
  - 핵심 클레임과 근거 source 문서 매핑
  - README 링크 변경 여부와 이유
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안(`demo`, `evidence export` 관점)
