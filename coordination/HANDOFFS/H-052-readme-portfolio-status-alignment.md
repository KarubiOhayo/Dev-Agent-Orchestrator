# H-052 README portfolio status alignment

Owner: WT-52 (`codex/h052-readme-portfolio-status-alignment`)
Priority: Highest

## 목표
- root `README.md`의 entrypoint / current-limits / next-focus copy를 H-051 결과와 정렬해 external-facing portfolio package 상태를 일관되게 보이게 한다.
- `docs/portfolio-case-study.md`가 이미 존재한다는 사실을 README가 다시 future work처럼 약화하지 않도록 조정한다.
- README는 entrypoint, case study는 second-layer narrative라는 역할 분리를 유지한다.

## 작업 범위
- 신규/수정 허용:
  - `README.md`
- 참고 전용:
  - `docs/portfolio-case-study.md`
  - `docs/PROJECT_OVERVIEW.md`
  - `coordination/TASK_BOARD.md`
  - `coordination/DECISIONS.md`
  - `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`
  - `coordination/REPORTS/H-051-result.md`
  - `coordination/REPORTS/H-051-review.md`
  - `coordination/RELAYS/H-051-review-to-main.md`

## 구현 지침
- `README.md`의 docs map 내 case study 링크는 유지한다.
- `Current Limits And Next Focus` 문구는 case study foundation이 이미 존재한다는 현재 상태를 반영해야 한다. "portfolio copy / case study" 전체를 아직 미완료인 하나의 future-work 묶음처럼 다시 쓰지 않는다.
- 남은 후속 작업은 demo / showcase walkthrough, evidence / report export, 필요한 최소 polishing 쪽으로 좁힌다.
- README 전체 구조를 다시 쓰지 말고, 문제된 상태 문구를 최소 수정으로 정렬한다.
- README가 case study 본문을 대신하지 않게 한다. README는 entrypoint, case study는 deeper narrative로 역할을 분리한다.
- 없는 운영 성과, 외부 사용자 수, 배포 상태, 성능 수치를 추가하지 않는다. 모든 클레임은 현재 문서/구현으로 역추적 가능해야 한다.

## 수용 기준
1. `README.md`가 `docs/portfolio-case-study.md` 존재와 H-051 결과를 반영하며, case study를 여전히 미래 작업으로 오해하게 만들지 않는다.
2. `README.md`의 current-limits / next-focus 문구가 `coordination/TASK_BOARD.md`, `docs/PROJECT_OVERVIEW.md`, `coordination/REPORTS/CURRENT_STATUS_2026-03-18.md`의 active focus와 정합하다.
3. README 수정은 최소 범위 copy alignment에 머물고, 구조 재작성이나 신규 섹션 확장은 하지 않는다.
4. README와 case study의 역할 분리(entrypoint vs second-layer narrative)가 유지된다.
5. handoff 범위 밖 파일, 공통 승인 대상 파일, 코드/설정은 수정하지 않는다.
6. `./gradlew clean test --no-daemon`를 통과한다.

## 비범위
- `docs/portfolio-case-study.md` 전면 재작성
- demo / showcase walkthrough 패키징
- evidence / report export bundle 제작
- 코드/설정 변경
- fallback-warning parked 트랙 재개

## 제약
- handoff 범위 밖 파일 수정 금지
- 공통 파일 변경 필요 시 즉시 중단하고 Main-Control 승인 요청만 남긴다
- README 상태 문구는 case study 완료 상태를 반영하되, portfolio package 전체가 이미 완결된 것처럼 과장하지 않는다.

## 보고서
- 완료 시 `coordination/REPORTS/H-052-result.md` 생성
- 리뷰 입력 릴레이 `coordination/RELAYS/H-052-executor-to-review.md` 생성
- 필수 포함:
  - 변경 전/후 핵심 문구와 정렬 이유
  - README current-limits / next-focus가 어떤 source 문서와 맞춰졌는지
  - 테스트 결과(`./gradlew clean test --no-daemon`)
  - 남은 후속 제안(`demo`, `evidence export` 관점)
