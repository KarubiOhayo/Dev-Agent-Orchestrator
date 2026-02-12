# H-005 결과 보고서 (ReviewAgent + Code -> Review Chain)

## 상태
- 현재 상태: **완료 (구현 + 테스트 + 문서 갱신 완료)**

## 변경 파일 목록
- `src/main/java/me/karubidev/devagent/agents/review/ReviewGenerateRequest.java`
- `src/main/java/me/karubidev/devagent/agents/review/ReviewGenerateResponse.java`
- `src/main/java/me/karubidev/devagent/agents/review/ReviewOutputSchemaParser.java`
- `src/main/java/me/karubidev/devagent/agents/review/ReviewAgentService.java`
- `src/main/java/me/karubidev/devagent/agents/review/CodeReviewChainService.java`
- `src/main/java/me/karubidev/devagent/api/ReviewAgentController.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeGenerateResponse.java`
- `src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
- `src/test/java/me/karubidev/devagent/agents/review/ReviewAgentServiceTest.java`
- `src/test/java/me/karubidev/devagent/agents/review/CodeReviewChainServiceTest.java`
- `src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`
- `src/test/java/me/karubidev/devagent/agents/doc/CodeDocChainServiceTest.java`
- `src/test/java/me/karubidev/devagent/agents/spec/SpecCodeChainServiceTest.java`
- `src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
- `docs/code-agent-api.md`
- `coordination/REPORTS/H-005-result.md`

## 테스트 결과 (명령 + 통과/실패)
- 명령: `./gradlew test --no-daemon`
- 결과: **통과 (BUILD SUCCESSFUL)**
- 명령: `./gradlew clean test --no-daemon`
- 결과: **통과 (BUILD SUCCESSFUL)**

## 수용기준 충족 여부
1. `POST /api/agents/review/generate` 응답: **충족**
  - `ReviewAgentController` + `ReviewAgentService` 구현, 구조화된 `review` JSON 반환
2. `chainToReview=true` Code -> Review 체인: **충족**
  - `CodeGenerateRequest.chainToReview/reviewUserRequest`
  - `CodeGenerateResponse.chainedReviewResult`
  - `CodeAgentService`에서 `CodeReviewChainService` 연쇄 실행
3. run-state 체인 이벤트(`CHAIN_REVIEW_*`) 기록: **충족**
  - `CHAIN_REVIEW_TRIGGERED`, `CHAIN_REVIEW_DONE`, `CHAIN_REVIEW_FAILED`
  - fallback 발생 시 `REVIEW_OUTPUT_FALLBACK_WARNING` 기록
4. `./gradlew clean test` 통과: **충족**

## API 요청/응답 예시
- Code -> Review 체인 요청:
```json
{
  "projectId": "demo-auth",
  "targetProjectRoot": ".",
  "userRequest": "로그인 API 스켈레톤 생성",
  "mode": "BALANCED",
  "riskLevel": "MEDIUM",
  "strictJsonRequired": true,
  "apply": false,
  "chainToReview": true,
  "reviewUserRequest": "보안/안정성 중심으로 리뷰"
}
```
- Code 응답 내 체인 결과 예시:
```json
{
  "runId": "code-run-123",
  "chainedReviewResult": {
    "runId": "review-run-456",
    "sourceCodeRunId": "code-run-123",
    "review": {
      "summary": "입력 검증 누락 가능성",
      "overallRisk": "MEDIUM",
      "findings": [
        {
          "title": "Null 체크 누락",
          "severity": "HIGH",
          "file": "src/main/java/me/example/AuthController.java",
          "line": 42,
          "description": "request body null 처리 없음",
          "suggestion": "validation 추가"
        }
      ],
      "strengths": ["구조 분리 양호"],
      "nextActions": ["입력 검증 보강", "예외 처리 테스트 추가"]
    }
  }
}
```
- Review 단독 호출 요청:
```json
{
  "projectId": "demo-auth",
  "targetProjectRoot": ".",
  "userRequest": "생성 코드 리뷰",
  "mode": "BALANCED",
  "riskLevel": "MEDIUM",
  "strictJsonRequired": true,
  "codeRunId": "code-run-123",
  "codeOutput": "{\"files\":[...]}",
  "codeFiles": [
    {"path": "src/main/java/me/example/AuthController.java", "content": "..."}
  ]
}
```

## 남은 리스크
- 체인 실패 전파는 현재 기존 정책과 동일하게 전체 Code 요청 실패로 처리됨(부분 성공 정책은 H-007에서 결정 필요).
- `ReviewOutputSchemaParser` fallback은 구조 보정 중심이라 의미 품질(정확한 파일/라인 매핑)은 모델 출력 품질에 영향받음.
- Review 전용 프롬프트 파일(`prompts/agents/review.md`)은 미추가 상태이며 기본 프롬프트+요청 템플릿 기반으로 동작.

## 메인 병합 시 주의사항
- 기존 정책(D-012)과 동일하게 체인 실패 시 Code 요청 전체 실패로 전파되므로, 운영 적용 시 `chainToReview=true` 기본값은 유지하지 않는 것을 권장.
- Review JSON은 fallback 시 구조는 유지되지만 내용 품질 보장은 제한적이므로, 운영 모니터링에서 `REVIEW_OUTPUT_FALLBACK_WARNING` 이벤트 비율을 함께 관찰 필요.

## 공통 파일 변경 승인 필요 여부/적용 여부
- `application.yml` 변경: **없음**
- 공용 모델 변경: **없음**
- 빌드 설정 변경: **없음**
- 사전 승인 필요 항목 적용 여부: **해당 없음**

## 리뷰 지적사항 대응 내역
- 현재 라운드 기준 리뷰 스레드 지적사항 수신 전 상태. 신규 지적사항 수신 시 본 문서에 후속 반영 예정.
