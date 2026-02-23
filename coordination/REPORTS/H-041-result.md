# H-041 결과 보고서 (code-output parser safety guard + apply verification)

## 상태
- 현재 상태: **완료 (parser safety 가드 + 회귀 테스트 + apply 실증 + 게이트 통과)**
- 실행일(KST): `2026-02-23`
- 입력 기준:
  - handoff: `coordination/HANDOFFS/H-041-code-output-parser-safety-and-apply-verification.md`
  - main relay: `coordination/RELAYS/H-041-main-to-executor.md`
  - 참고: `coordination/REPORTS/H-040-result.md`, `coordination/REPORTS/H-040-review.md`, `coordination/RELAYS/H-040-review-to-main.md`

## 변경 파일 목록
- `src/main/java/me/karubidev/devagent/agents/code/apply/CodeOutputParser.java`
- `src/test/java/me/karubidev/devagent/agents/code/apply/CodeOutputParserTest.java`
- `docs/code-agent-api.md`
- `coordination/REPORTS/H-041-result.md`
- `coordination/RELAYS/H-041-executor-to-review.md`

## 구현 요약

### 1) parser safety 가드(`LOOSE_JSON_FALLBACK`) 재설계
- `CodeOutputParser`의 loose fallback 경로를 전역 `"path"`/`"content"` 선형 스캔 방식에서 아래 방식으로 변경했다.
  - `files[]` 배열 키 후보(`"files": [`)를 먼저 탐지
  - 해당 배열 구간 내부에서만 객체 단위 복구 수행
  - 동일 객체 경계(depth=1) 안의 `"path"` + `"content"` 쌍만 파일로 승격
- 효과:
  - `files[]` 밖 설명문/메타 JSON/로그 문자열에 등장하는 키쌍은 파싱 대상에서 제외
  - 서로 다른 객체에 분리된 `path/content`는 결합되지 않음
  - 잘린 JSON에서도 `files[]` 내부 유효 쌍만 복구

### 2) 회귀 테스트 보강
- 기존 truncated JSON 테스트를 강화해 아래를 함께 검증하도록 수정:
  - `files[]` 밖 메타 `path/content` 토큰 무시
  - `content`가 없는 불완전 객체 무시
- 신규 테스트 3건 추가:
  - `parseFilesDoesNotExtractLoosePairsOutsideFilesArray`
  - `parseFilesDoesNotMatchPathAndContentAcrossDifferentObjects`
  - `parseFilesIgnoresEscapedPathTokensInsideContent`

### 3) 운영 문서 최소 동기화
- `docs/code-agent-api.md`에 parser safety 가드 원칙 추가:
  - `LOOSE_JSON_FALLBACK`는 `files[]` 컨텍스트 내부에서만 복구
  - 동일 객체 경계의 `path/content` 쌍만 허용

## 테스트 케이스 목록 + 핵심 assertion

### 신규/수정 케이스
1. `parseFilesExtractsFromTruncatedJsonByLooseFallback` (수정)
- 핵심 assertion:
  - `files[]` 내부 유효 쌍 2건만 복구 (`pyproject.toml`, `focusbar/__init__.py`)
  - `files[]` 외부 메타 토큰 및 불완전 객체(`path`만 존재)는 무시

2. `parseFilesDoesNotExtractLoosePairsOutsideFilesArray` (신규)
- 핵심 assertion:
  - `metadata.path/content`가 있어도 `files`는 빈 배열
  - parse source는 `EMPTY`

3. `parseFilesDoesNotMatchPathAndContentAcrossDifferentObjects` (신규)
- 핵심 assertion:
  - `{"path":...}` / `{"content":...}`가 서로 다른 객체면 파일 생성 없음
  - parse source는 `EMPTY`

4. `parseFilesIgnoresEscapedPathTokensInsideContent` (신규)
- 핵심 assertion:
  - content 문자열 내부 escaped `\"path\"`/`\"content\"` 토큰은 추가 파일로 오탐되지 않음
  - 실제 파일 1건(`notes.txt`)만 복구

### 기존 복원력 케이스 유지
- direct JSON
- JSON code block
- generic code fence JSON
- embedded JSON payload
- markdown fallback
- nested wrapper(`result.files`)

## writable apply 재현 명령 및 결과 (수용기준 #3/#4)

### 명령
```bash
GRADLE_USER_HOME="$PWD/.gradle-local" ./devagent generate \
  --project-id h041-apply-proof \
  --target-root "$PWD/tmp/h041-apply-target-2" \
  --user-request "$(cat tmp/h041-apply-request.txt)" \
  --mode BALANCED \
  --risk-level MEDIUM \
  --strict-json-required false \
  --apply true \
  --overwrite-existing false \
  --json true
```

### 결과
- `exitCode=0`
- `runId=33943538-f511-42e8-b397-b32e98ac7cf6`
- `parsedFiles=2`
- `writtenFiles=2`
- 생성 파일 목록:
  - `tmp/h041-apply-target-2/src/main/java/demo/HelloTool.java`
  - `tmp/h041-apply-target-2/README.md`
- `fileResults`:
  - `src/main/java/demo/HelloTool.java -> WRITTEN`
  - `README.md -> WRITTEN`

## FocusBar 절대경로 재검증 시도 결과

### 시도 1 (기존 요청 기반)
```bash
GRADLE_USER_HOME="$PWD/.gradle-local" ./devagent generate \
  --project-id focusbar \
  --target-root "/Users/apple/dev_source/focusbar" \
  --user-request "$(cat /Users/apple/dev_source/focusbar/focusbar_request.txt)" \
  --mode BALANCED \
  --risk-level MEDIUM \
  --strict-json-required false \
  --apply true \
  --overwrite-existing false \
  --json true
```
- `exitCode=0`
- `runId=ea89ea54-7ccb-4113-8912-b81a2e2b8a4c`
- `parsedFiles=1`, `writtenFiles=0`
- `fileResults`: `tests/test_timer.py -> SKIPPED (file exists)`

### 시도 2 (신규 파일 강제 생성 요청)
```bash
GRADLE_USER_HOME="$PWD/.gradle-local" ./devagent generate \
  --project-id focusbar \
  --target-root "/Users/apple/dev_source/focusbar" \
  --user-request "Return exactly JSON with one file: path h041_probe_permission_check.txt and content permission probe from H-041." \
  --mode BALANCED \
  --risk-level MEDIUM \
  --strict-json-required false \
  --apply true \
  --overwrite-existing false \
  --json true
```
- `exitCode=0`
- `runId=4e7f9394-a20d-4503-8d15-a5199b1f7a61`
- `parsedFiles=1`, `writtenFiles=0`
- `fileResults`: `h041_probe_permission_check.txt -> ERROR (Operation not permitted)`

### 결론
- FocusBar 절대경로(`/Users/apple/dev_source/focusbar`)는 현 실행 sandbox에서 쓰기 제한이 재현된다.
- handoff 요구의 `writtenFiles > 0` 실증은 **writable 경로(`tmp/h041-apply-target-2`)에서 대체 근거로 충족**했다.

## 테스트 명령/결과
- 파서 회귀 선검증:
  - 명령: `./gradlew test --tests me.karubidev.devagent.agents.code.apply.CodeOutputParserTest --no-daemon`
  - 결과: `BUILD SUCCESSFUL`
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: `BUILD SUCCESSFUL`

## 남은 리스크
- `LOOSE_JSON_FALLBACK`는 safety 가드가 적용됐지만, 모델 출력 자유도가 높아 향후 변형 패턴(예: 비정형 주석+배열 혼합)에 대한 추가 샘플 축적은 필요하다.
- FocusBar 절대경로 실증은 sandbox 제약으로 직접 쓰기 성공을 확인하지 못했고, writable 경로 실증으로 대체했다.
- H-039 fallback-warning 재개는 H-041 리뷰/메인 승인 이후 진행한다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`src/main/resources/application.yml`, 공용 모델/계약 파일, `build.gradle`, `settings.gradle`, `gradle/wrapper/**`) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `CodeOutputParser`의 `LOOSE_JSON_FALLBACK`가 `files[]` 컨텍스트 + 동일 객체 경계 제약을 정확히 강제하는지
2. 신규 회귀 테스트 3건과 수정된 truncated 테스트가 H-040 P2 시나리오를 충분히 커버하는지
3. writable apply 실증(`writtenFiles=2`)과 FocusBar 절대경로 오류 증빙(`Operation not permitted`)의 결론 연결이 타당한지
