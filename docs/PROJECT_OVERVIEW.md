# PROJECT OVERVIEW

## 1) 프로젝트 목표
- 목표: LLM을 단순 챗봇이 아니라 개발 파이프라인에 내장된 자동화 에이전트로 운영
- 범위:
  - Context Engineering (규칙/예시 주입)
  - Issue -> Spec -> Code -> Doc 체이닝
  - PR Review/Refactor 자동화(후순위)

## 2) 현재 구현 상태 (2026-02-20)
- 완료:
  - 모델 라우팅 엔진 + 라우팅 API
  - 벤더 어댑터(OpenAI/Anthropic/Google) + fallback 실행
  - CodeAgent API (`/api/agents/code/generate`)
  - SpecAgent API (`/api/agents/spec/generate`)
  - DocAgent API (`/api/agents/doc/generate`)
  - ReviewAgent API (`/api/agents/review/generate`)
  - Spec -> Code 체이닝
  - Code -> Doc 체이닝(1차)
  - Code -> Review 체이닝(1차)
  - PromptRegistry(공통/에이전트/프로젝트 계층)
  - ContextPolicy(규칙/예시 선별 + 최근 run 메모리 주입)
  - RunState(SQLite 우선, fallback 로그/메모리)
  - Code 출력 JSON `files[]` 우선 파싱 + markdown fallback warning 이벤트
  - spec input/output 경로 안전성 강화(target root 내부 상대경로 강제)
  - CLI 초안(`devagent generate/spec/help`) + 종료코드 보완(H-003.2)
  - CLI 고도화(H-006): `--json`, 대표 alias, 반복 실행 성능 개선 (최종 정합성 확인 완료)
  - H-007 strict-json 기본값/라우팅 우선순위 재설계 완료(Go)
  - H-008 파일 적용 경계 입력 방어 강화 완료
  - H-008.1 심볼릭 링크 경계 우회 차단 보강 완료(Go)
  - H-009 체인 실패 전파 정책(API 계약) 확정 완료(Go)
  - H-010 API 입력검증/오류 응답 계약 표준화 완료(Go)
  - H-010.1 오류 계약 정합성 보강 완료(`INVALID_JSON_REQUEST` 매핑 동기화 + `MISSING_REQUIRED_ANY_OF` 계약 고정)
  - H-011 spec/doc/review 프롬프트 자산 보강 완료(Go)
  - H-012 spec fallback warning 관측성 정합화 완료(Go)
  - H-013 fallback warning run-state 집계 기준 문서화 완료(모수/경고율/임계치/알림 룰 + 야간 점검 템플릿 정합화)
  - H-014 fallback warning 집계 기준 문구 정합화 완료(`INSUFFICIENT_SAMPLE` 제외 규칙 동기화, H-014.1 보강 반영)
  - H-014.1 Code `parseEligibleRunCount` 모수 정의 정합화 완료(직접 호출 + Spec 체인 호출 포함 기준 고정, Go)
  - H-015 fallback warning 임계치/알림 룰 실측 보정 준비 완료(14일 가용성/집계 불가/보정 보류 조건 문서화, Go)
  - H-016 fallback warning 임계치/알림 룰 실측 기반 보정 실행 완료(게이트 미충족으로 보정 보류 확정, 임계치/알림 룰 수치 유지)
  - H-017 fallback warning 보정 재착수용 샘플 확보 계획 수립 완료(기준선/정량 목표/Projection/착수·보류 분기 규칙 문서화, Go)
  - H-018 fallback warning 샘플 확보 계획 운영 적용 점검 수행 완료(실측/Projection 오차/보류 판정 보고, Review `Conditional Go`)
  - H-018.1 fallback warning 운영 문서 산식/게이트 정합화 완료(진행률 산식 상한 + 재보정 착수 게이트 4종 기준 동기화, Review `Go`)
  - H-019 fallback warning 재보정 착수 가능 시점 재점검 완료(최신 14일 재집계 + `READY/HOLD` 판정 갱신, Review `Go`)
  - H-020 fallback warning 샘플 확보 실행률 추적 정합화 완료(최근 7일 실행률/달성률 계약 동기화 + `READY/HOLD` 입력 강화, Review `Go`)
  - H-021 fallback warning 실행량 증대 검증용 호출 믹스 추적 완료(최근 7일 직접/체인 호출 믹스 계약 동기화 + `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 분리 근거 고정, Review `Go`)
  - H-022 fallback warning 실행량 회복 액션 플랜 수립/운영 점검 완료(최근 7일 목표-실적 gap + 실행량 회복 액션 계약 동기화, Review `Go`)
  - H-023 fallback warning 실행량 회복 액션 이행률 추적/검증 완료(최근 7일 절대 gap + delta + `executionRecoveryTrend`/`recoveryActionStatus` 계약 동기화, Review `Go`)
  - H-025 Spec -> Code 체인에서 Code의 Doc/Review 체인 옵션 전파 + CLI 옵션/출력 보강 완료(Review `Go`)
  - H-026 Spec/CLI 원샷 체이닝 E2E 계약 테스트 보강 완료(`PARTIAL_SUCCESS` + `chainFailures[]` 소비 검증, Review `Go`)
  - H-027 CLI `PARTIAL_SUCCESS` 소비 가드레일 보강 완료(`--fail-on-chain-failures`, 종료코드 `3`, human/json 가시성 보강, Review `Go`)
  - H-028 CLI 가드레일 실사용성 점검 완료(`data.guardrailTriggered`, human 경고 `guardrail=enabled|disabled`, 자동화/CI 체크리스트 + 샘플 파이프라인 검증, Review `Go`)
  - H-029 fallback-warning H-024 동결 트랙 재개 조건 점검 완료(`KEEP_FROZEN` 판정 고정 + 운영 문서/야간 템플릿 계약 동기화, Review `Go`)
  - H-030 fallback-warning `KEEP_FROZEN` 상태 실행량/체인 커버리지 회복 액션 이행 추적 완료(`recoveryActionTracking[]` + `recoveryActionCompletionRate` + `blockedActionCount` 계약 동기화, `KEEP_FROZEN` 유지)
  - H-031 fallback-warning `KEEP_FROZEN` 후속 점검 및 `RESUME_H024` 재개 근거 재검증 완료(최신 14일/7일 실측 재집계 + 단일 판정 유지 + 운영 문서/야간 템플릿 동기화)
  - H-032 fallback-warning `KEEP_FROZEN` 신호 개선 실증 데이터 확보 계약 정합화 완료(`signalRecoveryEvidenceLedger[]` 필드 고정 + 단일 판정/게이트 근거 동기화, `KEEP_FROZEN` 유지)
  - H-033 fallback-warning `KEEP_FROZEN` 실행 증거 누적 점검 정합화 완료(`evidenceAccumulationSummary[]` 필드/산식 추가 + 단일 판정/게이트 근거 동기화, `KEEP_FROZEN` 유지)
  - H-034 fallback-warning `KEEP_FROZEN` 신선 증거 복구 추적 정합화 완료(`evidenceFreshnessSummary[]` 필드/산식 추가 + 단일 판정/게이트 근거 동기화, `KEEP_FROZEN` 유지)
  - H-035 fallback-warning 트래픽 시딩 워크로드 부트스트랩 1차 구현 완료(시딩 스크립트/운영 문서 동기화 + runId/체인 이벤트 실측 증거 확보)
  - H-035.1 traffic seeding fail-fast 종료코드 신뢰성 보강 완료(`runId` 누락 + `exit_code=0` 케이스 non-zero 강제, Review `Go`)
  - apply/dry-run 파일 반영
- 미완료:
  - H-036 fallback-warning `KEEP_FROZEN` seeding throughput 추적 점검(반복 시딩 실행량/체인 커버리지 누적 + 최신 게이트 재집계)
  - H-024 fallback warning 실행량 회복 액션 최소 이행률 하한선/증거 규약 고정(Frozen/Backlog, `RESUME_H024` 판정 근거 확보 시 재개)
  - fallback warning 임계치/알림 룰 보정안의 운영 적용 후 회귀 점검(지속 데이터 누적 필요)

## 3) 핵심 아키텍처
- `api/`: 엔드포인트
  - `RoutingController`, `CodeAgentController`, `SpecAgentController`, `DocAgentController`, `ReviewAgentController`
- `orchestration/routing/`: 모델 라우팅 정책/결정
- `llm/`: 벤더별 API 호출 및 fallback 실행
- `context/`: 규칙/예시/메모리 기반 컨텍스트 구성
- `prompt/`: 전역/에이전트/프로젝트 프롬프트 계층 합성
- `state/`: 실행 이력 및 프로젝트 메모리 저장
- `agents/code/`: 코드 생성 및 파일 적용
- `agents/spec/`: 명세 생성 + Spec -> Code 체인
- `agents/doc/`: 문서 생성 + Code -> Doc 체인
- `agents/review/`: 리뷰 생성 + Code -> Review 체인
- `cli/`: `devagent` 명령 파서/포매터/실행기

## 4) 운영 정책 (3스레드 + Stateless 라운드)
- Thread A: Main-Control (읽기 전용)
  - 설계/분배/승인(Go/No-Go)/문서 동기화
  - 코드 수정/커밋 금지
- Thread R: Review-Control (읽기 전용)
  - 리뷰 전담(버그/리스크/테스트 누락)
  - 코드 수정/커밋 금지
- Thread B: Executor (실행 전용)
  - handoff 범위 구현/테스트/커밋/보고
- Stateless 원칙:
  - 각 라운드 시작 시 핵심 문서 재로딩
  - 세션 기억 대신 `coordination` 문서를 단일 소스 오브 트루스로 사용
- 상세 규칙:
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/PROMPTS/main-controller.md`
  - `coordination/PROMPTS/review-controller.md`
  - `coordination/PROMPTS/worker-executor.md`

## 5) 주요 설정 포인트
- 모델 라우팅: `src/main/resources/application.yml`의 `devagent.model-routing`
- LLM 설정: `src/main/resources/application.yml`의 `devagent.llm`
- 컨텍스트/프롬프트/상태저장 설정:
  - `devagent.context`
  - `devagent.prompt`
  - `devagent.run-state`

## 6) API 요약
- 라우팅 확인:
  - `POST /api/routing/resolve`
- 코드 생성:
  - `POST /api/agents/code/generate`
  - `apply=false` 기본 검토(dry-run), `apply=true` 실제 파일 쓰기
  - `chainToDoc=true` 시 DocAgent 연쇄 실행
  - `chainToReview=true` 시 ReviewAgent 연쇄 실행
- 명세 생성:
  - `POST /api/agents/spec/generate`
  - `chainToCode=true` 시 CodeAgent 연쇄 실행
- 문서 생성:
  - `POST /api/agents/doc/generate`
  - Code run 결과(`codeRunId`, `codeOutput`, `codeFiles`) 기반 문서 JSON 생성
- 리뷰 생성:
  - `POST /api/agents/review/generate`
  - Code run 결과(`codeRunId`, `codeOutput`, `codeFiles`) 기반 리뷰 JSON 생성

## 7) 현재 운영 리스크
- 모델 출력 비정형 시 fallback 비율이 상승할 수 있음
- fallback warning 임계치는 초기 기준값이므로 트래픽/모델 분포 변화 시 오탐/미탐 가능성이 있음
- `files[]`/`document`/`review` 구조는 보정되지만 의미 품질 검증은 아직 제한적
- `PARTIAL_SUCCESS` 사용 시 클라이언트가 `chainFailures`를 확인하지 않으면 체인 실패를 간과할 수 있음(가드레일은 opt-in)
- CLI JSON 출력은 지원되지만, 옵션 파싱 경계 케이스는 지속 회귀 점검 필요

## 8) 다음 우선순위
1. H-036 fallback-warning `KEEP_FROZEN` seeding throughput 추적 점검(반복 실행으로 `parseEligibleRunCount` 누적 + `RESUME_H024|KEEP_FROZEN` 재판정)
2. H-024 fallback warning 실행량 회복 액션 최소 이행률 하한선/증거 규약 고정(Frozen/Backlog, `RESUME_H024` 판정 근거 확보 시 재개)
3. fallback warning 임계치/알림 룰 보정안의 운영 적용 후 회귀 점검(지속 데이터 누적 필요)

## 9) 라운드 시작 체크 (Stateless)
1. `docs/PROJECT_OVERVIEW.md` 읽기
2. `coordination/TASK_BOARD.md` 최신 상태 확인
3. `coordination/DECISIONS.md` 충돌 여부 확인
4. 대상 handoff/result/review 문서 1개씩 확정
5. Main -> Executor/Review 지시 분리 전달

## 10) 참고 평가 문서
- 초기 설계 대비 현재 상태 점검(2026-02-13):
  - `docs/assessments/2026-02-13-initial-plan-gap-analysis.md`
