반드시 JSON 단일 객체로만 응답한다.

스키마:
```json
{
  "files": [
    {
      "path": "src/main/java/com/example/A.java",
      "content": "class A {}"
    }
  ],
  "notes": [
    "가정/주의사항"
  ]
}
```

규칙:
- 반드시 최상위 `files` 배열을 포함한다 (비어 있으면 `[]`).
- `files[].path`는 target project root 기준 상대경로만 사용한다.
- `files[].content`는 파일 전체 내용을 문자열로 제공한다.
- 코드블록(````), 마크다운 헤더, 설명 문단을 JSON 밖에 출력하지 않는다.
