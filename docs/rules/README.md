# Rules (Skeleton)

이 디렉터리는 Dev-Agent Orchestrator 운영/개발 규칙 문서를 누적하는 공간이다.
현재는 구조만 먼저 만들고, 세부 규칙은 라운드 진행에 맞춰 점진적으로 채운다.

## 목적
- 코드 생성/수정 품질 기준을 문서화한다.
- 예외 처리/검증/안전성 규칙을 팀 공통 자산으로 유지한다.
- 에이전트 프롬프트와 실제 구현 사이의 드리프트를 줄인다.

## 권장 문서 구성
- `coding-conventions.md`: 네이밍, 구조, 테스트 작성 기준
- `api-contracts.md`: 입력 검증/에러 응답/호환성 규칙
- `safety-boundaries.md`: 파일 경계, overwrite, 권한 관련 규칙
- `review-checklist.md`: P1/P2/P3 중심 리뷰 체크리스트

## 작성 원칙
- 규칙은 "왜 필요한지(리스크)"를 한 줄 이상 함께 기록한다.
- 예시는 `docs/examples/`와 연결한다.
- 운영 정책 변경 시 `coordination/DECISIONS.md`와 함께 갱신한다.

## TODO
- [ ] coding 규칙 초안
- [ ] API 에러 계약 규칙 초안
- [ ] 리뷰 체크리스트 상세화

