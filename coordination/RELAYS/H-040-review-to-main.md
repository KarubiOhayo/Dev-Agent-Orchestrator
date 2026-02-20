# [H-040] Review -> Main Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md`
- result: `coordination/REPORTS/H-040-result.md`
- review: `coordination/REPORTS/H-040-review.md`

## 리뷰 결과 요약
- 리스크 수준: `MEDIUM`
- P1 개수: `0`
- P2 개수: `1`
- P3 개수: `1`

## 핵심 Findings
1. [P2] `CodeOutputParser`의 `LOOSE_JSON_FALLBACK`가 `files[]` 구조 검증 없이 전역 `"path" -> "content"` 선형 매칭을 수행해 과매칭(오탐) 위험이 있습니다 (`src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java:65`, `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java:280`, `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java:304`).
2. [P3] handoff 수용기준 #2(`apply=true`에서 `writtenFiles > 0`)가 sandbox 권한 제약으로 미검증 상태입니다 (`coordination/HANDOFFS/H-040-code-generate-provider-compat-and-files-json-hardening.md:81`, `coordination/REPORTS/H-040-result.md:120`, `coordination/REPORTS/H-040-result.md:129`).
3. A/B/C 및 strict-json 기본값/경고 신호 관련 핵심 요구사항은 코드/테스트 근거상 충족했습니다(`coordination/REPORTS/H-040-review.md` 상세 근거 참조).

## 승인 게이트 체크
- 수용기준 충족 여부: **부분 충족**
  - #1/#3/#4/#5/#6/#7 충족
  - #2(`apply=true` 실파일 반영) 미검증
- `./gradlew clean test --no-daemon` 통과 여부: **통과(Executor 보고 인용: `BUILD SUCCESSFUL`)**
- 공통 파일 변경 승인 절차 준수 여부: **준수** (`application.yml` 변경은 H-040 사전 승인 범위 내)

## Main-Control 요청
- 권고 판단: `Conditional Go`
- 다음 라운드 제안 1건:
  - `CodeOutputParser`의 `LOOSE_JSON_FALLBACK`를 `files[]` 컨텍스트 기반으로 안전화하고 오탐 방지 회귀 테스트를 추가한 뒤, writable 환경에서 focusbar `--apply true` 재검증(`writtenFiles > 0` + 생성 파일 목록 증빙)을 완료하는 보강 라운드를 권장합니다.
