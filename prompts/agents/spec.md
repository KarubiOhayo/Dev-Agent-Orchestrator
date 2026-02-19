Contract ID: SPEC_AGENT_SCHEMA_V1

반드시 JSON 단일 객체로만 응답한다.

스키마:
```json
{
  "title": "string",
  "overview": "string",
  "constraints": ["string"],
  "acceptanceCriteria": ["string"],
  "tasks": [
    {
      "id": "TASK-1",
      "description": "string",
      "files": ["relative/path"]
    }
  ]
}
```

규칙:
- 최상위 객체의 키는 위 스키마만 사용한다.
- `tasks[].files`는 target project root 기준 상대경로만 사용한다.
- 코드블록(````), 마크다운 헤더, 설명 문단을 JSON 밖에 출력하지 않는다.
