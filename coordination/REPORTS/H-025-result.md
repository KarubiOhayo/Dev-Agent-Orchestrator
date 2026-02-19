# H-025 결과 보고서 (Spec -> Code 체인에서 Code의 Doc/Review 옵션 전파 + CLI 옵션/출력 보강)

## 상태
- 현재 상태: **완료 (구현 + 문서 + 테스트 게이트 통과)**
- 실행일(KST): `2026-02-19`

## 변경 파일
- `src/main/java/me/karubidev/devagent/agents/spec/SpecGenerateRequest.java`
- `src/main/java/me/karubidev/devagent/agents/spec/SpecCodeChainService.java`
- `src/main/java/me/karubidev/devagent/cli/DevAgentCliArguments.java`
- `src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java`
- `src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java`
- `src/test/java/me/karubidev/devagent/agents/spec/SpecCodeChainServiceTest.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliArgumentsTest.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`
- `src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
- `docs/cli-quickstart.md`
- `docs/code-agent-api.md`
- `coordination/REPORTS/H-025-result.md`
- `coordination/RELAYS/H-025-executor-to-review.md`

## 구현 요약
- `SpecGenerateRequest`에 Code 체인 확장 필드를 추가했습니다.
  - `codeChainToDoc`, `codeDocUserRequest`, `codeChainToReview`, `codeReviewUserRequest`, `codeChainFailurePolicy`
- `SpecCodeChainService`에서 Spec -> Code 체인 시 위 필드를 `CodeGenerateRequest`로 전파하도록 반영했습니다.
- CLI 옵션을 확장했습니다.
  - `generate`: `--chain-to-doc`, `--doc-user-request`, `--chain-to-review`, `--review-user-request`, `--chain-failure-policy`
  - `spec`: `--code-chain-to-doc`, `--code-doc-user-request`, `--code-chain-to-review`, `--code-review-user-request`, `--code-chain-failure-policy`
- CLI human/json 출력 summary를 확장했습니다.
  - `chainedDoc`, `chainedReview`, `chainFailures(count)`
- CLI json 출력에 `chainFailures` 배열(구조화)을 추가했습니다.

## Spec API curl 예시 (원샷 체이닝) + 결과 요약
```bash
curl -X POST http://localhost:8080/api/agents/spec/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "로그인/토큰 재발급 명세를 JSON으로 작성해줘",
    "mode": "QUALITY",
    "riskLevel": "MEDIUM",
    "chainToCode": true,
    "codeUserRequest": "위 명세를 기준으로 코드를 생성해줘",
    "codeChainToDoc": true,
    "codeDocUserRequest": "생성된 코드 기준 API 문서를 작성해줘",
    "codeChainToReview": true,
    "codeReviewUserRequest": "보안/안정성 관점 리뷰를 작성해줘",
    "codeChainFailurePolicy": "PARTIAL_SUCCESS",
    "codeApply": false
  }'
```
- 기대 결과 요약:
  - `chainedCodeResult`가 존재
  - `chainedCodeResult.chainedDocResult`/`chainedCodeResult.chainedReviewResult`가 체인 옵션에 따라 채워짐
  - `PARTIAL_SUCCESS`에서 체인 실패 시 `chainedCodeResult.chainFailures[]`에 누적

## CLI 예시 (원샷 체이닝) + 출력 샘플
```bash
./devagent spec \
  -u "로그인/토큰 재발급 명세를 JSON으로 작성해줘" \
  --chain-to-code true \
  --code-user-request "위 명세를 바탕으로 코드를 구현해줘" \
  --code-chain-to-doc true \
  --code-doc-user-request "생성된 코드 기준 문서를 작성해줘" \
  --code-chain-to-review true \
  --code-review-user-request "보안/안정성 관점 리뷰를 작성해줘" \
  --code-chain-failure-policy PARTIAL_SUCCESS
```

출력 샘플(human summary 핵심 필드):
```text
== generate summary ==
| field         | value |
| ...           | ...   |
| chainedDoc    | true  |
| chainedReview | true  |
| chainFailures | 1     |
```

출력 샘플(json summary 핵심 필드):
```json
{
  "data": {
    "summary": {
      "chainedDoc": true,
      "chainedReview": true,
      "chainFailures": 1
    },
    "chainFailures": [
      {
        "agent": "DOC",
        "failedStage": "CHAIN_DOC",
        "errorMessage": "doc failure"
      }
    ]
  }
}
```

## 테스트 결과
- 실행 명령(게이트): `./gradlew clean test --no-daemon`
- 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- CLI JSON 소비자가 `data.summary`만 사용하도록 고정돼 있으면 신규 `data.chainFailures`를 추가로 읽지 못할 수 있습니다(호환성은 유지되지만 관측성 이점이 제한될 수 있음).
- `PARTIAL_SUCCESS` 사용 시 `chainFailures[]` 확인 누락 위험은 완화됐지만, 클라이언트가 여전히 해당 필드를 무시하면 실패 체인을 놓칠 수 있습니다.

## 추가 승인 필요 항목
- 공통 승인 대상 파일(`application.yml`, 공용 모델/계약, 빌드 설정) 변경: **없음**
- Main 사전 승인 요청: **해당 없음**

## 리뷰 집중 포인트
1. `SpecCodeChainService`에서 신규 Spec 필드가 `CodeGenerateRequest`로 정확히 전파되는지
2. CLI 신규 옵션이 `generate/spec` 각각에서 unknown option 없이 파싱되고 요청 객체에 매핑되는지
3. CLI 출력 summary/JSON에 `chainedDoc`, `chainedReview`, `chainFailures` 및 구조화 `chainFailures[]`가 누락 없이 반영되는지
