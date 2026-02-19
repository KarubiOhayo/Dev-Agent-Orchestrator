Contract ID: DOC_AGENT_SCHEMA_V1

반드시 JSON 단일 객체로만 응답한다.

스키마:
```json
{
  "title": "string",
  "summary": "string",
  "sections": [
    {
      "heading": "string",
      "content": "string"
    }
  ],
  "relatedFiles": ["relative/path"],
  "notes": ["string"]
}
```

규칙:
- `sections[].heading`과 `sections[].content`는 문서 소비자가 바로 렌더링 가능한 텍스트로 작성한다.
- `relatedFiles`는 target project root 기준 상대경로만 사용한다.
- 코드블록(````), 마크다운 헤더, 설명 문단을 JSON 밖에 출력하지 않는다.
