# H-004 결과 보고서 (DocAgent + Code -> Doc Chain)

## 변경 파일 목록
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/api/DocAgentController.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/doc/DocAgentService.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/doc/DocGenerateRequest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/doc/DocGenerateResponse.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/doc/DocOutputSchemaParser.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/doc/CodeDocChainService.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/code/CodeGenerateRequest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/agents/code/CodeGenerateResponse.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/agents/doc/DocAgentServiceTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/agents/doc/CodeDocChainServiceTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/agents/spec/SpecCodeChainServiceTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/docs/code-agent-api.md`

## 테스트 결과 (명령 + 통과/실패)
- 명령: `./gradlew clean test`
- 결과: **통과 (BUILD SUCCESSFUL)**

## API 요청/응답 예시

### 1) DocAgent 단독 호출

요청
```bash
curl -X POST http://localhost:8080/api/agents/doc/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "로그인 코드 기준 운영 문서를 생성해줘",
    "mode": "BALANCED",
    "riskLevel": "MEDIUM",
    "strictJsonRequired": true,
    "codeRunId": "code-run-123",
    "codeOutput": "{\"files\":[...]}",
    "codeFiles": [
      {"path":"src/main/java/me/example/AuthController.java","content":"..."}
    ]
  }'
```

응답(예시)
```json
{
  "runId": "doc-run-1",
  "projectId": "demo-auth",
  "targetProjectRoot": "/Users/apple/dev_source/Dev-Agent Orchestrator",
  "routeDecision": {"agentType":"DOC","mode":"BALANCED"},
  "usedProvider": "openai",
  "usedModel": "gpt-5.2",
  "document": {
    "title": "Login API Docs",
    "summary": "인증 흐름 문서",
    "sections": [{"heading": "Endpoints", "content": "POST /login"}],
    "relatedFiles": ["src/main/java/me/example/AuthController.java"],
    "notes": []
  },
  "attempts": [],
  "referencedContextFiles": ["docs/rules/doc-style.md"],
  "projectSummary": "...",
  "sourceCodeRunId": "code-run-123"
}
```

### 2) Code -> Doc 체이닝 호출

요청
```bash
curl -X POST http://localhost:8080/api/agents/code/generate \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "demo-auth",
    "targetProjectRoot": ".",
    "userRequest": "로그인 API 스켈레톤을 만들어줘",
    "mode": "BALANCED",
    "riskLevel": "MEDIUM",
    "strictJsonRequired": true,
    "apply": false,
    "overwriteExisting": false,
    "chainToDoc": true,
    "docUserRequest": "생성 코드 기준 API 문서를 작성해줘"
  }'
```

응답(예시)
```json
{
  "runId": "code-run-1",
  "files": [{"path":"src/main/java/me/example/AuthController.java","content":"..."}],
  "applyResult": {"dryRun": true, "parsedFiles": 1, "writtenFiles": 0, "skippedFiles": 0, "files": []},
  "chainedDocResult": {
    "runId": "doc-run-2",
    "document": {
      "title": "Auth Module Docs",
      "summary": "...",
      "sections": [],
      "relatedFiles": ["src/main/java/me/example/AuthController.java"],
      "notes": []
    },
    "sourceCodeRunId": "code-run-1"
  }
}
```

## 남은 리스크
- `DocOutputSchemaParser`의 fallback은 스키마 일관성을 보장하지만, 모델이 비정형 텍스트를 반환하면 문서 품질이 저하될 수 있음(`DOC_OUTPUT_FALLBACK_WARNING`로 탐지).
- Code -> Doc 체이닝 실패 시 전체 Code 요청이 실패로 전파되므로, 운영 환경에서 체인 실패 허용 정책(부분 성공)을 별도로 결정할 필요가 있음.
- 현재 CLI는 DocAgent 직접 호출/체인 옵션 노출을 제공하지 않음(API 중심 경로만 제공).

## 병합 시 주의사항
- 현재 워크트리에 H-004 외 선행/병행 변경 파일이 존재하므로, 병합 시 본 보고서의 변경 파일 목록만 선별 체리픽 필요.
- `CodeGenerateResponse` 스키마가 확장(`chainedDocResult`)되었으므로, 이 응답을 엄격 역직렬화하는 다운스트림 소비자가 있다면 병합 전 호환성 확인 필요.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정)은 변경하지 않았음.
