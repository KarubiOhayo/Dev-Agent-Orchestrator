# H-011 spec/doc/review 프롬프트 자산 보강

Owner: WT-11 (`codex/h011-prompt-assets`)
Priority: Highest

## 목표
- spec/doc/review 에이전트 전용 프롬프트 자산을 추가해 code 중심 편중을 해소한다.
- 프롬프트 지시문을 각 파서 스키마와 정합화해 비정형 출력으로 인한 fallback 발생 가능성을 낮춘다.
- 프롬프트 로딩 동작을 테스트로 고정해 회귀를 방지한다.

## 작업 범위
- 프롬프트 자산 추가/보강
  - `prompts/agents/spec.md` (신규)
  - `prompts/agents/doc.md` (신규)
  - `prompts/agents/review.md` (신규)
  - 필요 시 `prompts/global.md` 문구 최소 보강
- 프롬프트 로딩 회귀 테스트
  - `src/test/java/me/karubidev/devagent/prompt/PromptRegistryTest.java` (신규 또는 보강)
  - 필요 시 `src/main/java/me/karubidev/devagent/prompt/PromptRegistry.java` 최소 보정

## 구현 지침
- 모든 에이전트 프롬프트는 "JSON 단일 객체만 출력" 규칙을 명시한다.
- 스키마 키는 현재 파서 정규화 기준과 정합화한다.
  - spec: `title`, `overview`, `constraints`, `acceptanceCriteria`, `tasks`
  - doc: `title`, `summary`, `sections[{heading,content}]`, `relatedFiles`, `notes`
  - review: `summary`, `overallRisk`, `findings[{title,severity,file,line,description,suggestion}]`, `strengths`, `nextActions`
- 출력 외부에 코드블록/설명 문단을 붙이지 않도록 지시문을 강화한다.
- 기존 code 에이전트 프롬프트(`prompts/agents/code.md`) 계약은 변경하지 않는다.

## 수용 기준
1. `prompts/agents/spec.md`, `prompts/agents/doc.md`, `prompts/agents/review.md`가 추가되고 각 파일에 JSON 단일 객체/스키마 지시가 포함된다.
2. `PromptRegistry`를 통한 프롬프트 조립 시 spec/doc/review 에이전트별 `AGENT_BASE` 내용이 반영됨을 테스트로 검증한다.
3. 기존 code 프롬프트 계약(`files[]` 중심 JSON 객체 출력)은 회귀되지 않는다.
4. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 신규 Agent/엔드포인트 추가
- API 오류 계약(H-010/H-010.1) 재수정
- 모델 라우팅 정책 재설계

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 Main-Control 사전 승인 필수.
- 프롬프트 보강은 출력 계약 안정화 목적이며, 기존 성공/실패 API 스키마를 변경하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-011-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-011-executor-to-review.md` 생성
- 필수 항목:
  - 변경 파일 목록
  - 에이전트별 프롬프트 핵심 규칙 요약(spec/doc/review)
  - 프롬프트 로딩 회귀 테스트 결과
  - 전체 테스트 결과(명령 + 통과/실패)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
