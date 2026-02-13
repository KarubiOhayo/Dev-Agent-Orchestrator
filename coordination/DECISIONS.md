# Architecture Decisions

## D-001 라우팅 기본 모드
- Date: 2026-02-11
- Decision: 기본 모드는 `BALANCED`
- Rationale: 비용/품질 균형을 유지하면서 운영 리스크 최소화
- Consequence: 특수 요구는 요청 단위 모드 전환으로 처리

## D-002 CODE/REFACTOR 기본 모델
- Date: 2026-02-11
- Decision: `BALANCED`에서 `openai:gpt-5.2-codex` 사용
- Rationale: 코드 생성/수정 작업 품질 향상
- Consequence: strict-json escalation이 켜진 요청은 openai:gpt-5.2가 우선될 수 있음

## D-003 Gemini 3 Preview 운영 정책
- Date: 2026-02-11
- Decision: `GEMINI3_CANARY`는 일부 에이전트(CODE/DOC)만 허용
- Rationale: Preview 모델 리스크를 제한된 범위에서 검증
- Consequence: 미지원 agent의 canary 요청은 default mode fallback

## D-004 상태 저장소
- Date: 2026-02-11
- Decision: RunState는 SQLite 우선, 실패 시 파일 fallback
- Rationale: 운영 시 조회 가능성과 내구성 확보 + 로컬 실행 안정성
- Consequence: `storage/`는 형상관리 제외

## D-005 파일 적용 정책
- Date: 2026-02-11
- Decision: `apply=false` 기본, `apply=true`일 때만 파일 쓰기
- Rationale: 안전한 검토 우선 워크플로우
- Consequence: 사용자 액션으로만 실제 파일 변경 발생

## D-006 프롬프트 계층
- Date: 2026-02-11
- Decision: `global -> agent -> project-global -> project-agent`
- Rationale: 오케스트레이터 공통 규칙과 프로젝트 커스터마이징 분리
- Consequence: 멀티 프로젝트 운영 시 재사용성 향상

## D-007 Spec 입출력 경로 안전 정책
- Date: 2026-02-11
- Decision: `specInputPath`, `specOutputPath`는 `targetProjectRoot` 내부 상대경로만 허용
- Rationale: 멀티 프로젝트 분리 보장 및 path traversal/임의 파일 접근 차단
- Consequence: 절대경로 및 `..` 경로는 `IllegalArgumentException`으로 거부되며 run-state 실패 이벤트에 사유 기록

## D-008 Code 출력 스키마 우선순위
- Date: 2026-02-11
- Decision: CodeAgent 출력 파싱은 `JSON(files[])` 우선, 실패 시 markdown fallback 허용
- Rationale: 구조적 응답 안정성을 높이되 기존 프롬프트/모델 변동에 대한 호환성 유지
- Consequence: markdown fallback 사용 시 `CODE_OUTPUT_FALLBACK_WARNING` 이벤트를 기록해 운영 관측성 확보

## D-009 CLI 운영 위치
- Date: 2026-02-11
- Decision: `devagent` 스크립트 + Spring Boot `ApplicationRunner` 기반 draft CLI를 기본 진입점으로 사용
- Rationale: 반복 `curl` 의존을 줄이고 실행 결과(runId/model/file 결과)를 사람이 읽기 쉬운 포맷으로 제공
- Consequence: 현재는 사람이 읽기 위한 출력이 우선이며, `--json`/저소음 로그 UX는 후속 개선 항목으로 관리

## D-010 CLI 오류 출력 정책
- Date: 2026-02-11
- Decision: CLI 오류는 `DevAgentCliRunner` 내부에서 처리해 사용자 메시지 중심으로 출력하고, boot 로그/스택트레이스 노출을 최소화
- Rationale: 명령형 UX에서 오류 원인을 빠르게 인지할 수 있도록 신호 대비 잡음을 줄이기 위함
- Consequence: 앱 내부 종료 코드는 `ExitCodeGenerator`로 유지되지만, `gradlew bootRun` 래퍼 경유 시 셸 종료코드는 `1`로 관찰될 수 있어 후속 래퍼 개선이 필요

## D-011 CLI 실행 래퍼 정책
- Date: 2026-02-11
- Decision: `devagent` 스크립트는 `bootRun` 대신 `bootJar -> java -jar` 경로를 사용
- Rationale: CLI 종료코드를 셸에 정확히 전달해 자동화/스크립팅 신뢰성을 확보
- Consequence: unknown option 등 CLI 오류는 종료코드 `2`가 유지되며, 대신 실행 시 jar 빌드 체크 비용이 증가할 수 있음

## D-012 Code -> Doc 체이닝 운영 정책
- Date: 2026-02-11
- Decision: `chainToDoc=true`일 때 Code 완료 후 DocAgent를 연쇄 실행한다. 체인 실패 전파는 `chainFailurePolicy`를 따른다.
- Rationale: 기본 fail-fast 호환성을 유지하면서도, 운영 요구에 따라 부분 성공을 명시적으로 허용하기 위함
- Consequence:
  - 기본값 `FAIL_FAST`에서는 체인 실패가 Code 요청 실패로 전파된다.
  - `PARTIAL_SUCCESS`에서는 체인 실패를 `chainFailures[]`에 기록하고 Code 응답은 성공 반환된다.
  - run-state에 `CHAIN_DOC_TRIGGERED/DONE/FAILED`를 기록한다.

## D-013 조정 체계 분리(3-Thread)
- Date: 2026-02-12
- Decision: 조정 체계를 `Main-Control(분배/승인)` + `Review-Control(리뷰 전담)` + `Executor(구현 전담)`로 분리
- Rationale: 메인 스레드 컨텍스트 소모를 줄이고 리뷰 품질을 안정적으로 유지
- Consequence: 메인은 상세 코드리뷰를 직접 수행하지 않고 review 리포트를 승인 판단 근거로 사용

## D-014 Stateless 라운드 운영
- Date: 2026-02-12
- Decision: 각 스레드는 라운드 시작 시 핵심 문서를 재로딩하고 세션 기억에 의존하지 않음
- Rationale: 세션 교체/중단 상황에서도 일관된 판단과 추적성을 유지
- Consequence: `TASK_BOARD/DECISIONS/REPORTS` 최신화 discipline이 운영 품질의 핵심 전제가 됨

## D-015 스레드 간 프롬프트 릴레이 자동 생성
- Date: 2026-02-12
- Decision: 라운드마다 `Executor -> Review -> Main` 순서의 전달 프롬프트를 `coordination/RELAYS/`에 자동 생성한다.
- Rationale: 스레드 간 컨텍스트 전달 누락을 줄이고, 리뷰/승인 입력을 표준 포맷으로 고정하기 위함
- Consequence:
  - Executor 완료 후 `coordination/RELAYS/H-XXX-executor-to-review.md` 생성
  - Review 완료 후 `coordination/RELAYS/H-XXX-review-to-main.md` 생성
  - Main은 릴레이 프롬프트를 요약 입력으로 사용하되 최종 판단은 원본 result/review 보고서 대조로 확정

## D-016 Code -> Review 체이닝 운영 정책
- Date: 2026-02-12
- Decision: `chainToReview=true`일 때 Code 완료 후 ReviewAgent를 연쇄 실행한다. 체인 실패 전파는 `chainFailurePolicy`를 따른다.
- Rationale: 리뷰 체인도 Doc 체인과 동일한 정책 계약을 공유해 API 해석 일관성을 유지하기 위함
- Consequence:
  - run-state에 `CHAIN_REVIEW_TRIGGERED/DONE/FAILED`를 기록
  - Review 출력 파싱 fallback 시 `REVIEW_OUTPUT_FALLBACK_WARNING` 이벤트를 기록
  - 기본값 `FAIL_FAST`에서는 체인 실패가 Code 요청 실패로 전파된다.
  - `PARTIAL_SUCCESS`에서는 체인 실패를 `chainFailures[]`에 기록하고 Code 응답은 성공 반환된다.

## D-017 H-XXX Placeholder 해석 규칙
- Date: 2026-02-13
- Decision: `H-XXX` 표기는 템플릿 placeholder이며, 실행 시에는 대상 라운드의 최신 실제 파일(`H-00N-*`)로 치환한다.
- Rationale: 자동화/에이전트가 placeholder를 literal path로 해석해 파일 탐색에 실패하는 문제를 방지
- Consequence:
  - Main/Review/Executor 프롬프트와 Task Board에 해석 규칙을 명시
  - 릴레이/리포트 탐색 로직은 항상 실제 번호 파일을 대상으로 동작

## D-018 strict-json 기본값/적용조건 정책
- Date: 2026-02-13
- Status: Approved (Scope: routing layer)
- Decision:
  - `RouteRequest.strictJsonRequired`는 기본적으로 "명시 요청 기반"으로 동작하도록 설계한다(기본 escalation 비활성 후보).
  - strict-json escalation은 `strictJsonRequired=true`가 명시된 요청에서 우선 적용한다.
  - escalation 우선순위 후보는 `review-high-risk -> strict-json -> large-context -> mode-primary -> mode-fallbacks` 순으로 관리한다.
- Rationale: 현재 기본 strict-json 활성은 CODE/REFACTOR의 기본 모델 선택을 덮어쓸 수 있어 의도치 않은 비용/품질 변동을 유발할 수 있음.
- Consequence:
  - H-007에서 `RouteRequest`/`ModelRouter` 및 회귀 테스트로 정책이 반영되었다.
  - Agent 요청 DTO의 `strictJsonRequired` 기본값 정합성은 H-010에서 API 계약 관점으로 추가 정리한다.

## D-019 체인 실패 전파 정책(API 계약)
- Date: 2026-02-13
- Status: Approved
- Decision:
  1. 기본 정책은 `FAIL_FAST`로 유지한다(정책 미지정/null 포함).
  2. 선택 정책 `PARTIAL_SUCCESS`를 지원한다.
  3. `PARTIAL_SUCCESS`에서는 체인 실패를 `chainFailures[]`(`agent`, `failedStage`, `errorMessage`)로 응답에 포함하고 Code 응답은 성공 반환한다.
  4. run-state 이벤트(`CHAIN_*_TRIGGERED/DONE/FAILED`)는 정책 모드와 무관하게 유지한다.
- Rationale: 하위 호환성과 운영 단순성(기본 fail-fast)을 유지하면서, 부분 성공 활용 시나리오를 안전하게 확장하기 위함.
- Consequence:
  - D-012/D-016은 `chainFailurePolicy` 기반 계약으로 정합화한다.
  - 클라이언트는 `PARTIAL_SUCCESS` 사용 시 `chainFailures[]` 확인을 필수로 처리해야 한다.

## D-020 파일 적용 경계 검증 정책
- Date: 2026-02-13
- Status: Approved
- Decision:
  - 파일 적용 경계 검증은 `relative path + normalize + root startsWith` 사전 검증을 기본으로 유지한다.
  - 실제 쓰기 직전에는 `toRealPath` 기반 실경로 재검증을 수행해 심볼릭 링크 경유 우회를 차단한다.
  - 경계 위반은 `REJECTED`, 파일시스템 I/O 실패는 `ERROR`로 상태를 분리한다.
- Rationale: H-008 리뷰 P2에서 확인된 심볼릭 링크 경계 우회 가능성을 H-008.1에서 차단해 파일 반영 안전성의 최소 기준을 확보함.
- Consequence:
  - `FileApplyService`의 경계 검증은 단순 prefix 비교를 넘어 실경로 재검증을 포함한다.
  - 신규/회귀 테스트(`rejectSymlinkBoundaryBypass` 포함)로 경계 계약을 고정한다.

## D-021 Main -> Executor 릴레이 자동 생성
- Date: 2026-02-13
- Status: Approved
- Decision:
  - Main은 다음 라운드 시작 시 handoff 확정 직후 `coordination/RELAYS/H-00N-main-to-executor.md`를 생성한다.
  - 생성 포맷은 `coordination/RELAYS/TEMPLATE-main-to-executor.md`를 표준으로 사용한다.
  - main-controller의 "반드시 3개 출력" 규칙은 유지하고, 2번 출력에 relay 생성 내용을 포함한다.
- Rationale: 스레드 간 컨텍스트 복붙/누락을 줄이고, Executor 시작 입력을 라운드 단위로 고정하기 위함.
- Consequence:
  - 목적: Main 승인 판단 이후 실행 지시를 표준 릴레이로 즉시 연결한다.
  - 결과: 릴레이 체계가 `Main -> Executor -> Review -> Main` 3종으로 정합화된다.
  - 파일경로:
    - 템플릿: `coordination/RELAYS/TEMPLATE-main-to-executor.md`
    - 라운드 파일: `coordination/RELAYS/H-00N-main-to-executor.md`
    - 운영 규칙 반영: `coordination/PROMPTS/main-controller.md`, `coordination/TASK_BOARD.md`

## D-022 Automations Plan A (Report-Only)
- Date: 2026-02-13
- Status: Approved
- Decision:
  - Automations 범위는 점검/요약/발견사항 보고(report-only)로 한정한다.
  - 자동 파일수정, 자동 커밋, 자동 PR/웹훅 연동은 금지한다.
- Rationale: 로컬 단독 운영 단계에서 자동 변경 리스크를 차단하고 운영 신뢰도를 우선 확보하기 위함.
- Consequence:
  - Automations 출력은 inbox 보고를 기본으로 한다.
  - 운영 템플릿은 버전관리 목적으로 레포에 보관한다.
  - 템플릿 경로: `coordination/AUTOMATIONS/A-001-nightly-test-report.md`, `coordination/AUTOMATIONS/A-002-doc-drift-check.md`, `coordination/AUTOMATIONS/A-003-relay-watch.md`
