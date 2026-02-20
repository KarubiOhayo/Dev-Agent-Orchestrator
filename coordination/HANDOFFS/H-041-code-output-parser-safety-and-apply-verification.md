# H-041 code-output parser safety guard + apply verification

Owner: WT-41 (`codex/h041-code-output-parser-safety-and-apply-verification`)
Priority: Highest

## 목표
- H-040 리뷰 P2 이슈(`LOOSE_JSON_FALLBACK` 과매칭 가능성)를 해소해 `path/content` 오탐 파일 반영 리스크를 제거한다.
- H-040 리뷰 P3 이슈(`apply=true` 실파일 반영 증빙 미완료)를 writable 환경 기준으로 닫아 운영 승인 근거를 보강한다.
- `parsedFiles=0` 방어 신호(`CODE_OUTPUT_EMPTY_WARNING` + apply 실패 전환)는 유지하면서 parser 복원력과 안전성의 균형을 재고정한다.

## 작업 범위
- Code parser 안전화/테스트 보강
  - `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java`
  - `src/test/java/me/karubidev/devagent/agents/code/apply/CodeOutputParserTest.java`
- 필요 시 연동 테스트 보강(최소 범위)
  - `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`
- 운영 문서 최소 동기화(필요 시)
  - `docs/code-agent-api.md`
  - `docs/model-routing-policy.md`

## 구현 지침
- parser safety 가드:
  - `LOOSE_JSON_FALLBACK`는 `files[]` 컨텍스트 내부 후보에서만 동작하도록 제한한다.
  - `"path"`와 `"content"` 매칭은 반드시 동일 파일 객체 경계 내에서만 허용한다.
  - 설명 텍스트/메타 JSON/로그 문자열에 등장한 `path/content` 키쌍은 파일로 승격하지 않는다.
  - H-040에서 보강한 wrapper/generic-fence/embedded/truncated JSON 복구 경로는 유지한다.
- 회귀 테스트:
  - 비-`files` JSON에 `path/content`가 있어도 `GeneratedFile`이 생성되지 않는 케이스를 추가한다.
  - `path`/`content`가 서로 다른 객체에 분리된 경우 매칭되지 않음을 검증한다.
  - escaped 문자열/설명문 내 `"path"` 토큰이 오탐되지 않음을 검증한다.
  - 잘린 JSON에서도 `files[]` 컨텍스트 안의 유효 쌍만 복구되는지 검증한다.
- apply 실증 정책:
  - writable target root(예: 저장소 하위 임시 디렉터리)에서 `./devagent generate --apply true --json true`를 실행해 `writtenFiles > 0` 증빙을 확보한다.
  - 결과 보고서에 `exitCode`, `runId`, `parsedFiles`, `writtenFiles`, 생성 파일 목록을 필수로 남긴다.
  - 기존 FocusBar 절대경로(`/Users/apple/dev_source/focusbar`) 재검증은 가능하면 수행하되, 권한 제약 시 오류 증빙과 함께 "writable 경로 실증으로 대체"를 명시한다.

## 수용 기준
1. `CodeOutputParser`에서 loose fallback이 `files[]` 컨텍스트 외부 `path/content`를 파일로 파싱하지 않는다(신규 회귀 테스트 포함).
2. H-040에서 확보한 파싱 복원력(wrapper/generic-fence/embedded/truncated JSON)의 기존 테스트가 유지/통과한다.
3. writable target root 기준 `--apply true` 실행 결과가 `exitCode=0`이고 `writtenFiles > 0`이다.
4. 결과 보고서에 apply 생성 파일 목록(경로별)과 실패 시 원인 분류가 함께 기록된다.
5. `./gradlew clean test --no-daemon`를 통과한다.
6. 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약, 빌드 설정) 변경이 없다.

## 비범위
- fallback-warning(H-024/H-039) 게이트/임계치 정책 변경
- 라우팅 모델 정책의 신규 확장
- 자동 커밋/PR/웹훅 자동화

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`src/main/resources/application.yml`, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경 필요 시 즉시 중단하고 Main 승인 요청만 남긴다.

## 보고서
- 완료 시 `coordination/REPORTS/H-041-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-041-executor-to-review.md` 생성
- 필수 포함:
  - 변경 파일 목록
  - parser safety 설계 변경 요약(`files[]` 컨텍스트/객체 경계 제한 방식)
  - 신규/기존 테스트 케이스 목록 + 핵심 assertion
  - writable apply 재현 명령 및 결과(`exitCode`, `runId`, `parsedFiles`, `writtenFiles`, 생성 파일 목록)
  - FocusBar 절대경로 재검증 시도 결과(성공/권한 제약/대체 근거)
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 리스크 및 차기 액션(H-039 재개 조건 포함)
