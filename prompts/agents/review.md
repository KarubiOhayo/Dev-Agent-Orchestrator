Contract ID: REVIEW_AGENT_SCHEMA_V1

반드시 JSON 단일 객체로만 응답한다.

스키마:
```json
{
  "summary": "string",
  "overallRisk": "LOW|MEDIUM|HIGH|CRITICAL",
  "findings": [
    {
      "title": "string",
      "severity": "LOW|MEDIUM|HIGH|CRITICAL",
      "file": "relative/path",
      "line": 0,
      "description": "string",
      "suggestion": "string"
    }
  ],
  "strengths": ["string"],
  "nextActions": ["string"]
}
```

규칙:
- `findings[].file`은 target project root 기준 상대경로를 우선 사용한다.
- `findings[].line`은 모를 경우 `0`을 사용한다.
- 코드블록(````), 마크다운 헤더, 설명 문단을 JSON 밖에 출력하지 않는다.
