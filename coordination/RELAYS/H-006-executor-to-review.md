# [H-006] Executor -> Review Relay Prompt

## 라운드 정보
- handoff: `coordination/HANDOFFS/H-006-cli-json-performance.md`
- result: `coordination/REPORTS/H-006-result.md`

## 구현 요약
- 기존 범위 유지:
  - `generate/spec` JSON 출력 모드(`--json`, `-j`)
  - 대표 옵션 alias(`-p/-r/-u/-m/-k/-j`, `-a`, `-c`)
  - `devagent` 반복 실행 성능 개선(boot jar 재사용)
- 금회 보완(H-006-PATCH):
  1. alias 중복 입력 동치 비교 보완
     - 동일 canonical key 중복 시 enum/bool 계열은 normalize 동치 허용
     - 자유 문자열/경로 계열은 strict 비교 유지
  2. 분리형 long option 값 소비 규칙 보완
     - `--key value`에서 단일 `-` 시작 value 허용(정책 키 기반)
     - `--` 시작 토큰은 옵션으로 유지

## 변경 파일
- `src/main/java/me/karubidev/devagent/cli/DevAgentCliArguments.java`
- `src/test/java/me/karubidev/devagent/cli/DevAgentCliArgumentsTest.java`

## 테스트 게이트
- 실행 명령:
  - `./gradlew test --tests "me.karubidev.devagent.cli.*" --no-daemon`
  - `./gradlew clean test --no-daemon`
- 결과:
  - 모두 **BUILD SUCCESSFUL**

## 추가 회귀 테스트
- `parseAllowsEquivalentEnumAliasValues`
- `parseAllowsEquivalentBooleanAliasValues`
- `parseConsumesDashPrefixedSeparatedValueForUserRequest`
- `parseConsumesDashPrefixedSeparatedValueForTargetRoot`
- `parseConsumesRiskLevelDashValueThenFailsEnumValidation`

## 정책 명시 (`--risk-level -1`)
- 현재 정책: `-1`을 분리형 값으로 소비한 뒤 enum 검증에서 실패 처리
- 기대: `열거형 옵션 값이 올바르지 않습니다` 예외 발생

## 리뷰 집중 포인트
1. `DevAgentCliArguments`의 키별 동치 비교가 false positive/false negative 없이 동작하는지
2. `--key value`에서 단일 `-` 값 소비가 기존 옵션 토큰 해석과 충돌하지 않는지
3. 추가 테스트가 P2 재발 방지에 충분한지

## 알려진 리스크 / 오픈 이슈
- 단일 `-` 시작 값을 허용하는 키는 allow-list 기반 정책이며, 필요 시 키 확장/축소가 필요할 수 있음

## 요청 사항 (Review Thread)
- `coordination/REPORTS/H-006-review.md` 재작성
- P1/P2/P3 심각도 기준으로 근거 파일/라인 포함
- 최종 결론은 `No findings` 또는 `LOW`로 판정 가능한지 명시
