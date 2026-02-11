# DevAgent Task Board

Last Updated: 2026-02-11
Owner: Main Controller Thread

## 현재 스냅샷
- 목표: A(Context Engineering) 완성 후 C(Spec -> Code -> Doc) 체이닝으로 확장
- 현재 상태: CodeAgent 단일 루프는 동작함 (라우팅, LLM 호출, dry-run/apply, run-state)
- 핵심 리스크: LLM 출력을 Markdown 파싱에 의존 중 (JSON 스키마 강제 미완료)

## 완료된 작업
- [x] 모델 라우팅 정책 및 라우터 구현
- [x] OpenAI/Anthropic/Google Provider 어댑터 + fallback 실행
- [x] CodeAgent API 구현 (`/api/agents/code/generate`)
- [x] PromptRegistry(전역/에이전트/프로젝트 오버라이드)
- [x] ContextPolicy(top-k 유사 선별 + 최근 메모리 주입)
- [x] RunState(SQLite 우선 + fallback 로그)
- [x] apply/dry-run 파일 반영
- [x] 기본 테스트(라우팅/LLM fallback/파서/파일적용)

## 병렬 작업 분배

### MAIN-CONTROL (메인 스레드)
- Branch: `main` (또는 `codex/control`) 
- 역할: 설계 승인, 작업 분배, 리뷰, 통합
- TODO:
  - [ ] Worker 결과 리뷰 후 병합 순서 결정
  - [ ] 분기별 위험/의존성 조정
  - [ ] DECISIONS 문서 갱신

### WT-1 SPEC-CHAIN (워크트리 스레드)
- Branch: `codex/spec-chain`
- Handoff: `coordination/HANDOFFS/H-001-spec-agent-chain.md`
- TODO:
  - [ ] SpecAgent 구현
  - [ ] Spec JSON 스키마 고정
  - [ ] Spec -> Code 체이닝 1차 연결

### WT-2 CODE-SCHEMA (워크트리 스레드)
- Branch: `codex/code-json-schema`
- Handoff: `coordination/HANDOFFS/H-002-code-schema-json.md`
- TODO:
  - [ ] Code 출력을 JSON files[] 우선으로 전환
  - [ ] apply 계층을 JSON-only 적용으로 강화
  - [ ] markdown 파싱 fallback 전략 설계

### WT-3 UX-CLI (워크트리 스레드)
- Branch: `codex/cli-ux`
- Handoff: `coordination/HANDOFFS/H-003-cli-ux.md`
- TODO:
  - [ ] CLI 명령 인터페이스(`devagent spec/generate`)
  - [ ] 출력 가독성 개선(pretty summary + apply report)
  - [ ] 운영 문서/예시 업데이트

## 진행 규칙
1. Worker는 자기 Handoff 범위 밖 수정 금지
2. 공통 파일 변경 시 MAIN-CONTROL 승인 후 진행
3. Worker 완료 시 `coordination/REPORTS/`에 보고서 생성 필수
4. 메인 스레드만 병합/최종 리팩토링 수행
