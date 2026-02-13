# 초기 설계 대비 현재 상태 점검 (2026-02-13)

## 목적
- 최초 프로젝트 설계안과 현재 구현/문서 상태의 차이를 정리한다.
- 개인 생산성 도구(내부 사용) 기준에서 지금 당장 챙길 것과 나중에 미룰 것을 구분한다.

## 한 줄 결론
- 큰 방향은 잘 맞게 진행됐다. 특히 A(Context) -> C(Pipeline) 순서는 유효했고, 운영 가능한 수준의 구조로 성숙했다.
- 다만 초기 설계의 핵심 자산(`rules/examples`)과 일부 확장 축(툴 추상화, PR 자동화)은 아직 비어 있거나 약하다.

## 초기 설계 대비 강화된 점
1. 멀티모델 라우팅/에스컬레이션 체계가 구체화됐다.
   - `src/main/resources/application.yml`
   - `src/main/java/me/karubidev/devagent/orchestration/routing/ModelRouter.java`
2. 체인 실패 정책이 API 계약으로 명시됐다 (`FAIL_FAST`, `PARTIAL_SUCCESS`, `chainFailures`).
   - `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
   - `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
3. Run-state/이벤트 로깅/CLI JSON 출력까지 포함되어 실사용성이 높아졌다.
   - `src/main/java/me/karubidev/devagent/state/RunStateStore.java`
   - `src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java`
4. 운영 문서(결정/태스크/상태)가 정돈되어 다음 작업 우선순위가 명확하다.
   - `coordination/TASK_BOARD.md`
   - `coordination/DECISIONS.md`
   - `coordination/REPORTS/CURRENT_STATUS_2026-02-13.md`

## 누락 또는 드리프트(초기안 대비)
1. `docs/rules`, `docs/examples`가 아직 저장소에 없다.
   - 현재 `docs/`는 개요/CLI/API 문서 중심이며 규칙/예시 자산이 비어 있다.
2. 프롬프트 자산이 `code` 중심이고 spec/doc/review는 보강 필요 상태다.
   - `prompts/agents/code.md`
   - `docs/PROJECT_OVERVIEW.md` (H-011)
3. 초기안의 RouterAgent(요청 분류)와 현재 Router(모델 선택)는 역할이 다르다.
   - 현재는 `ModelRouter` 중심으로 모델 결정을 수행한다.
4. 초기안의 Tool 계층(`write_files`, `run_tests`, `open_pr`, `update_docs`)이 독립 모듈로 분리되어 있지 않다.
   - 현재는 서비스 내부 로직 중심으로 파일 반영을 수행한다.
5. B단계(PR Webhook 기반 Review/Refactor 자동화)는 아직 본격 착수 전이다.

## 개인용 도구 기준 우선순위 제안
서비스 공개를 당장 하지 않는 전제에서는, 서비스급 보안 요구사항을 1순위로 둘 필요는 없다.
다만 개인용이어도 “내 로컬 자산 손상/오동작”을 막는 최소 안전장치는 선행하는 것이 좋다.

### 지금 당장 (Must)
1. H-010: API 입력검증/에러계약 표준화
2. 경로 경계 검증의 일관화(spec 입력/출력 포함)
3. 기본 안전값 유지(`apply=false` 기본, overwrite 보수적)

### 다음 단계 (Should)
1. `docs/rules`, `docs/examples` 실제 축적 (A단계 핵심 자산)
2. H-011: spec/doc/review 프롬프트 자산 보강
3. Tool 호출 단위 추상화 시작 (`write_files`, `run_tests` 우선)

### 나중 단계 (Could)
1. GitHub Webhook/PR 자동 리뷰 파이프라인(B단계)
2. 공개 서비스 전환을 위한 인증/권한/레이트리밋/감사 로그

## 즉시 실행 체크리스트
- [ ] `docs/rules/` 초안 생성 (코딩/아키텍처/예외 처리 규칙)
- [ ] `docs/examples/` 샘플 3개(Controller/Service/Repository)
- [ ] H-010 범위 확정 및 에러 응답 계약 문서화
- [ ] H-011 범위 확정 및 에이전트별 프롬프트 파일 분리

## 참고 문서
- `docs/PROJECT_OVERVIEW.md`
- `docs/code-agent-api.md`
- `docs/model-routing-policy.md`
- `coordination/TASK_BOARD.md`
- `coordination/DECISIONS.md`
- `coordination/REPORTS/CURRENT_STATUS_2026-02-13.md`
