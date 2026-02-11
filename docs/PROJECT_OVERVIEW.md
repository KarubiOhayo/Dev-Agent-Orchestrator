# PROJECT OVERVIEW

## 1) 프로젝트 목표
- 목표: LLM을 단순 챗봇이 아니라 개발 파이프라인에 내장된 자동화 에이전트로 운영
- 범위:
  - Context Engineering (규칙/예시 주입)
  - Issue -> Spec -> Code -> Doc 체이닝
  - PR Review/Refactor 자동화(후순위)

## 2) 현재 구현 상태 (2026-02-11)
- 완료:
  - 모델 라우팅 엔진 + 라우팅 API
  - 벤더 어댑터(OpenAI/Anthropic/Google) + fallback 실행
  - CodeAgent API (`/api/agents/code/generate`)
  - PromptRegistry(공통/에이전트/프로젝트 계층)
  - ContextPolicy(규칙/예시 선별 + 최근 run 메모리 주입)
  - RunState(SQLite 우선, fallback 로그/메모리)
  - apply/dry-run 파일 반영
- 미완료:
  - SpecAgent/DocAgent/ReviewAgent 체이닝
  - Code 출력 JSON files[] 강제화(현재 markdown 파싱 fallback 중심)
  - CLI UX 개선

## 3) 핵심 아키텍처
- `api/`: 엔드포인트
  - `RoutingController`, `CodeAgentController`
- `orchestration/routing/`: 모델 라우팅 정책/결정
- `llm/`: 벤더별 API 호출 및 fallback 실행
- `context/`: 규칙/예시/메모리 기반 컨텍스트 구성
- `prompt/`: 전역/에이전트/프로젝트 프롬프트 계층 합성
- `state/`: 실행 이력 및 프로젝트 메모리 저장
- `agents/code/`: 코드 생성 및 파일 적용

## 4) 운영 정책 (2스레드)
- Thread A: Main-Control (읽기 전용)
  - 설계/분배/리뷰/통합 승인
  - 코드 수정/커밋 금지
- Thread B: Executor (실행)
  - handoff 범위 구현/테스트/커밋/보고
- 상세 규칙:
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/PROMPTS/main-controller.md`
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
  - `apply=false` 기본 검토(dry-run)
  - `apply=true` 실제 파일 쓰기

## 7) 다음 우선순위
1. SpecAgent 구현 (정형 JSON 스키마 출력)
2. Spec -> Code 체이닝 연결
3. Code 출력 JSON files[] 우선화 및 apply JSON-only 강화
4. CLI(`devagent spec/generate`) + 결과 포맷 개선

## 8) 메인 스레드 시작 체크
1. `docs/PROJECT_OVERVIEW.md` 읽기
2. `coordination/TASK_BOARD.md` 최신 상태 확인
3. `coordination/DECISIONS.md` 충돌 여부 확인
4. 이번 라운드 handoff 1개 확정 후 실행 스레드에 전달
