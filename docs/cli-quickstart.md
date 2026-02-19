# DevAgent CLI Quickstart (Draft)

## 개요

`devagent` 스크립트로 `generate`/`spec` 명령을 한 줄로 실행할 수 있습니다.
스크립트는 `bootRun` 대신 `bootJar + java -jar` 경로를 사용해
CLI 종료코드(예: 잘못된 옵션은 `2`, 체인 실패 가드레일은 `3`)를 그대로 전달합니다.

- 기본 출력: human-readable 요약 테이블
- `--json`/`-j`: machine parsing 용 JSON 출력(성공/실패 공통 envelope)

## 설치

프로젝트 루트에서 실행:

```bash
chmod +x ./devagent
```

선택 사항(어디서든 `devagent` 실행):

```bash
ln -sf "$(pwd)/devagent" /usr/local/bin/devagent
```

## 실행

### 1) Code Generate (dry-run 기본)

```bash
./devagent generate \
  -p demo-auth \
  -r . \
  -u "로그인 API 스켈레톤을 만들어줘" \
  -m BALANCED \
  -k MEDIUM
```

또는 PATH 등록 후:

```bash
devagent generate --user-request "로그인 API 스켈레톤을 만들어줘"
```

## 실행 경로와 트레이드오프

- 실행 경로:
  - (필요 시) `./gradlew --quiet bootJar`
  - `java -jar build/libs/<boot-jar>.jar ...`
- 장점:
  - CLI 프로세스 종료코드를 정확히 전달 (`help=0`, `unknown option=2`)
  - `bootRun` 실패 래핑으로 인한 종료코드 왜곡 방지
  - 최신 boot jar 재사용으로 반복 실행 시 `bootJar` 호출을 생략 가능
- 참고:
  - 스크립트는 `build.gradle/settings.gradle/gradle.properties/src/gradle/buildSrc` 변경 시에만 재빌드합니다.
  - 항상 재빌드하려면 `DEVAGENT_FORCE_BOOTJAR=true ./devagent ...` 를 사용하세요.

### 2) Code Generate (실제 파일 적용)

```bash
./devagent generate \
  -u "JWT 로그인 핸들러를 만들어줘" \
  -a true \
  --overwrite-existing false
```

### 3) Code -> Doc/Review 체인 (원샷)

```bash
./devagent generate \
  -u "로그인 API 코드를 생성해줘" \
  --chain-to-doc true \
  --doc-user-request "생성된 코드를 기준으로 API 문서를 작성해줘" \
  --chain-to-review true \
  --review-user-request "보안/안정성 중심 리뷰를 작성해줘" \
  --chain-failure-policy PARTIAL_SUCCESS
```

### 4) Spec -> Code -> Doc/Review 체인 (원샷)

```bash
./devagent spec \
  -u "로그인/토큰 재발급 명세를 JSON으로 작성해줘" \
  --chain-to-code true \
  --code-user-request "위 명세를 바탕으로 코드를 구현해줘" \
  --code-chain-to-doc true \
  --code-doc-user-request "생성된 코드 기준 문서를 작성해줘" \
  --code-chain-to-review true \
  --code-review-user-request "보안/안정성 관점 리뷰를 작성해줘" \
  --code-chain-failure-policy PARTIAL_SUCCESS \
  -m QUALITY \
  -k MEDIUM
```

### 5) `PARTIAL_SUCCESS` 소비 가드레일 (자동화/CI)

`--fail-on-chain-failures=true`를 사용하면 `PARTIAL_SUCCESS` + `chainFailures[]` 발생 시
출력은 유지하면서 종료코드를 `3`으로 반환합니다.

```bash
./devagent generate \
  -u "로그인 API 코드를 생성해줘" \
  --chain-to-doc true \
  --chain-to-review true \
  --chain-failure-policy PARTIAL_SUCCESS \
  --fail-on-chain-failures true
```

```bash
./devagent spec \
  -u "로그인/토큰 재발급 명세를 JSON으로 작성해줘" \
  --chain-to-code true \
  --code-chain-to-doc true \
  --code-chain-to-review true \
  --code-chain-failure-policy PARTIAL_SUCCESS \
  --fail-on-chain-failures true
```

### 6) JSON 출력 모드

```bash
./devagent generate \
  -u "로그인 API 스켈레톤을 만들어줘" \
  -j
```

## 출력 예시 (dry-run)

```text
== generate summary ==
+-------------+----------------------+
| field       | value                |
+-------------+----------------------+
| runId       | run-123              |
| model       | openai:gpt-5.2-codex |
| parsedFiles | 2                    |
| applyOutcome| DRY_RUN              |
| writtenFiles| 0                    |
| skippedFiles| 0                    |
| chainedDoc  | true                 |
| chainedReview| true                |
| chainFailures| 1                   |
+-------------+----------------------+

[warning] chainFailures detected: 1 (guardrail=disabled, use --fail-on-chain-failures=true to return exit code 3)

file results
- DRY_RUN src/main/java/AuthController.java (planned)
- DRY_RUN src/main/java/AuthService.java (planned)
```

## JSON 출력 예시

### 성공 (`generate --json`)

```json
{
  "ok": true,
  "command": "generate",
  "runId": "run-123",
  "model": {
    "provider": "openai",
    "name": "gpt-5.2-codex",
    "id": "openai:gpt-5.2-codex"
  },
  "data": {
    "summary": {
      "parsedFiles": 2,
      "applyOutcome": "DRY_RUN",
      "writtenFiles": 0,
      "skippedFiles": 0,
      "chainedDoc": true,
      "chainedReview": true,
      "chainFailures": 1
    },
    "hasChainFailures": true,
    "guardrailTriggered": false,
    "fileResults": [
      {
        "path": "src/main/java/AuthController.java",
        "status": "DRY_RUN",
        "message": "planned"
      }
    ],
    "chainFailures": [
      {
        "agent": "DOC",
        "failedStage": "CHAIN_DOC",
        "errorMessage": "doc failure"
      }
    ]
  },
  "error": null
}
```

주의:

- 기본 모드(기본값): `--fail-on-chain-failures` 미사용 또는 `false`면, `chainFailures[]`가 있어도 종료코드는 `0`입니다.
- 가드레일 모드: `--fail-on-chain-failures=true`면, `chainFailures[]`가 비어 있지 않을 때 종료코드 `3`을 반환합니다.
- API에서 `chainFailurePolicy=PARTIAL_SUCCESS`를 사용한 경우, HTTP/API 호출 자체가 성공이어도 `chainFailures[]`를 반드시 확인해야 합니다.
- JSON 출력에서는 `data.hasChainFailures`와 `data.guardrailTriggered` 보조 필드로 빠른 분기 처리가 가능합니다.

### 자동화/CI 소비 체크리스트

| 모드 | 실행 옵션 | `chainFailures[] > 0` 시 종료코드 | 필수 점검 항목 |
|---|---|---:|---|
| 기본 모드 | `--fail-on-chain-failures` 미사용 또는 `false` | `0` | `data.chainFailures[]` 또는 `data.hasChainFailures`를 직접 확인 |
| 가드레일 모드 | `--fail-on-chain-failures=true` | `3` | 종료코드 + `data.guardrailTriggered=true` + `data.chainFailures[]` 동시 확인 |

권장 shell 파이프라인 예시:

```bash
./devagent generate \
  --user-request "로그인 API 코드를 생성해줘" \
  --chain-to-doc true \
  --chain-to-review true \
  --chain-failure-policy PARTIAL_SUCCESS \
  --fail-on-chain-failures true \
  --json > devagent-result.json
exit_code=$?

if [ "$exit_code" -eq 3 ]; then
  echo "chain failures detected (guardrail triggered)"
  cat devagent-result.json
  exit 1
fi

if [ "$exit_code" -ne 0 ]; then
  echo "cli failed with exit code: $exit_code"
  exit "$exit_code"
fi
```

권장 GitHub Actions 예시:

```yaml
- name: Run devagent
  id: devagent
  run: |
    set +e
    ./devagent generate \
      --user-request "로그인 API 코드를 생성해줘" \
      --chain-to-doc true \
      --chain-to-review true \
      --chain-failure-policy PARTIAL_SUCCESS \
      --fail-on-chain-failures true \
      --json > devagent-result.json
    code=$?
    echo "exit_code=$code" >> "$GITHUB_OUTPUT"
    exit 0

- name: Check devagent exit code
  run: |
    code="${{ steps.devagent.outputs.exit_code }}"
    if [ "$code" = "3" ]; then
      echo "chain failures detected (guardrail triggered)"
      cat devagent-result.json
      exit 1
    fi
    if [ "$code" != "0" ]; then
      echo "cli failed with exit code: $code"
      exit "$code"
    fi
```

안티패턴:

- `continue-on-error: true`로 종료코드 `3`을 무시하고 다음 단계를 계속 진행
- stdout 로그만 확인하고 실제 프로세스 종료코드를 확인하지 않음
- `PARTIAL_SUCCESS` 사용 시 `chainFailures[]`를 확인하지 않음

### 실패 (`--json` + 잘못된 옵션)

```json
{
  "ok": false,
  "command": "generate",
  "runId": null,
  "model": null,
  "data": null,
  "error": {
    "exitCode": 2,
    "message": "지원하지 않는 옵션입니다: --unknown-option"
  }
}
```

## 주요 옵션

- `--project-id`, `--project`, `-p`: 실행 컨텍스트 프로젝트 ID
- `--target-root`, `--root`, `-r`: 파일 적용 대상 루트 경로
- `--user-request`, `--request`, `-u`: 생성 요청 텍스트
- `--mode`, `-m`: `COST_SAVER | BALANCED | QUALITY | GEMINI3_CANARY`
- `--risk-level`, `--risk`, `-k`: `LOW | MEDIUM | HIGH`
- `--json`, `-j`: JSON 출력 모드(기본 `false`)
- `--apply`, `-a`: `true`면 실제 파일 쓰기, `false`면 dry-run
- `--spec-input-path`: `generate`에서 스펙 JSON 상대 경로 주입
- `--chain-to-doc`, `--doc-user-request`: `generate`에서 Code -> Doc 체인 옵션
- `--chain-to-review`, `--review-user-request`: `generate`에서 Code -> Review 체인 옵션
- `--chain-failure-policy`: `generate` 체인 실패 정책 (`FAIL_FAST | PARTIAL_SUCCESS`)
- `--fail-on-chain-failures`: `generate/spec` 공통 가드레일 (`true` + `chainFailures>0`이면 종료코드 `3`)
- `--chain-to-code`, `-c`: `spec`에서 스펙 생성 후 코드 생성 체이닝
- `--code-chain-to-doc`, `--code-doc-user-request`: `spec`의 Code 체인에서 Doc 체인 옵션
- `--code-chain-to-review`, `--code-review-user-request`: `spec`의 Code 체인에서 Review 체인 옵션
- `--code-chain-failure-policy`: `spec`의 Code 체인 실패 정책 (`FAIL_FAST | PARTIAL_SUCCESS`)

## 옵션 파싱 규칙 참고

- `--key=value`와 `--key value`를 모두 지원합니다.
- 분리형 long option(`--key value`)에서 value가 `-`로 시작해도, 해당 토큰은 값으로 소비합니다.
- 다만 실제로 지원되는 옵션 키/값 검증은 기존 규칙(unknown option 차단, enum/boolean 검증)을 그대로 따릅니다.
