# H-003 결과 보고서 (CLI + Output UX)

## 작업 요약
- `devagent generate`, `devagent spec` 초안 CLI 엔트리포인트를 구현했습니다.
- dry-run 기준 결과를 표 + 리스트 형태로 읽기 쉽게 출력하는 formatter를 추가했습니다.
- 설치/실행/예시를 포함한 CLI 문서를 보강했습니다.

## 사용 예시
```bash
# 도움말
./devagent help

# code generate (dry-run)
./devagent generate --user-request "로그인 API 스켈레톤을 만들어줘" --apply false

# spec generate
./devagent spec --user-request "간단한 TODO API 명세"
```

## UX 개선 포인트
- 반복 `curl` 없이 한 줄 명령으로 실행 가능: `devagent generate/spec`
- 결과 핵심값(runId, model, parsedFiles, applyOutcome)을 표로 고정 출력
- 파일별 반영 계획/결과를 리스트로 노출해 dry-run 검토 속도 개선
- CLI 옵션 오류 시 즉시 메시지 출력(잘못된 명령/옵션/enum/bool 값)

## 제약사항
- 현재 CLI는 draft 단계이며 JSON 원문 출력/머신 파싱용 출력(`--json`)은 미지원
- `devagent` 스크립트는 내부적으로 `./gradlew bootRun`을 호출하므로 첫 실행 시 Gradle 부팅 비용이 있음
- 네트워크/모델키 상태에 따라 LLM 호출 성공 여부가 달라짐

## 변경 파일
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/DevAgentOrchestratorApplication.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/cli/CliCommandException.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/cli/DevAgentCliArguments.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/cli/CliResultFormatter.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/cli/DevAgentCliArgumentsTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/cli/CliResultFormatterTest.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/devagent`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/docs/cli-quickstart.md`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/docs/code-agent-api.md`

## 테스트 결과
- 단위/통합 테스트:
  - 명령: `./gradlew clean test`
  - 결과: **BUILD SUCCESSFUL**
- CLI 스모크:
  - `./devagent help` 실행 성공
  - `./devagent generate --user-request "CLI smoke test" ...` 실행 성공 (요약 표/리스트 출력 확인)
  - `./devagent spec --user-request "간단한 TODO API 명세" ...` 실행 성공

## 남은 리스크
- CLI 파서가 현재 최소 기능 위주라 축약 옵션/명령 별칭은 미지원
- 실행 로그가 Spring Boot 기본 로그 포함이라 더 조용한 CLI UX가 필요할 수 있음
- 옵션 확장 시 help 텍스트와 파서 허용 옵션 리스트 동기화 필요

## 메인 병합 시 주의사항
- 현재 워크트리에 H-003 외 변경분(기존 진행 작업)이 함께 존재하므로 병합/체리픽 시 H-003 파일만 선별 반영 필요
- CLI 스크립트(`devagent`) 실행 권한(`chmod +x`) 유지 필요

## 추가 승인 필요 항목
- 없음 (공통 파일 `application.yml`, 공용 모델, 빌드 설정 미변경)

---

# H-003.1 보완 패치 결과 (CLI 오류 UX + API 문서 계약)

## 변경 파일 목록
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/cli/DevAgentCliRunner.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/main/java/me/karubidev/devagent/DevAgentOrchestratorApplication.java`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/docs/code-agent-api.md`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/src/test/java/me/karubidev/devagent/cli/DevAgentCliRunnerTest.java`

## 반영 내용
- CLI 오류를 `DevAgentCliRunner` 내부에서 처리하도록 변경해, 기본 경로에서 Spring Boot 예외 전파/스택트레이스 노출을 차단
- `CliCommandException` 발생 시 종료 코드를 `ExitCodeGenerator` 경로로 유지(`2`)
- CLI 모드에서 배너/startup info 로그를 비활성화해 출력 노이즈 최소화
- API 문서(`docs/code-agent-api.md`)에 `files` 필드 계약(형식/의미/`applyResult`와의 관계) 추가

## 테스트/스모크 명령과 결과
- 단위/통합 테스트
  - 명령: `./gradlew clean test`
  - 결과: **BUILD SUCCESSFUL**
  - 포함 검증: `DevAgentCliRunnerTest`에서 unknown option 시 `getExitCode()==2` 확인
- CLI 스모크 1
  - 명령: `./devagent help`
  - 결과: 성공, usage 텍스트만 출력(불필요한 Boot 실패 로그 없음)
- CLI 스모크 2
  - 명령: `./devagent generate --unknown-option=true`
  - 결과: `[devagent-cli] 지원하지 않는 옵션입니다: --unknown-option` 오류 메시지 출력 확인
  - 비고: Boot 스택트레이스는 출력되지 않음

## 남은 리스크
- `./devagent`가 `gradlew bootRun` 래퍼를 사용하므로, CLI 종료코드가 0이 아닐 때 Gradle 태스크 실패 요약 문구가 추가로 노출될 수 있음
- 오류 메시지 자체는 간결화됐지만, 완전한 “순수 CLI 출력”을 위해서는 추후 실행 래퍼 개선(bootRun 비경유 실행) 검토가 필요

## 병합 시 주의사항
- 워크트리에 H-003/H-002 관련 선행 변경이 함께 존재하므로 H-003.1 파일만 선별 체리픽 필요
- 특히 `DevAgentCliRunner`는 현재 브랜치에서 신규 파일 상태일 수 있어 병합 시 파일 추가/수정 반영 여부를 함께 확인해야 함

---

# H-003.2 보완 패치 결과 (devagent 종료코드 전달 보장)

## 변경 파일 목록
- `/Users/apple/dev_source/Dev-Agent Orchestrator/devagent`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/docs/cli-quickstart.md`
- `/Users/apple/dev_source/Dev-Agent Orchestrator/coordination/REPORTS/H-003-result.md`

## 반영 내용
- `devagent` 실행 경로를 `gradlew bootRun`에서 `gradlew bootJar -> java -jar`로 전환
- CLI 프로세스 종료코드가 Gradle 태스크 실패 코드(1)로 덮이지 않고 애플리케이션 종료코드가 그대로 전달되도록 개선
- 오류 출력 포맷(`[devagent-cli] ...`)은 유지
- 문서에 실행 경로 및 트레이드오프(정확한 종료코드 vs 초기/반복 실행 비용 증가 가능성) 명시

## 테스트/스모크 명령 + 실제 종료코드
- 단위/통합 테스트
  - 명령: `./gradlew clean test`
  - 결과: **BUILD SUCCESSFUL**
- CLI 스모크 1 (help)
  - 명령:
    - `./devagent help > /tmp/h0032_help.out 2>/tmp/h0032_help.err; echo $?`
  - 실제 종료코드: `0`
  - stderr: 빈 출력
- CLI 스모크 2 (unknown option)
  - 명령:
    - `./devagent generate --unknown-option=true > /tmp/h0032_unknown.out 2>/tmp/h0032_unknown.err; echo $?`
  - 실제 종료코드: `2`
  - stderr: `[devagent-cli] 지원하지 않는 옵션입니다: --unknown-option`

## 남은 리스크
- 매 실행 시 `bootJar`를 거치므로, 소스 변경이 잦은 개발 루프에서 실행 지연이 체감될 수 있음
- `java` 런타임이 PATH에 없거나 다른 버전일 경우 실행 실패 가능성(환경 의존)

## 병합 시 주의사항
- 기존 워크트리에 H-003/H-003.1 및 기타 선행 변경이 함께 존재하므로 H-003.2 대상 파일만 선별 반영 필요
- `devagent` 스크립트 실행 권한(`chmod +x`)이 유지되어야 함
