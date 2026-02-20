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
- Status: Approved (Extended by D-021)
- Decision:
  - 라운드마다 `coordination/RELAYS/`에 전달 릴레이를 자동 생성한다.
  - D-015의 범위는 `Executor -> Review`, `Review -> Main` 2종을 다룬다.
  - `Main -> Executor` 릴레이는 D-021에서 추가되었으며,
    현재 표준 릴레이 체계는 `Main -> Executor -> Review -> Main` 3종이다.
- Rationale: 스레드 간 컨텍스트 전달 누락을 줄이고, 리뷰/승인 입력을 표준 포맷으로 고정하기 위함.
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

## D-023 API 입력검증/오류 응답 계약 표준화
- Date: 2026-02-19
- Status: Approved (H-010 Scope)
- Decision:
  - Agent API(`POST /api/routing/resolve`, `POST /api/agents/*/generate`)의 입력 오류는 HTTP 400 + 공통 오류 envelope로 반환한다.
  - 공통 오류 envelope는 최소 `code`, `message`, `path`, `timestamp`를 포함하고, 필드 단위 오류가 있으면 `details[]`를 포함한다.
  - `chainFailurePolicy=PARTIAL_SUCCESS`는 실패 응답으로 격하하지 않고 기존 성공 응답 + `chainFailures[]` 계약을 유지한다.
- Rationale: endpoint/예외 유형별로 분산된 오류 포맷을 통일해 클라이언트 분기 비용과 운영 해석 불일치를 줄이기 위함.
- Consequence:
  - H-010에서 컨트롤러/예외 핸들러/테스트/문서를 함께 갱신한다.
  - API 문서에 `PARTIAL_SUCCESS` 사용 시 `chainFailures[]` 필수 확인 규약을 명시한다.
  - 빌드 설정/공통 설정 파일 변경이 필요하면 범위를 분리해 Main 사전 승인 후 진행한다.
  - H-010 리뷰에서 문서-구현 오류 코드 매핑(`INVALID_JSON_REQUEST`) 및 복합 필수조건 `details.field` 표현 정합성 보강이 후속 라운드(H-010.1) 과제로 확정되었다.

## D-024 필수값 오류 코드 세분화 및 any-of details 규약
- Date: 2026-02-19
- Status: Approved (H-010.1 Scope)
- Decision:
  - 필수값 누락 오류는 단일 필드와 복합(any-of) 조건을 분리해 표준화한다.
  - 단일 필드 누락은 `MISSING_REQUIRED_FIELD`를 유지한다.
  - 복합(any-of) 필수 누락은 `MISSING_REQUIRED_ANY_OF`를 사용한다.
  - 복합(any-of) 오류의 `details[]`는 후보 필드별 항목으로 생성하고 `reason=any_of_required`를 사용한다.
  - API 문서 400 매핑 표에는 `INVALID_JSON_REQUEST`를 구현 반환 코드와 동기화해 누락 없이 유지한다.
- Rationale: H-010 review에서 확인된 문서-구현 코드 집합 불일치와 복합 필수조건 파싱 모호성을 제거해 클라이언트 분기 일관성을 확보하기 위함.
- Consequence:
  - 예외 처리와 테스트가 단일/복합 필수 누락을 구분해 검증하도록 고정된다.
  - 오류 응답 소비자는 any-of 케이스에서 `details[]`의 다중 필드 정보를 안정적으로 파싱할 수 있다.
  - H-010.1 완료로 H-010 보류 이슈(P2/P3)가 해소되어 Main 승인(Go) 판단이 가능해졌다.

## D-025 Spec fallback warning 관측성 계약
- Date: 2026-02-19
- Status: Approved (H-012 Scope)
- Decision:
  - spec 출력 파싱 source는 `DIRECT_JSON`, `JSON_CODE_BLOCK`, `FALLBACK_SCHEMA` 3종으로 구분한다.
  - run-state 경고 이벤트 `SPEC_OUTPUT_FALLBACK_WARNING`은 `DIRECT_JSON`이 아닌 source에서만 기록한다.
  - 이벤트 메시지 형식은 `source=<PARSE_SOURCE>`로 고정한다.
- Rationale: code/doc/review 대비 비어 있던 spec fallback 관측 경로를 동일 계약으로 정렬해 에이전트 간 운영 지표 비교 가능성을 확보하기 위함.
- Consequence:
  - spec fallback 경로가 run-state 이벤트로 남아 운영 점검 범위가 `CODE/SPEC/DOC/REVIEW` 4종으로 정합화된다.
  - 다음 라운드(H-013)에서 warning 이벤트 집계 기준(모수/경고율/임계치/알림 룰) 문서화 작업이 가능해졌다.

## D-026 fallback warning 모수 집계 단위 정합화(Code 체인 포함)
- Date: 2026-02-19
- Status: Approved (H-014.1 Scope)
- Decision:
  - `parseEligibleRunCount`는 agent 서비스 run 기준으로 집계한다.
  - Code 모수는 직접 `POST /api/agents/code/generate` 호출 run과 Spec 체인(`chainToCode=true`)으로 내부 `CodeAgentService`가 실행된 run을 모두 포함한다.
  - Spec/Doc/Review 모수 정의도 동일 원칙(직접 호출 + 해당 체인 실행 포함)으로 유지한다.
- Rationale: H-014 리뷰에서 확인된 Code 모수 정의 충돌을 제거해 fallback warning `warningRate` 분모 해석을 단일화하기 위함.
- Consequence:
  - 운영 문서(`docs/code-agent-api.md`, 자동 점검 템플릿)는 Code 체인 포함 기준으로 동기화한다.
  - `INSUFFICIENT_SAMPLE` 제외 규칙 및 임계치 값(`0.05`, `0.15`)은 변경하지 않는다.

## D-027 fallback warning 임계치 보정 준비 단계 운영 정책
- Date: 2026-02-19
- Status: Approved (H-015 Scope)
- Decision:
  - 임계치/알림 룰 보정 전 최소 14일 운영 데이터 가용성(집계 성공/실패/샘플 부족)을 선행 점검한다.
  - H-015 라운드는 준비 단계로 제한하고, 임계치/알림 룰 수치(`0.05`, `0.15`, `0.10`)는 변경하지 않는다.
  - `INSUFFICIENT_SAMPLE`(`parseEligibleRunCount < 20`) 비중과 집계 불가 원인은 보정 후보와 분리해 별도 보고한다.
- Rationale: 데이터 가용성 검증 없이 임계치를 조정하면 오탐/미탐 위험과 운영 해석 변동성이 커질 수 있기 때문이다.
- Consequence:
  - `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 보정 준비 출력 항목(14일 가용성/샘플 부족/집계 불가 분류)이 반영된다.
  - 실제 임계치/알림 룰 수치 조정은 후속 보정 라운드에서 수행한다.

## D-028 fallback warning 보정 실행 보류/유지 정책
- Date: 2026-02-19
- Status: Approved (H-016 Scope)
- Decision:
  - H-016 실측 실행 결과, 최근 14일(2026-02-06~2026-02-19, KST) `INSUFFICIENT_SAMPLE` 비율이 `1.00`이면 보정을 `보류`로 판정한다.
  - 보류 판정 시 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 변경하지 않고 유지한다.
  - 보류 판정 시 후보값 산정/적용 전후 비교는 수행하지 않으며, 미충족 게이트와 수치 근거를 문서에 고정한다.
  - 후속 라운드는 보정 재착수 전제조건(모수 확보 계획, 재보정 착수 조건) 문서화에 집중한다.
- Rationale: 표본 충분 일수(`parseEligibleRunCount >= 20`)가 14일 중 0일인 상태에서 임계치 후보를 산정하면 오탐/미탐 이동을 검증할 수 없어 의사결정 신뢰도가 낮아지기 때문이다.
- Consequence:
  - `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`는 보류 분기와 수치 유지 규칙을 운영 기준으로 사용한다.
  - 다음 라운드(H-017)는 샘플 확보 계획/추적 지표/재보정 착수 조건을 고정하는 작업으로 진행한다.

## D-029 fallback warning 재보정 착수 게이트/Projection 추적 정책
- Date: 2026-02-19
- Status: Approved (H-017 Scope)
- Decision:
  - H-016 기준선(`INSUFFICIENT_SAMPLE` 비율 `1.00`, 샘플 충분 일수 0일)을 기준으로 H-017 샘플 확보 정량 목표를 운영 게이트로 사용한다.
  - 재보정 착수 가능 조건은 최근 14일 기준 `집계 성공 >= 10`, `INSUFFICIENT_SAMPLE <= 0.50`, `집계 불가 < 3`, `샘플 충분 일수(parseEligibleRunCount >= 20) >= 7`로 고정한다.
  - H-017 이후 운영 점검은 `parseEligibleRunCount` 추세(전체 + agent별), 목표 대비 진행률, Projection(`requiredSufficientDays`)을 함께 보고한다.
  - 게이트 미충족 시 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)는 유지하고, 원인 분류(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`, `COLLECTION_FAILURE`) 기반 보완 액션을 우선한다.
- Rationale: 보정 보류 상태를 해소하려면 임계치 자체 조정보다 샘플 확보와 착수 시점 예측 정확도를 먼저 관리해야 하며, 단일 수치 보고만으로는 착수 가능 시점 판단이 불안정하기 때문이다.
- Consequence:
  - `docs/code-agent-api.md`는 기준선/목표/분기/Projection 규칙을 기준 문서로 유지한다.
  - `coordination/AUTOMATIONS/A-001-nightly-test-report.md`는 추세/진행률/Projection/다음 액션을 필수 출력으로 사용한다.
  - 다음 라운드(H-018)는 H-017 계획의 운영 적용 점검(실측 추세, Projection 오차, 재보정 착수 시점 추적)으로 진행한다.

## D-030 fallback warning 진행률 표기/착수 게이트 문서 계약 고정
- Date: 2026-02-19
- Status: Approved (H-018.1 Scope)
- Decision:
  - 목표 대비 진행률 표기는 `집계 성공 달성률 = min(1, 집계 성공 일수 / 10)`으로 고정하고, 표기 범위는 `0~100%`로 제한한다.
  - 목표 초과 정보는 진행률에 합산하지 않고 `목표 초과 일수 = max(0, 집계 성공 일수 - 10)`로 분리 표기한다.
  - 재보정 착수/보류 판정은 4개 게이트(`집계 성공`, `INSUFFICIENT_SAMPLE`, `집계 불가`, `샘플 충분 일수`)를 단일 기준으로 유지한다.
  - 운영 문서(`docs/code-agent-api.md`), 자동 점검 템플릿(`coordination/AUTOMATIONS/A-001-nightly-test-report.md`), 라운드 결과 보고(`coordination/REPORTS/H-018-result.md`)는 동일 계약으로 동기화한다.
- Rationale: H-018 리뷰에서 확인된 진행률 과대 표기(140%)와 3개/4개 게이트 혼재를 해소하지 않으면 라운드 간 추세 비교 및 재보정 착수 판정의 일관성이 깨지기 때문이다.
- Consequence:
  - H-018.1에서 산식/게이트 문구가 정합화되어 Review `Go`가 확정되었다.
  - 이후 라운드는 동일 산식/게이트 계약을 기준으로 재보정 착수 가능/보류를 판단한다.
  - 계약 드리프트 발생 시 Main은 후속 정합화 라운드를 우선 배정한다.

## D-031 fallback warning 실행률 추적 계약(샘플 확보 단계)
- Date: 2026-02-19
- Status: Approved (H-019 Scope)
- Decision:
  - H-019 결과에서 `recalibrationReadiness=HOLD`가 유지되더라도 `READY/HOLD + unmetGates` 보고 계약은 변경하지 않는다.
  - 샘플 확보 단계의 실행력 측정을 위해 일일 목표(`CODE 16`, `SPEC 4`, `DOC 6`, `REVIEW 6`) 대비 실제 실행량/달성률을 운영 문서에 고정한다.
  - 실행률 산식은 agent별 `achievementRate = min(1, actualRuns/targetRuns)`, 전체 `overallExecutionRate = min(1, totalActualRuns/32)`로 고정한다.
  - Projection은 기존 전제조건(`최근 3일 평균 전체 모수 >= 32`) 미충족 시 `예상 재보정 착수 가능일`을 미산정 처리하고 사유를 명시한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙은 유지한다.
- Rationale: H-019에서 게이트 미충족이 지속된 원인이 임계치 조정 문제가 아니라 샘플 유입 부족(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`)임이 재확인되어, 재보정 착수 전 실행률 지표를 표준 출력으로 고정할 필요가 있다.
- Consequence:
  - H-020은 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 실행률 필드/산식을 동기화하는 라운드로 진행한다.
  - 이후 Main 판단은 게이트 충족 여부와 실행률 추세를 함께 비교해 `READY/HOLD`를 확정한다.

## D-032 fallback warning 실행량 증대 검증용 호출 믹스 추적 정책
- Date: 2026-02-19
- Status: Approved (H-020 Scope)
- Decision:
  - H-020에서 고정한 `READY/HOLD + unmetGates + executionRate` 보고 계약은 유지한다.
  - 최근 7일 실행량은 `직접 호출`과 `체인 호출`로 분리 집계하고, agent별 `directRuns`, `chainRuns`, `chainShare`를 필수 보고 항목으로 고정한다.
  - `chainShare`는 `chainRuns / (directRuns + chainRuns)`로 계산하며, 분모가 `0`이면 `0`으로 처리한다.
  - `LOW_TRAFFIC`는 전체 실행량/실행률 추세로, `CHAIN_COVERAGE_GAP`는 `DOC/REVIEW`의 `chainRuns`·`chainShare`로 분리 판정한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- Rationale: H-020 결과에서 최근 7일 누적 실행률이 `0.45%`(`1/224`), `DOC/REVIEW` 실행이 `0`으로 확인되어 총량 지표만으로는 병목(직접 호출 부족 vs 체인 커버리지 부족)을 분리 판단하기 어렵다.
- Consequence:
  - H-021은 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 호출 믹스 출력 계약을 동기화하는 라운드로 진행한다.
  - 이후 Main 판단은 14일 게이트와 함께 `direct vs chain` 비중 추세를 비교해 `READY/HOLD` 전환 가능성을 평가한다.

## D-033 fallback warning 실행량 회복 액션 플랜 운영 정책
- Date: 2026-02-19
- Status: Approved (H-021 Scope)
- Decision:
  - H-021 결과에서 `recalibrationReadiness=HOLD`가 유지되면, 다음 라운드는 호출 믹스 관측 자체보다 실행량 회복 액션 플랜 수립/추적을 우선한다.
  - 실행량 회복 평가는 최근 7일 agent별 `directRuns`, `chainRuns`, `totalActualRuns`, `chainShare`를 목표 대비 gap으로 함께 보고한다.
  - `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 분류는 유지하되, `HOLD` 지속 시에는 일일 실행 목표 증량 액션(직접 호출 + 체인 호출)을 우선순위로 제시한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- Rationale: H-021에서 호출 믹스 분리 근거는 확보되었지만 최근 7일 누적 실행률 `0.45%`와 `DOC/REVIEW chainRuns=0` 상태가 유지되어, 지표 관측만으로는 `HOLD` 해소가 어렵기 때문이다.
- Consequence:
  - H-022는 실행량 회복 액션 플랜(일일 목표, 목표-실적 gap, 우선순위 액션)을 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 동기화한다.
  - Main 판단은 14일 게이트 + 실행량 회복 진행률(gap 축소 여부)을 함께 근거로 사용한다.

## D-034 fallback warning 실행량 회복 액션 이행률 추적 정책
- Date: 2026-02-19
- Status: Approved (H-022 Scope)
- Decision:
  - H-022에서 고정한 `executionRecoveryPlan`/`executionRecoveryProgress` 출력 계약은 유지한다.
  - 다음 라운드는 최근 7일 기준 `executionGap`, `chainShareGap`의 절대값과 추세(직전 7일 대비 delta)를 함께 보고해 실행량 회복 이행률을 검증한다.
  - `HOLD`가 지속되면 액션 우선순위(`LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP`/`COLLECTION_FAILURE`)별로 `status(IN_PROGRESS|BLOCKED|DONE)`와 근거(`runId`/집계표)를 함께 고정한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- Rationale: H-022에서도 최근 7일 누적 실행률이 `0.45%`(`1/224`), `executionGap=223`, `DOC/REVIEW chainRuns=0` 상태가 유지되어 플랜 수립만으로는 회복 신호를 검증할 수 없기 때문이다.
- Consequence:
  - H-023은 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 gap 추세/액션 이행 상태 출력을 동기화하는 라운드로 진행한다.
  - Main 판단은 14일 게이트 판정과 함께 `executionGap`/`chainShareGap` 축소 추세 및 액션 이행 근거를 공동 기준으로 사용한다.

## D-035 fallback warning 실행량 회복 액션 최소 이행률 하한선/증거 ledger 정책
- Date: 2026-02-19
- Status: Approved (H-023 Scope)
- Decision:
  - H-023 결과(`executionGapDelta=+3`, `chainShareGapDelta=0.00%p`) 기준으로 `HOLD` 지속 시점에는 추세 지표만으로 개선 여부를 판단하지 않고 일일 최소 이행률 하한선 점검을 추가한다.
  - H-024부터 최근 7일 일자별 `dailyExecutionBaseline`(agent별 최소 direct/chain/total run 하한선) 대비 실측 PASS/FAIL과 `weeklyComplianceRate = compliantDays / 7`를 필수 출력으로 고정한다.
  - `weeklyComplianceRate` 단계 분류는 `ON_TRACK(>=0.70)`, `AT_RISK(>=0.40, <0.70)`, `OFF_TRACK(<0.40)`로 표준화한다.
  - `HOLD`가 지속되면 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP`/`COLLECTION_FAILURE` 원인별 `recoveryEvidenceLedger`(`status`, `evidenceRef(runId/집계표)`, `owner`, `updatedAt`)를 함께 고정한다.
  - 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- Rationale: H-023에서도 14일 게이트 미충족과 delta 미개선이 동시에 유지되어, 액션 이행 증거 누락을 줄이고 회복 진행 상태를 일 단위로 판정할 운영 기준이 필요하기 때문이다.
- Consequence:
  - H-024는 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`에 하한선/이행률/증거 ledger 출력 계약을 동기화한다.
  - Main 판단은 기존 14일 게이트 + gap 추세에 더해 `weeklyComplianceRate` 단계와 원인별 evidence ledger 충족 여부를 함께 확인한다.

## D-036 fallback-warning(H-024) 트랙 동결 및 다음 트랙 전환
- Date: 2026-02-19
- Status: Approved
- Decision: H-024는 동결하고, H-025(체이닝/CLI)로 우선순위를 전환한다.
- Rationale: 14일 게이트 HOLD(샘플 충분 일수 0일) + DOC/REVIEW chainRuns=0 상태에서 H-024 거버넌스 강화는 ROI가 낮다.
- Consequence:
  - fallback-warning 관련 문서/자동화는 현 상태 유지(변경 최소화)로 운영한다.
  - 신규 기능은 체이닝/CLI 사용성 개선(H-025)에 투자한다.

## D-037 Spec -> Code 체인 옵션 전파 및 CLI chainFailures 노출 계약 고정
- Date: 2026-02-19
- Status: Approved (H-025 Scope)
- Decision:
  - Spec 요청은 Code 체인 확장 옵션(`codeChainToDoc`, `codeDocUserRequest`, `codeChainToReview`, `codeReviewUserRequest`, `codeChainFailurePolicy`)을 지원하고, Spec -> Code 체인 시 `CodeGenerateRequest`로 동일 의미를 전파한다.
  - CLI `generate/spec`는 Doc/Review 체인 옵션과 `chainFailurePolicy`를 모두 노출하며, human/json summary에 `chainedDoc`, `chainedReview`, `chainFailures`를 고정 출력한다.
  - CLI json 출력은 기존 `summary`, `fileResults` 호환을 유지하면서 구조화 `data.chainFailures[]`를 추가 제공한다.
- Rationale: 원샷 체이닝 사용성 개선과 함께 `PARTIAL_SUCCESS`에서 체인 실패 누락(소비자 미확인) 리스크를 출력 계약으로 완화해야 운영 관측성과 회귀 점검 신뢰도를 높일 수 있기 때문이다.
- Consequence:
  - H-025 결과는 리뷰 `Go`로 승인되며, 체이닝/CLI 계약이 운영 기준으로 승격된다.
  - 다음 라운드(H-026)는 Spec/CLI 원샷 체이닝 경로의 E2E 계약 테스트(특히 `PARTIAL_SUCCESS` + `chainFailures[]` 소비 검증) 보강으로 진행한다.

## D-038 Spec/CLI 원샷 체이닝 E2E 계약 테스트 기준 고정
- Date: 2026-02-19
- Status: Approved (H-026 Scope)
- Decision:
  - Spec -> Code -> (Doc/Review) 원샷 체이닝 E2E 회귀 테스트는 `PARTIAL_SUCCESS` 사용 시 `chainFailures[]` 노출을 API 응답(`chainedCodeResult`)과 CLI 출력(human/json) 양쪽에서 검증한다.
  - CLI `generate/spec` 테스트는 `summary`(`chainedDoc`, `chainedReview`, `chainFailures`)와 JSON `data.chainFailures[]`를 함께 검증한다.
  - 신규 옵션 미사용(default) 경로는 `FAIL_FAST` 및 체인 off 기본값 유지 회귀를 필수 검증 항목으로 고정한다.
- Rationale: H-025에서 계약을 확장했어도 E2E 회귀 검증이 약하면 `PARTIAL_SUCCESS`의 체인 실패 신호가 경로별(API/CLI)로 누락될 위험이 남기 때문이다.
- Consequence:
  - H-026 결과는 테스트 게이트 통과 + 리뷰 `Go`로 Main 승인 조건을 충족한다.
  - 다음 라운드(H-027)는 소비자 누락 리스크를 더 줄이기 위해 CLI 소비 가드레일(`--fail-on-chain-failures`)과 운영 문서 체크리스트를 보강한다.

## D-039 CLI `PARTIAL_SUCCESS` 소비 가드레일/종료코드 계약 고정
- Date: 2026-02-19
- Status: Approved (H-027 Scope)
- Decision:
  - CLI `generate/spec`는 `--fail-on-chain-failures=<true|false>` 옵션을 지원하고 기본값은 `false`로 유지한다.
  - `--fail-on-chain-failures=true` + `chainFailures[] > 0` 조합에서는 성공 출력(human/json)을 유지하면서 종료코드 `3`을 반환한다.
  - `chainFailures[] = 0`이거나 옵션 미사용/`false`인 경로는 기존 성공 종료코드(`0`)를 유지한다.
  - human 출력은 체인 실패 존재 시 경고 1줄을 추가하고, json 출력은 기존 필드 호환을 유지하면서 `data.hasChainFailures` 보조 필드를 제공한다.
- Rationale: `PARTIAL_SUCCESS`의 성공 응답 유지 정책 때문에 소비자가 `chainFailures[]`를 누락 확인하면 실패를 간과할 수 있어, 자동화에서 선택적으로 강제 실패 신호를 사용할 수 있는 계약이 필요하다.
- Consequence:
  - H-027 결과는 테스트 게이트 통과 + 리뷰 `Go`로 Main 승인 조건을 충족한다.
  - 다음 라운드(H-028)는 자동화/CI 소비자 관점에서 `exit code 3` 처리 체크리스트와 샘플 파이프라인 검증을 고정한다.

## D-040 CLI 가드레일 소비 신호(`guardrailTriggered`) 및 CI 점검 계약 고정
- Date: 2026-02-19
- Status: Approved (H-028 Scope)
- Decision:
  - CLI `generate/spec` JSON 출력 `data`에 `guardrailTriggered` 보조 필드를 추가한다.
  - `guardrailTriggered=true` 조건은 `--fail-on-chain-failures=true` + `chainFailures[] > 0` + 종료코드 `3` 조합으로 고정한다.
  - human 경고 문구는 체인 실패 시 `guardrail=enabled|disabled` 상태를 1줄로 명시한다.
  - 자동화/CI 소비 규약은 종료코드와 함께 JSON 신호(`chainFailures[]`, `hasChainFailures`, `guardrailTriggered`)를 동시 점검하도록 문서화한다.
  - opt-in 정책(`--fail-on-chain-failures` 기본값 `false`)과 API 서버 응답 계약(`PARTIAL_SUCCESS`, `chainFailures[]`)은 유지한다.
- Rationale: 종료코드만 또는 본문만 단독 소비하는 자동화 경로에서 체인 실패 해석 불일치가 발생할 수 있어, 기계 소비 신호와 운영 체크리스트를 동시에 고정할 필요가 있다.
- Consequence:
  - H-028 결과는 테스트 게이트 통과 + 리뷰 `Go`로 Main 승인 조건을 충족한다.
  - 다음 라운드(H-029)는 fallback-warning H-024 동결 트랙 재개 조건 점검(`RESUME_H024/KEEP_FROZEN`)으로 진행한다.

## D-041 fallback-warning H-024 동결 유지(`KEEP_FROZEN`) 및 H-030 이행 추적 전환
- Date: 2026-02-19
- Status: Approved (H-029 Scope)
- Decision:
  - H-029 최신 14일/7일 실측 기준으로 H-024 재개 판정은 `KEEP_FROZEN`으로 고정한다.
  - `KEEP_FROZEN` 판정 근거는 `INSUFFICIENT_SAMPLE_RATIO=1.00`, `SUFFICIENT_DAYS=0`, `weeklyComplianceRate=0.00`, `DOC/REVIEW actualChainRuns=0`으로 유지한다.
  - 기존 게이트/산식/임계치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
  - 다음 라운드(H-030)는 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 신호의 회복 액션 이행 추적(상태/증거/다음 액션) 계약을 운영 문서/자동화 템플릿에 고정한다.
- Rationale: H-029에서도 재개 게이트 4종 중 2종 미충족이 지속되어 H-024를 즉시 재개할 근거가 부족하며, 동결 상태를 유지하더라도 실행량/체인 커버리지 회복 이행의 증거 누적 체계를 먼저 표준화해야 다음 재개 판단의 재현성을 확보할 수 있기 때문이다.
- Consequence:
  - H-024는 Frozen/Backlog 상태를 유지하고 `RESUME_H024` 근거 확보 전까지 재개하지 않는다.
  - H-030은 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md` 중심의 운영 계약 정합화 라운드로 진행한다.
  - Main 판단은 기존 게이트 충족 여부와 함께 신호별 이행 증거 누적 상태를 공동 근거로 사용한다.

## D-042 fallback-warning `KEEP_FROZEN` 후속 점검/재개 판정 정책 고정
- Date: 2026-02-19
- Status: Approved (H-030 Scope)
- Decision:
  - H-030 결과 기준선(`resumeDecision=KEEP_FROZEN`, `recoveryActionCompletionRate=0.00`, `blockedActionCount=2`, `executionGapDelta=+3`, `chainShareGapDelta=0.00%p`)을 유지 기준으로 고정한다.
  - 다음 라운드(H-031)는 최신 14일/7일 실측을 재집계해 `RESUME_H024|KEEP_FROZEN` 단일 판정을 유지하되, 게이트 4종 + 회복 액션 상태(`recoveryActionTracking[]`)를 동시에 검증한다.
  - `RESUME_H024` 전환은 게이트 4종 충족과 함께 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 신호가 `BLOCKED`가 아님(`IN_PROGRESS|DONE`)을 근거로 판단한다.
  - 게이트 미충족 또는 핵심 신호 `BLOCKED` 지속 시 `KEEP_FROZEN`을 유지하고, 임계치/알림 룰 수치(`0.05`, `0.15`, `+0.10p`, `0.10`) 및 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- Rationale: H-030에서 운영 계약 동기화는 완료되었지만 실행량/체인 커버리지 회복 신호가 여전히 개선되지 않아, H-024 재개 판단을 위해서는 게이트 충족 여부와 액션 상태를 함께 보는 후속 점검 기준이 필요하기 때문이다.
- Consequence:
  - H-031은 `docs/code-agent-api.md`와 `coordination/AUTOMATIONS/A-001-nightly-test-report.md`의 기존 계약을 유지한 채 최신 실측/판정 근거를 갱신하는 추적 라운드로 진행한다.
  - Main 판단 문서(`CURRENT_STATUS`, `TASK_BOARD`, `RELAYS`)는 H-031 결과를 기준으로 `RESUME_H024` 재개 여부를 단일값으로 고정한다.
  - H-024는 `RESUME_H024` 판정이 확정되기 전까지 Frozen/Backlog 상태를 유지한다.

## D-043 fallback-warning H-031 승인 및 H-032 신호 개선 실증 데이터 확보 정책
- Date: 2026-02-19
- Status: Approved (H-031 Close-out / H-032 Scope)
- Decision:
  - H-031 결과를 기준으로 `resumeDecision=KEEP_FROZEN` 단일 판정을 유지한다.
  - 다음 라운드(H-032)는 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 신호 개선 여부를 판단할 실증 데이터 확보 계약을 운영 문서와 야간 템플릿에 동일하게 고정한다.
  - H-032부터 신호별 증거 출력은 `signalRecoveryEvidenceLedger[]`를 사용하며 필수 필드는 `signal`, `requiredEvidence`, `observedEvidence`, `evidenceRefs`, `status`, `gapSummary`, `nextAction`, `updatedAt`로 고정한다.
  - 기존 게이트/산식/임계치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- Rationale: H-031에서도 게이트 미충족(14일 `INSUFFICIENT_SAMPLE` 비율 `1.00`, `sufficientDays=0`)과 신호 `BLOCKED` 상태가 지속되어, H-024 재개 판단 전에는 "판정 값"뿐 아니라 "신호 개선 증거"를 표준 포맷으로 누적해야 재현 가능한 의사결정이 가능하기 때문이다.
- Consequence:
  - H-031은 Main 승인(Go)으로 종료하고, 다음 실행 라운드를 `coordination/HANDOFFS/H-032-fallback-warning-keep-frozen-signal-recovery-evidence-acquisition.md`로 확정한다.
  - `coordination/REPORTS/CURRENT_STATUS_2026-02-19.md`, `coordination/TASK_BOARD.md`, `coordination/RELAYS/H-032-main-to-executor.md`는 H-032 기준으로 동기화한다.
  - H-024는 `RESUME_H024` 근거(게이트 충족 + 신호 개선 증거) 확보 전까지 Frozen/Backlog 상태를 유지한다.

## D-044 fallback-warning H-032 승인 및 H-033 실행 증거 누적 점검 정책
- Date: 2026-02-19
- Status: Approved (H-032 Close-out / H-033 Scope)
- Decision:
  - H-032 결과를 기준으로 `resumeDecision=KEEP_FROZEN` 단일 판정을 유지한다.
  - 다음 라운드(H-033)는 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 신호에 대해 실행 증거 누적 상태를 정량 점검하는 `evidenceAccumulationSummary[]` 계약을 운영 문서와 야간 템플릿에 동일하게 고정한다.
  - H-033의 `evidenceAccumulationSummary[]` 필수 필드는 `signal`, `requiredEvidenceCount`, `observedEvidenceCount`, `coverageRate`, `staleEvidenceCount`, `freshEvidenceCount`, `status`, `lastObservedAt`로 고정한다.
  - `signalRecoveryEvidenceLedger[]` 필수 필드와 `RESUME_H024|KEEP_FROZEN` 단일 판정 계약은 유지한다.
  - 기존 게이트/산식/임계치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- Rationale: H-032에서도 `executionGapDelta=+3`, `chainShareGapDelta=0.00%p`, `recoveryActionCompletionRate=0.00`, `blockedActionCount=2`, `signalRecoveryEvidenceLedger=BLOCKED/BLOCKED`가 유지되어 재개 판단 근거를 강화하려면 신호별 증거의 "존재"뿐 아니라 "누적 커버리지/신선도"를 표준 포맷으로 관리해야 하기 때문이다.
- Consequence:
  - H-032는 Main 승인(Go)으로 종료하고, 다음 실행 라운드를 `coordination/HANDOFFS/H-033-fallback-warning-keep-frozen-evidence-accumulation-check.md`로 확정한다.
  - `coordination/REPORTS/CURRENT_STATUS_2026-02-19.md`, `coordination/TASK_BOARD.md`, `coordination/RELAYS/H-033-main-to-executor.md`는 H-033 기준으로 동기화한다.
  - H-024는 `RESUME_H024` 근거(게이트 충족 + 신호 개선 증거 + 증거 누적 커버리지 확보) 전까지 Frozen/Backlog 상태를 유지한다.

## D-045 fallback-warning H-033 승인 및 H-034 신선 증거 복구 추적 정책
- Date: 2026-02-20
- Status: Approved (H-033 Close-out / H-034 Scope)
- Decision:
  - H-033 결과를 기준으로 `resumeDecision=KEEP_FROZEN` 단일 판정을 유지한다.
  - 다음 라운드(H-034)는 `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 신호에 대해 누적 증거(`evidenceAccumulationSummary[]`)를 유지한 채 신선 증거 복구 상태(`evidenceFreshnessSummary[]`)를 정량 추적하는 계약을 운영 문서와 야간 템플릿에 동일하게 고정한다.
  - H-034의 `evidenceFreshnessSummary[]` 필수 필드는 `signal`, `requiredFreshEvidenceCount`, `freshEvidenceCount`, `freshnessRate`, `staleEvidenceCount`, `freshnessStatus`, `refreshAction`, `nextRefreshDueAt`로 고정한다.
  - `signalRecoveryEvidenceLedger[]`, `evidenceAccumulationSummary[]`, `RESUME_H024|KEEP_FROZEN` 단일 판정 계약은 유지한다.
  - 기존 게이트/산식/임계치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙은 변경하지 않는다.
- Rationale: H-033에서도 `executionGapDelta=+5`, `chainShareGapDelta=0.00%p`, `recoveryActionCompletionRate=0.00`, `blockedActionCount=2`, `LOW_TRAFFIC coverageRate=0.50(freshEvidenceCount=0, staleEvidenceCount=1)`, `CHAIN_COVERAGE_GAP coverageRate=0.00`이 유지되어 재개 판단 근거 강화를 위해 "누적 증거 존재"와 함께 "신선 증거 확보 상태"를 분리 추적해야 하기 때문이다.
- Consequence:
  - H-033은 Main 승인(Go)으로 종료하고, 다음 실행 라운드를 `coordination/HANDOFFS/H-034-fallback-warning-keep-frozen-fresh-evidence-recovery-check.md`로 확정한다.
  - `coordination/REPORTS/CURRENT_STATUS_2026-02-20.md`, `coordination/TASK_BOARD.md`, `coordination/RELAYS/H-034-main-to-executor.md`는 H-034 기준으로 동기화한다.
  - H-024는 `RESUME_H024` 근거(게이트 충족 + 신호 개선 증거 + 신선 증거 충족) 전까지 Frozen/Backlog 상태를 유지한다.

## D-046 fallback-warning H-034 승인 및 H-035 traffic seeding 부트스트랩 정책
- Date: 2026-02-20
- Status: Approved (H-034 Close-out / H-035 Scope)
- Decision:
  - H-034 결과를 기준으로 `resumeDecision=KEEP_FROZEN` 단일 판정을 유지한다.
  - 다음 라운드(H-035)는 fallback-warning 문서 필드 확장보다 실제 run-state 데이터 생성(traffic seeding)과 runId/체인 이벤트 근거 확보를 우선한다.
  - H-035는 direct/chain 워크로드를 재현 가능한 스크립트로 고정하고, `specRunId -> codeRunId -> docRunId/reviewRunId` 매핑 및 `CHAIN_*` 이벤트 근거를 결과 보고에 포함한다.
  - 기존 게이트/산식/임계치(`0.05`, `0.15`, `+0.10p`, `0.10`)와 `INSUFFICIENT_SAMPLE` 제외 규칙, `RESUME_H024|KEEP_FROZEN` 단일 판정 계약은 변경하지 않는다.
- Rationale: H-034에서도 재개 게이트 미충족이 지속되어(HOLD) 재개 판정 신뢰도를 높이려면 문구 보강보다 실행량/체인 커버리지 실제 증거를 먼저 누적해야 하기 때문이다.
- Consequence:
  - H-034는 Main 승인(Go)으로 종료하고, 다음 실행 라운드를 `coordination/HANDOFFS/H-035-fallback-warning-traffic-seeding-workload-bootstrap.md`로 확정한다.
  - H-035는 main relay 누락 상태로 시작되었으나 handoff 기준 실행으로 보완되었고, 라운드 종료 후 상태 문서는 H-035 기준으로 동기화한다.
  - H-024는 `RESUME_H024` 근거 확보 전까지 Frozen/Backlog 상태를 유지한다.

## D-047 fallback-warning H-035 보류(No-Go) 및 H-035.1 fail-fast 종료코드 보강 정책
- Date: 2026-02-20
- Status: Approved (H-035 Close-out / H-035.1 Scope)
- Decision:
  - H-035는 테스트 게이트 통과에도 불구하고 리뷰 P1(fail-fast 모드에서 `runId` 누락 실패가 `exit 0`으로 종료될 수 있는 결함)로 Main `No-Go`로 판정한다.
  - 다음 라운드(H-035.1)는 `scripts/seed-fallback-warning-workload.sh`의 fail-fast 실패 판정/종료코드 경로를 보강해 `runId` 누락 시 반드시 non-zero 종료를 보장한다.
  - 실패 원인이 명령 실패(`exit_code != 0`)인 경로의 기존 종료코드 의미는 유지하고, non fail-fast 모드의 실패 카운트/계속 실행 동작도 유지한다.
  - fallback-warning 운영 계약 필드(`signalRecoveryEvidenceLedger[]`, `evidenceAccumulationSummary[]`, `evidenceFreshnessSummary[]`) 및 게이트/임계치 정책은 변경하지 않는다.
- Rationale: traffic seeding 자동화에서 종료코드는 증거 수집 성공/실패의 1차 신호이므로, `runId` 누락 실패가 성공으로 관찰되면 운영 판정이 왜곡될 위험이 높다.
- Consequence:
  - Main은 다음 실행 라운드를 `coordination/HANDOFFS/H-035-1-fallback-warning-seeding-fail-fast-exitcode-hardening.md`로 확정한다.
  - Main -> Executor 릴레이 `coordination/RELAYS/H-035-1-main-to-executor.md`를 생성해 보완 범위를 고정한다.
  - H-024는 재개 게이트 미충족(`INSUFFICIENT_SAMPLE_RATIO=1.00`, `SUFFICIENT_DAYS=0`)이 지속되는 동안 Frozen/Backlog를 유지한다.
