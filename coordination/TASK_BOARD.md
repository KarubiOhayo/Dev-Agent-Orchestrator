# DevAgent Task Board

Last Updated: 2026-02-11
Owner: Main Controller Thread

## 현재 스냅샷
- 목표: A(Context Engineering) 완성 후 C(Spec -> Code -> Doc) 체이닝으로 확장
- 현재 상태: CodeAgent 단일 루프는 동작함 (라우팅, LLM 호출, dry-run/apply, run-state)
- 핵심 리스크: LLM 출력을 Markdown 파싱에 의존 중 (JSON 스키마 강제 미완료)
- 운영 정책: 2스레드 체계(읽기 전용 메인 + 실행 스레드)

## 완료된 작업
- [x] 모델 라우팅 정책 및 라우터 구현
- [x] OpenAI/Anthropic/Google Provider 어댑터 + fallback 실행
- [x] CodeAgent API 구현 (`/api/agents/code/generate`)
- [x] PromptRegistry(전역/에이전트/프로젝트 오버라이드)
- [x] ContextPolicy(top-k 유사 선별 + 최근 메모리 주입)
- [x] RunState(SQLite 우선 + fallback 로그)
- [x] apply/dry-run 파일 반영
- [x] 기본 테스트(라우팅/LLM fallback/파서/파일적용)

## 2스레드 운영 분배

### THREAD-A MAIN-CONTROL (읽기 전용)
- Branch: `main` 또는 `codex/control-readonly`
- 역할: 설계 승인, 작업 분배, 리뷰, 통합 판단
- 제약: 코드 수정/커밋 금지
- TODO:
  - [ ] `H-001`, `H-002`, `H-003` 순서/수용기준 확정
  - [ ] 실행 스레드 산출물 리뷰 및 승인/보류 판단
  - [ ] `coordination/DECISIONS.md` 갱신

### THREAD-B EXECUTOR (실제 구현)
- Branch: `codex/execution`
- 역할: 구현, 테스트, 커밋, 결과 보고
- 제약: handoff 범위 밖 수정 금지
- 현재 우선순위:
  - [ ] H-001 SpecAgent + Spec -> Code 체이닝
  - [ ] H-002 Code 출력 JSON files[] 스키마 우선화
  - [ ] H-003 CLI/출력 가독성 개선

## 진행 규칙
1. THREAD-A는 코드 변경 없이 문서/분배/리뷰만 수행한다.
2. THREAD-B는 handoff 범위만 구현하고 테스트를 통과시킨다.
3. 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경은 THREAD-A 승인 후 진행한다.
4. 작업 완료 시 THREAD-B는 `coordination/REPORTS/H-XXX-result.md`를 반드시 작성한다.
5. 병합은 THREAD-A 승인 이후에만 수행한다.
