# H-006 결과 보고서 (CLI 고도화: `--json`, 옵션 별칭, 반복 실행 성능)

## 상태
- 현재 상태: **완료 (P2 보완 2건 반영 + 테스트 게이트 통과 + 리뷰 재요청 완료)**

## 변경 파일 목록
- `devagent`
- `docs/cli-quickstart.md`
- `src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java`
- `src/main/java/me/karubidev/devagent/cli/DevAgentCliArguments.java`
- `src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java`
- `src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliArgumentsTest.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`

## 구현 요약
- `generate/spec` 명령에 `--json`(`-j`) 모드 추가
  - 성공/실패 모두 JSON envelope 출력
  - 기본 출력은 기존 human-readable 유지
- 대표 옵션 별칭(alias) 추가 및 canonical key 충돌 제어
  - 공통: `--project| -p`, `--root| -r`, `--request| -u`, `-m`, `--risk| -k`, `-j`
  - 명령별: `generate`의 `-a`, `spec`의 `-c`
- `devagent` 반복 실행 성능 개선
  - 최신 boot jar 재사용 시 재빌드 생략
  - `DEVAGENT_FORCE_BOOTJAR=true`로 강제 재빌드 가능

## P2 보완 내역 (H-006-PATCH)
1. alias 중복 입력 동치 비교 보완
   - 동일 canonical key 재입력 시 키 타입별 비교 적용:
   - enum/bool 계열(`mode`, `risk-level`, `json`, `apply` 등): normalize/boolean 동치면 허용
   - 자유 문자열/경로 계열(`project-id`, `target-root`, `user-request` 등): 기존 strict 비교 유지
   - 재현 케이스 통과:
   - `./devagent generate --mode=quality -m QUALITY --json` (충돌 미발생)
2. 분리형 long option 값 소비 규칙 보완
   - `--key value`에서 value가 단일 `-`로 시작해도 정책 허용 키면 값으로 소비
   - `--`로 시작하는 다음 토큰은 기존대로 옵션으로 처리
   - 정책 반영 케이스:
   - `--user-request -hello` -> `-hello`를 값으로 처리
   - `--target-root -tmp` -> `-tmp`를 값으로 처리
   - `--risk-level -1` -> `-1`을 값으로 처리 후 enum 검증 단계에서 유효성 오류 발생(의도된 정책)

## 수용기준 충족 여부
1. `devagent generate ... --json` 파싱 가능한 JSON 출력: **충족**
2. `devagent spec ... --json` 파싱 가능한 JSON 출력: **충족**
3. 대표 옵션 alias 동작 + 기존 옵션 충돌 방지: **충족**
4. 반복 실행 성능 개선 근거 포함: **충족**
5. `./gradlew clean test --no-daemon` 통과: **충족**
6. 리뷰 P2 2건(P2-1/P2-2) 보완 및 회귀 테스트 추가: **충족**

## 테스트 결과 (명령/성공 여부)
- `./gradlew test --tests "me.karubidev.devagent.cli.*" --no-daemon` -> **BUILD SUCCESSFUL**
- `./gradlew clean test --no-daemon` -> **BUILD SUCCESSFUL**

## 추가/보강 테스트
- `DevAgentCliArgumentsTest.parseAllowsEquivalentEnumAliasValues`
- `DevAgentCliArgumentsTest.parseAllowsEquivalentBooleanAliasValues`
- `DevAgentCliArgumentsTest.parseConsumesDashPrefixedSeparatedValueForUserRequest`
- `DevAgentCliArgumentsTest.parseConsumesDashPrefixedSeparatedValueForTargetRoot`
- `DevAgentCliArgumentsTest.parseConsumesRiskLevelDashValueThenFailsEnumValidation`

## JSON 출력 예시
### 성공 예시 (`generate --json`)
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
      "parsedFiles": 1,
      "applyOutcome": "DRY_RUN",
      "writtenFiles": 0,
      "skippedFiles": 0
    },
    "fileResults": [
      {
        "path": "src/main/java/AuthController.java",
        "status": "DRY_RUN",
        "message": "planned"
      }
    ]
  },
  "error": null
}
```

### 실패 예시 (실행 검증)
명령: `./devagent generate --unknown-option=true --json`
```json
{"ok":false,"command":"generate","runId":null,"model":null,"data":null,"error":{"exitCode":2,"message":"지원하지 않는 옵션입니다: --unknown-option"}}
```

## 성능 개선 근거 (비교 기준 포함)
- 동일 명령 `./devagent help` 3회 반복 측정
- 기준: `DEVAGENT_FORCE_BOOTJAR=true`로 매 실행 `bootJar` 강제
- 개선안: 최신 jar 재사용(재빌드 생략)
- 결과(real, 초):
  - 강제 재빌드: `2.41`, `1.92`, `1.92` (평균 `2.08`)
  - 캐시 재사용: `1.42`, `1.41`, `1.44` (평균 `1.42`)
- 평균 `0.66s` 단축(약 `31.7%` 개선)

## 남은 리스크
- `--json` 성공 예시는 테스트 fixture 기반이며, 실제 운영 성공 출력은 외부 LLM/API 상태 영향이 있음.
- `devagent` 스크립트 신선도 판단은 mtime 기반이라 특수한 의존 변화 누락 가능성이 남아 있음.
- alias는 대표 옵션 위주 도입 상태라, 사용 패턴에 따라 확장 필요 가능성이 있음.

## 공통 파일 변경 승인 필요 여부/적용 여부
- `application.yml` 변경: **없음**
- 공용 모델 변경: **없음**
- 빌드 설정 변경: **없음**
- 사전 승인 필요 항목 적용 여부: **해당 없음**
