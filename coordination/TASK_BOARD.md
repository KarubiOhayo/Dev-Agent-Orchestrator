# DevAgent Task Board

Last Updated: 2026-02-20
Owner: Main Controller Thread
Primary Reference: `docs/PROJECT_OVERVIEW.md`

> 해석 규칙: 문서의 `H-XXX` 표기는 placeholder입니다. 실제 실행 시에는 대상 라운드의 최신 실제 파일명(예: `H-006-*`)으로 치환해 사용합니다.

## 현재 스냅샷
- 목표: A(Context Engineering) 완성 후 C(Spec -> Code -> Doc) 체이닝 확장 안정화
- 현재 상태: Spec -> Code -> Doc/Review 체이닝(1차) 운영 안정화 단계이며, H-009~H-040 라운드는 테스트 게이트 통과를 유지했다(H-035는 중간 `No-Go` 후 H-035.1 보완, H-040은 Main `Conditional Go`). H-040 긴급 복구로 공급자 호환/strict-json 정합/`parsedFiles=0` 경고 신호는 복구됐고, 후속 라운드 H-041에서 parser 안전화 + apply 실증 증빙을 우선 보강한다. H-039는 H-041 완료 후 재개, H-024는 Frozen/Backlog 유지.
- 핵심 리스크: H-040 리뷰 기준으로 (1) `CodeOutputParser`의 `LOOSE_JSON_FALLBACK` 과매칭 가능성(P2), (2) writable 환경 `apply=true` 실파일 반영 증빙 미완료(P3)가 잔여 리스크다.
- 운영 정책: 3스레드 체계(메인 제어 + 리뷰 전담 + 실행 전담), 라운드별 stateless 운영

## 완료된 작업
- [x] 모델 라우팅 정책 및 라우터 구현
- [x] OpenAI/Anthropic/Google Provider 어댑터 + fallback 실행
- [x] CodeAgent API 구현 (`/api/agents/code/generate`)
- [x] PromptRegistry(전역/에이전트/프로젝트 오버라이드)
- [x] ContextPolicy(top-k 유사 선별 + 최근 메모리 주입)
- [x] RunState(SQLite 우선 + fallback 로그)
- [x] apply/dry-run 파일 반영
- [x] 기본 테스트(라우팅/LLM fallback/파서/파일적용)
- [x] H-001 SpecAgent + Spec -> Code 체이닝
- [x] H-002 Code 출력 JSON files[] 스키마 우선화 + fallback warning 이벤트
- [x] H-003 CLI + Output UX + H-003.1/H-003.2 보완
- [x] H-004 DocAgent + Code -> Doc 체이닝(1차)
- [x] H-005 ReviewAgent + Code -> Review 체이닝(1차)
- [x] H-006 CLI 고도화(`--json`, 옵션 별칭, 반복 실행 성능)
- [x] H-007 strict-json 기본값/라우팅 우선순위 재설계
- [x] H-008 파일 적용 경계 입력 방어 강화
- [x] H-008.1 심볼릭 링크 경계 우회 차단 보강
- [x] H-009 체인 실패 전파 정책(API 계약) 확정
- [x] H-010 API 입력검증/오류 응답 계약 표준화
- [x] H-010.1 오류 계약 정합성 보강 (`INVALID_JSON_REQUEST` 매핑 + `MISSING_REQUIRED_ANY_OF` 계약)
- [x] H-011 spec/doc/review 프롬프트 자산 보강
- [x] H-012 spec fallback warning 관측성 정합화
- [x] H-013 fallback warning run-state 집계 기준 문서화
- [x] H-014 fallback warning 집계 기준 문구 정합화(`INSUFFICIENT_SAMPLE` 제외 규칙 동기화)
- [x] H-014.1 Code `parseEligibleRunCount` 모수 정의 정합화(직접 호출 + Spec 체인 호출 포함 기준 고정)
- [x] H-015 fallback warning 임계치/알림 룰 실측 보정 준비(14일 가용성/보류 조건 문서화)
- [x] H-016 fallback warning 임계치/알림 룰 실측 기반 보정 실행(게이트 미충족으로 보정 보류 확정, 수치 유지)
- [x] H-017 fallback warning 보정 재착수용 샘플 확보 계획 수립(정량 목표/Projection/착수·보류 분기 규칙 고정)
- [x] H-018 fallback warning 샘플 확보 계획 운영 적용 점검(실측/Projection 오차/보류 판정 반영, Review `Conditional Go`)
- [x] H-018.1 fallback warning 운영 문서 산식/게이트 정합화(진행률 산식 상한 + 재보정 착수 게이트 4종 기준 동기화, Review `Go`)
- [x] H-019 fallback warning 재보정 착수 가능 시점 재점검(최신 14일 재집계 + `READY/HOLD` 판정 갱신, Review `Go`)
- [x] H-020 fallback warning 샘플 확보 실행률 추적 정합화(최근 7일 목표 대비 실행량/달성률 고정 + `READY/HOLD` 입력 강화, Review `Go`)
- [x] H-021 fallback warning 실행량 증대 검증용 호출 믹스 추적(최근 7일 direct/chain 비중 계약 동기화 + `LOW_TRAFFIC`/`CHAIN_COVERAGE_GAP` 분리 근거 고정, Review `Go`)
- [x] H-022 fallback warning 실행량 회복 액션 플랜 수립/운영 점검(최근 7일 목표-실적 gap + `executionRecoveryPlan`/`executionRecoveryProgress` 계약 동기화, Review `Go`)
- [x] H-023 fallback warning 실행량 회복 액션 이행률 추적/검증(최근 7일 절대 gap + delta + `executionRecoveryTrend`/`recoveryActionStatus` 계약 동기화, Review `Go`)
- [x] H-025 Spec -> Code 체인에서 Code의 Doc/Review 체인 옵션 전파 + CLI 옵션/출력 보강(Review `Go`)
- [x] H-026 Spec/CLI 원샷 체이닝 E2E 계약 테스트 보강(`PARTIAL_SUCCESS` + `chainFailures[]` 소비 검증, Review `Go`)
- [x] H-027 CLI `PARTIAL_SUCCESS` 소비 가드레일 보강(`--fail-on-chain-failures`, 종료코드 `3`, human/json 가시성 보강, Review `Go`)
- [x] H-028 CLI 가드레일 실사용성 점검(`data.guardrailTriggered`, human 경고 `guardrail=enabled|disabled`, 자동화/CI 체크리스트 + 샘플 파이프라인, Review `Go`)
- [x] H-029 fallback-warning H-024 동결 트랙 재개 조건 점검(`KEEP_FROZEN` 판정 + 운영 문서/야간 템플릿 계약 동기화, Review `Go`)
- [x] H-030 fallback-warning `KEEP_FROZEN` 상태 실행량/체인 커버리지 회복 액션 이행 추적(`recoveryActionTracking[]`/`recoveryActionCompletionRate`/`blockedActionCount` 계약 동기화, Review `Go`)
- [x] H-031 fallback-warning `KEEP_FROZEN` 후속 점검 및 `RESUME_H024` 재개 근거 재검증(최신 14일/7일 실측 재집계 + 단일 판정 유지 + 운영 문서/야간 템플릿 동기화, Review `Go`)
- [x] H-032 fallback-warning `KEEP_FROZEN` 신호 개선 실증 데이터 확보 정합화(`signalRecoveryEvidenceLedger[]` 필드 고정 + 단일 판정/게이트 근거 동기화, Review `Go`)
- [x] H-033 fallback-warning `KEEP_FROZEN` 실행 증거 누적 점검 정합화(`evidenceAccumulationSummary[]` 필드/산식 고정 + 단일 판정/게이트 근거 동기화, Review `Go`)
- [x] H-034 fallback-warning `KEEP_FROZEN` 신선 증거 복구 추적 정합화(`evidenceFreshnessSummary[]` 필드/산식 고정 + 단일 판정/게이트 근거 동기화, Review `Go`)
- [x] H-035 fallback-warning 트래픽 시딩 워크로드 부트스트랩 1차 구현(테스트 게이트 통과 + runId/체인 이벤트 실측 증거 확보, 중간 `No-Go` 이력은 H-035.1에서 해소)
- [x] H-035.1 fallback-warning traffic seeding fail-fast 종료코드 신뢰성 보강(`runId` 누락 + `exit_code=0` 시 non-zero 강제, Review `Go`)
- [x] H-036 fallback-warning `KEEP_FROZEN` seeding throughput 추적 점검(반복 시딩 실행량/체인 커버리지 누적 + 최신 14일/7일 게이트 재집계 + `resumeDecision=KEEP_FROZEN` 유지, Review `Go`)
- [x] H-037 fallback-warning `KEEP_FROZEN` seeding follow-up + workspace hygiene 정합화(`.gradle-local` ignore 반영 + 반복 시딩 누적 + 최신 14일/7일 게이트 재집계, Review `Go`)
- [x] H-038 fallback-warning `KEEP_FROZEN` seeding failure pattern 후속 점검(fail-fast 반복 시딩 누적 + 체인 실패 원인 재발 빈도/완화 가이드 정합화 + 최신 14일/7일 게이트 재집계, Review `Go`)
- [x] H-040 code-generate provider compatibility + files JSON hardening(OpenAI codex `temperature` 제거 + Anthropic fallback 모델명 정정 + strict-json 기본값 정합 + `parsedFiles=0` 경고/실패 신호 고정, Main `Conditional Go`)

## 3스레드 운영 분배

### THREAD-A MAIN-CONTROL (읽기 전용)
- Branch: `main` 또는 `codex/control-readonly`
- 역할: 라운드 계획, handoff 확정, 승인/보류 최종 판단, 문서 상태 동기화
- 제약: 코드 수정/커밋 금지, 상세 코드리뷰는 THREAD-R에 위임
- 입력: `PROJECT_OVERVIEW`, `TASK_BOARD`, `DECISIONS`, `REPORTS/H-XXX-result.md`, `REPORTS/H-XXX-review.md`, `RELAYS/H-XXX-review-to-main.md`
- 산출: 다음 라운드 지시문, `RELAYS/H-00N-main-to-executor.md`, 상태 문서 갱신, Go/No-Go 결정

### THREAD-R REVIEW-CONTROL (읽기 전용)
- Branch: `main` 또는 `codex/review-readonly`
- 역할: 코드/테스트/리스크 리뷰 전담(버그/회귀/누락 중심)
- 제약: 코드 수정/커밋 금지
- 입력: `coordination/RELAYS/H-XXX-executor-to-review.md`
- 산출: `coordination/REPORTS/H-XXX-review.md`, `coordination/RELAYS/H-XXX-review-to-main.md`

### THREAD-B EXECUTOR (실제 구현)
- Branch: `codex/execution`
- 역할: handoff 범위 구현, 테스트, 커밋, 결과 보고, 리뷰 피드백 반영
- 제약: handoff 범위 밖 수정 금지
- 산출: `coordination/REPORTS/H-XXX-result.md`, `coordination/RELAYS/H-XXX-executor-to-review.md`

## 운영 자산 정책 (2026-02-13)
- 공통 운영 규칙은 루트 `AGENTS.md`를 기준으로 유지한다.
- 라운드 실행/검토 가이드는 `.agents/skills/` 스킬 문서로 표준화한다.
- 릴레이는 `Main -> Executor -> Review -> Main` 3종을 사용한다.
- Automations는 Plan A(report-only)로 운영하며 자동 파일수정/커밋/PR을 금지한다.

## 진행 규칙
1. 모든 스레드는 라운드 시작 시 문서 입력을 다시 로드하는 stateless 원칙을 따른다.
2. THREAD-A는 상세 코드리뷰를 직접 수행하지 않고 THREAD-R 리뷰 리포트를 승인 판단의 근거로 사용한다.
3. THREAD-A는 다음 라운드 시작 시 handoff 확정 직후 `coordination/RELAYS/H-00N-main-to-executor.md`를 생성한다.
4. THREAD-B는 handoff 범위만 구현하고 테스트를 통과시킨 뒤 결과 리포트를 제출한다.
5. THREAD-B는 결과 리포트 제출 직후 `coordination/RELAYS/H-XXX-executor-to-review.md`를 자동 생성해 THREAD-R 입력으로 전달한다.
6. THREAD-R은 결과 리포트와 실제 변경 코드를 대조해 review 리포트를 제출한다.
7. THREAD-R은 리뷰 완료 직후 `coordination/RELAYS/H-XXX-review-to-main.md`를 자동 생성해 THREAD-A 입력으로 전달한다.
8. 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경은 THREAD-A 사전 승인 후 진행한다.
9. 병합은 THREAD-A 최종 승인 이후에만 수행한다.

## 현재 우선순위
- [~] H-041 진행 예정: code-output parser safety guard + apply verification(`LOOSE_JSON_FALLBACK` 과매칭 차단 + writable `writtenFiles > 0` 실증 확보)
- [ ] H-039 보류: fallback-warning `KEEP_FROZEN` resume readiness follow-up check(H-041 보강 완료 후 재개)

## Frozen/Backlog
- [ ] H-024 동결: fallback warning 실행량 회복 액션 최소 이행률 하한선/증거 규약 고정
  사유: 트래픽/샘플 미충족(`LOW_TRAFFIC`, `CHAIN_COVERAGE_GAP`) 장기화 + H-038 기준 `KEEP_FROZEN` 유지 판정(`INSUFFICIENT_SAMPLE_RATIO=0.9286`, `SUFFICIENT_DAYS=1`, `executionGapDelta=-74`, `chainShareGapDelta=-41.77%p`, 최근 3일 평균 `parseEligibleRunCount=26.3333`)이 여전히 재개 기준에 못 미친다. 체인 실패 원인 분류/완화 가이드는 정합화됐지만, `RESUME_H024` 근거 확보 전까지 동결을 유지한다.
