# DevAgent Task Board

Last Updated: 2026-02-19
Owner: Main Controller Thread
Primary Reference: `docs/PROJECT_OVERVIEW.md`

> 해석 규칙: 문서의 `H-XXX` 표기는 placeholder입니다. 실제 실행 시에는 대상 라운드의 최신 실제 파일명(예: `H-006-*`)으로 치환해 사용합니다.

## 현재 스냅샷
- 목표: A(Context Engineering) 완성 후 C(Spec -> Code -> Doc) 체이닝 확장 안정화
- 현재 상태: Spec -> Code -> Doc/Review 체이닝(1차), H-009(체인 실패 전파 정책/API 계약) Go 확정, H-010.1 오류 계약 정합성 보강 완료(Go), H-011 프롬프트 자산 보강 완료(Go), H-012 spec fallback warning 관측성 정합화 완료(Go), H-013 집계 기준 문서화 완료(Review `Conditional Go`), H-014 handoff/relay 생성 완료(Executor 착수 대기)
- 핵심 리스크: `PARTIAL_SUCCESS` 사용 시 `chainFailures` 누락 확인 위험/`parseEligibleRunCount` 체인 포함 기준과 `INSUFFICIENT_SAMPLE` 제외 규칙이 문서 간 불일치해 경고율 해석이 흔들릴 수 있는 상태
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
- [~] H-014 진행중: fallback warning 집계 기준 문구 정합화 (`parseEligibleRunCount` 체인 포함 명시 + `INSUFFICIENT_SAMPLE` 임계치/알림 제외 규칙 동기화)
