# PROJECT OVERVIEW

## 1) 프로젝트 목표
- 목표: LLM을 단순 챗봇이 아니라 개발 파이프라인에 내장된 자동화 에이전트로 운영
- 범위:
  - Context Engineering (규칙/예시 주입)
  - Issue -> Spec -> Code -> Doc 체이닝
  - PR Review/Refactor 자동화(후순위)

## 2) 현재 구현 상태 (2026-02-12)
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
  - apply/dry-run 파일 반영
- 미완료:
  - 체인 실패 시 부분 성공 허용 정책 결정
  - `files[]`/`document`/`review` 의미 검증(path/content/section quality) 고도화
  - CLI 고도화(`--json`, 옵션 별칭, 반복 실행 성능 최적화)

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
- `files[]`/`document`/`review` 구조는 보정되지만 의미 품질 검증은 아직 제한적
- Code -> Doc/Review 체인 실패가 현재 Code 요청 실패로 전파됨(부분 성공 정책 미정)
- CLI는 동작 안정화됐지만 머신 파싱용 출력(`--json`)은 미지원

## 8) 다음 우선순위
1. H-006: CLI 고도화(`--json`, 옵션 별칭, 성능)
2. H-007: 체인 실패 전파 정책(부분 성공 허용 여부) 확정
3. 스키마 의미 검증기(files/document/review) 및 운영 지표 강화
4. PR Review/Refactor 자동화 단계 확장

## 9) 라운드 시작 체크 (Stateless)
1. `docs/PROJECT_OVERVIEW.md` 읽기
2. `coordination/TASK_BOARD.md` 최신 상태 확인
3. `coordination/DECISIONS.md` 충돌 여부 확인
4. 대상 handoff/result/review 문서 1개씩 확정
5. Main -> Executor/Review 지시 분리 전달
