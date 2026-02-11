# H-001 Result Report

## 변경 파일 목록
- src/main/java/me/karubidev/devagent/agents/code/CodeAgentService.java
- src/main/java/me/karubidev/devagent/agents/spec/SpecCodeChainService.java
- src/test/java/me/karubidev/devagent/agents/code/CodeAgentServiceTest.java
- src/test/java/me/karubidev/devagent/agents/spec/SpecCodeChainServiceTest.java
- coordination/REPORTS/H-001-result.md

## 테스트 결과
- 실행 명령: `./gradlew clean test`
- 결과: 통과 (BUILD SUCCESSFUL)
- 보강 테스트:
  - `CodeAgentServiceTest.generateRejectsAbsoluteSpecInputPath`
  - `CodeAgentServiceTest.generateRejectsTraversalSpecInputPath`
  - `SpecCodeChainServiceTest.chainRejectsAbsoluteSpecOutputPath`
  - `SpecCodeChainServiceTest.chainRejectsTraversalSpecOutputPath`
  - `SpecCodeChainServiceTest.chainRejectsOverwriteWhenSpecFileExists`

## 보완 내용
- `specInputPath` 읽기 경로를 `targetRoot` 내부 상대경로로 강제
  - 절대경로 거부
  - `..` 포함 경로 거부
  - 실패 시 `SPEC_INPUT_FAILED`, `CODE_FAILED` 이벤트에 원인 메시지 기록
- `specOutputPath` 쓰기 경로를 `targetRoot` 내부 상대경로로 강제
  - 절대경로 거부
  - `..` 포함 경로 거부
  - 체인 실패 시 `CHAIN_CODE_FAILED` 이벤트에 원인 메시지 기록
- spec 파일 overwrite 정책 명시
  - 기본 안전 정책 적용: 기존 파일 존재 시 덮어쓰기 금지
  - 기존 파일 존재 시 `IllegalArgumentException` 발생

## 남은 리스크
- 경로 차단 정책이 엄격(절대경로 전면 금지)하여 일부 기존 클라이언트 요청 포맷과 충돌할 수 있음
- overwrite 금지 기본값으로 동일 runId 재시도 시 spec 파일 충돌이 발생할 수 있음

## 메인 병합 시 주의사항
- 체인 호출 시 `CodeGenerateRequest.specInputPath`는 상대경로를 전달해야 함
- 운영/CLI 문서에서 `specInputPath`, `specOutputPath`의 허용 규칙(상대경로 + targetRoot 내부)과 overwrite 금지 정책을 반드시 명시해야 함

## 추가 승인 필요 항목
- 없음
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 없음
