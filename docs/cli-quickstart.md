# DevAgent CLI Quickstart (Draft)

## 개요

`devagent` 스크립트로 `generate`/`spec` 명령을 한 줄로 실행할 수 있습니다.
스크립트는 `bootRun`을 거치지 않고 `bootJar` 생성 후 `java -jar`로 실행하므로,
CLI 종료코드(예: 잘못된 옵션은 `2`)가 그대로 전달됩니다.

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
  --project-id demo-auth \
  --target-root . \
  --user-request "로그인 API 스켈레톤을 만들어줘" \
  --mode BALANCED \
  --risk-level MEDIUM
```

또는 PATH 등록 후:

```bash
devagent generate --user-request "로그인 API 스켈레톤을 만들어줘"
```

## 실행 경로와 트레이드오프

- 실행 경로:
  - `./gradlew --quiet bootJar`
  - `java -jar build/libs/<boot-jar>.jar ...`
- 장점:
  - CLI 프로세스 종료코드를 정확히 전달 (`help=0`, `unknown option=2`)
  - `bootRun` 실패 래핑으로 인한 종료코드 왜곡 방지
- 단점:
  - 매 실행마다 `bootJar` 체크가 수행되어, 기존 `bootRun` 대비 초기/반복 실행 비용이 증가할 수 있음

### 2) Code Generate (실제 파일 적용)

```bash
./devagent generate \
  --user-request "JWT 로그인 핸들러를 만들어줘" \
  --apply true \
  --overwrite-existing false
```

### 3) Spec Generate

```bash
./devagent spec \
  --user-request "로그인/토큰 재발급 명세를 JSON으로 작성해줘" \
  --mode QUALITY \
  --risk-level MEDIUM
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
+-------------+----------------------+

file results
- DRY_RUN src/main/java/AuthController.java (planned)
- DRY_RUN src/main/java/AuthService.java (planned)
```

## 주요 옵션

- `--project-id`: 실행 컨텍스트 프로젝트 ID
- `--target-root`: 파일 적용 대상 루트 경로
- `--user-request`: 생성 요청 텍스트
- `--mode`: `COST_SAVER | BALANCED | QUALITY | GEMINI3_CANARY`
- `--risk-level`: `LOW | MEDIUM | HIGH`
- `--apply`: `true`면 실제 파일 쓰기, `false`면 dry-run
- `--spec-input-path`: `generate`에서 스펙 JSON 상대 경로 주입
- `--chain-to-code`: `spec`에서 스펙 생성 후 코드 생성 체이닝
