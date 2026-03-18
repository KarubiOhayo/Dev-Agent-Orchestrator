# H-050 README / project positioning foundation

Owner: WT-50 (`codex/h050-readme-project-positioning-foundation`)
Priority: Highest

## 목표
- 루트 `README.md`를 새로 작성해 이 저장소가 "LLM을 개발 파이프라인에 내장하는 Dev-Agent Orchestrator"라는 포지셔닝을 첫 화면에서 이해할 수 있게 만든다.
- 현재 구현된 핵심 기능(Spec -> Code -> Doc/Review 체이닝, 모델 라우팅, 컨텍스트/프롬프트 계층, CLI, run-state)을 외부 공유 가능한 한 장의 엔트리 문서로 압축한다.
- active roadmap와 parked fallback-warning 정책을 반영해 README가 현재 우선순위를 왜곡하지 않도록 정리한다.

## 작업 범위
- 신규/수정 허용:
  - `README.md` (신규)
- 참고 전용:
  - `docs/PROJECT_OVERVIEW.md`
  - `docs/cli-quickstart.md`
  - `docs/code-agent-api.md`
  - `docs/model-routing-policy.md`
  - `docs/codex-ops-workflow.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`

## 구현 지침
- README는 외부 기여자/평가자가 저장소 첫 화면만 보고도 다음 질문에 답할 수 있게 구성한다.
  1. 이 프로젝트는 무엇인가?
  2. 왜 존재하는가?
  3. 지금 무엇이 동작하는가?
  4. 어떻게 실행해볼 수 있는가?
  5. 더 자세한 문서는 어디에 있는가?
- 권장 섹션:
  1. 제목 + 1~2문장 positioning
  2. 문제 정의 / 왜 필요한가
  3. 핵심 기능 또는 capability snapshot
  4. 동작 흐름 또는 아키텍처 개요
  5. quickstart
  6. 상세 문서 맵
  7. 현재 한계와 다음 로드맵
- 클레임은 반드시 현재 저장소의 구현/문서로 뒷받침되는 내용만 사용한다. 아직 없는 기능, 외부 배포 상태, 운영 자동화를 추정해서 쓰지 않는다.
- quickstart는 실제 현재 저장소 기준으로 맞춘다.
  - 필수 항목: API key 환경 변수, `./gradlew clean test --no-daemon`, 최소 1개 이상의 `./devagent generate` 예시
  - 선택 항목: `./devagent spec` 체인 예시는 실제 옵션명과 경로가 현재 문서와 일치할 때만 포함한다.
- README의 톤은 portfolio / product-facing으로 유지하되 내부 round ID, parked handoff, historical fallback-warning follow-up을 본문 중심 서사로 끌어오지 않는다.
- `fallback-warning` 트랙은 현재 parked 상태이므로, README에서는 active blocker나 대표 기능처럼 보이지 않게 다룬다. 제한사항/운영 리스크를 적을 경우에도 한두 줄 수준으로만 언급한다.
- 루트 `HELP.md`는 현재 Spring 기본 placeholder이지만, 본 라운드 범위는 `README.md`에 한정한다. `HELP.md` 정리는 후속 과제로 남긴다.

## 수용 기준
1. 루트 `README.md`가 생성되고, 첫 화면에서 프로젝트 포지셔닝과 핵심 가치가 명확히 드러난다.
2. README에 현재 구현된 핵심 기능(라우팅, Code/Spec/Doc/Review agent, 체이닝, CLI, run-state)이 과장 없이 요약된다.
3. README에 실제 실행 가능한 quickstart가 포함되고, `./gradlew clean test --no-daemon`와 최소 1개 이상의 `./devagent generate` 예시가 들어간다.
4. README에서 상세 문서(`docs/cli-quickstart.md`, `docs/code-agent-api.md`, `docs/model-routing-policy.md`, `docs/codex-ops-workflow.md`)로 자연스럽게 이어지는 링크가 제공된다.
5. README의 roadmap/limitations는 현재 active priorities와 일치하고, parked fallback-warning 트랙을 current next step처럼 제시하지 않는다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정)과 handoff 범위 밖 파일은 수정하지 않는다.
7. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- portfolio copy / case study 본문 작성
- demo / showcase walkthrough 패키징
- evidence / report export bundle 제작
- 코드/설정 변경
- `HELP.md` 정리

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- README 예시 명령은 현재 CLI 옵션명과 문서 기준을 그대로 따른다

## 보고서
- 완료 시 `coordination/REPORTS/H-050-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-050-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - README 섹션별 구성 요약
  - 각 핵심 섹션의 근거로 사용한 source 문서 목록
  - quickstart에 포함한 명령 예시와 선택 이유
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 후속 제안(`portfolio copy`, `demo`, `evidence export` 관점)
