# H-006 CLI 고도화 (`--json`, 옵션 별칭, 반복 실행 성능)

Owner: WT-6 (`codex/cli-json-perf`)
Priority: High

## 목표
- CLI를 자동화 친화적으로 개선한다.
  - 머신 파싱용 `--json` 출력
  - 자주 쓰는 옵션 별칭(alias)
  - 반복 실행 성능 개선(불필요한 빌드/초기화 비용 최소화)

## 작업 범위
- CLI 명령 파서/옵션 처리 개선
  - `src/main/java/me/karubidev/devagent/cli/` 하위
  - 필요 시 `devagent` 스크립트 인자 전달/실행 전략 보완
- `generate`, `spec` 중심으로 `--json` 출력 모드 추가
  - 성공/실패 모두 JSON 구조를 일관되게 반환
  - 기존 human-readable 출력은 기본값으로 유지
- 옵션 별칭 추가(예: `-p`/`--project`, `-r`/`--root` 형태)
  - 기존 옵션과 충돌하지 않도록 정리
- 반복 실행 성능 개선
  - 실행 경로 분석 및 병목 완화
  - 최소 1개 이상 성능 개선 근거(측정 로그 또는 비교 결과) 보고
- 문서 업데이트
  - `docs/cli-quickstart.md`
  - 필요 시 `docs/code-agent-api.md`의 CLI 관련 섹션
- 테스트 추가/보강
  - CLI JSON 출력 정상/오류 케이스
  - 옵션 별칭 파싱 케이스

## 수용 기준
1. `devagent generate ... --json`이 파싱 가능한 JSON만 출력한다.
2. `devagent spec ... --json`이 파싱 가능한 JSON만 출력한다.
3. 대표 옵션 별칭이 동작하며 기존 옵션과 충돌이 없다.
4. 반복 실행 성능 개선 근거가 결과 리포트에 포함된다.
5. `./gradlew clean test --no-daemon` 통과.

## 비범위
- 신규 agent 추가
- API 엔드포인트 스키마 변경
- 체인 실패 부분 성공 정책(H-007) 결정

## 제약
- handoff 범위 밖 수정 금지.
- 공통 파일(`application.yml`, 공용 모델, 빌드 설정) 변경 필요 시 메인 컨트롤 사전 승인 필수.
- 기존 CLI human-readable 출력 계약은 기본값으로 유지.
- JSON 출력은 안정된 필드 구조를 유지(버전 내 갑작스런 breaking change 금지).

## 보고서
- 완료 시 `coordination/REPORTS/H-006-result.md` 생성
- 리뷰 스레드 결과는 `coordination/REPORTS/H-006-review.md` 작성
- 필수 항목:
  - 변경 파일 목록
  - 테스트 결과(명령 + 통과/실패)
  - JSON 출력 예시(성공/실패 각각)
  - 성능 개선 근거(비교 기준 포함)
  - 남은 리스크
  - 공통 파일 변경 승인 여부
