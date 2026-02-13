# H-008.1 결과 보고서 (심볼릭 링크 경계 우회 차단)

## 상태
- 현재 상태: **완료 (수용기준 충족 + 테스트 게이트 통과)**

## 변경 파일
- `src/main/java/me/karubidev/devagent/agents/code/apply/FileApplyService.java`
- `src/test/java/me/karubidev/devagent/agents/code/apply/FileApplyServiceTest.java`

## 구현 요약
- `FileApplyService`에 실제 경로 기준 경계 재검증을 추가했습니다.
  - 기존: `resolved.startsWith(normalizedRoot)` prefix 검증
  - 추가: `findNearestExistingAncestor` + `toRealPath()` 기반 실제 경로 환산 후 root boundary 재검증
- 실제 쓰기 직전(디렉터리 생성 전/후) 모두 경계 재검증을 수행해 심볼릭 링크 경유 우회를 `REJECTED`로 차단합니다.
- 경계 위반은 `REJECTED`, 파일시스템 I/O 예외는 `ERROR`로 유지해 상태 계약 혼합을 방지했습니다.

## 재현-차단 증빙
- 추가 테스트: `rejectSymlinkBoundaryBypass`
  - 재현 구성: `tempDir/link-outside` 심볼릭 링크를 `tempDir` 바깥 디렉터리로 연결
  - 입력 파일 경로: `link-outside/evil.txt`
  - 기대/확인 결과:
    - 상태: `REJECTED`
    - 메시지: `path traversal is not allowed`
    - 외부 경로 파일(`outsideDir/evil.txt`) 생성되지 않음 (`Files.exists(...) == false`)

## 수용기준 점검
1. 심볼릭 링크 경유 우회 케이스에서 root 바깥 쓰기 차단: **충족**
2. 우회 차단 시 `REJECTED` 상태/메시지 테스트 고정: **충족**
3. 기존 경계 케이스 회귀 없음: **충족**
4. `./gradlew clean test --no-daemon` 통과: **충족**

## 테스트 결과
- 대상 테스트:
  - 명령: `./gradlew test --no-daemon --tests me.karubidev.devagent.agents.code.apply.FileApplyServiceTest`
  - 결과: **BUILD SUCCESSFUL**
- 승인 게이트:
  - 명령: `./gradlew clean test --no-daemon`
  - 결과: **BUILD SUCCESSFUL**

## 남은 리스크
- 쓰기 직전 재검증을 추가했지만, 파일시스템 동시성(TOCTOU) 리스크를 완전히 제거하려면 더 강한 OS/FS 레벨 제약(예: dirfd 기반 안전 write)이 필요할 수 있습니다.
- 심볼릭 링크 미지원 환경에서는 신규 테스트가 skip 될 수 있어, 해당 환경 전용 대체 검증 수단은 후속 고려가 필요합니다.

## 승인 필요 항목
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경: **없음**
- 사전 승인 필요 항목: **해당 없음**
