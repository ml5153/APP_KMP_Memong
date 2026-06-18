# Memong.app

메몽은 Kotlin Multiplatform 기반의 메모 앱입니다. 현재 소스 구조상 Android 앱(`androidApp`)이 주 기능을 담당하고, KMP `shared` 모듈은 공통 네트워크/플랫폼 코드와 iOS 연동 프레임워크를 제공합니다.

## 프로젝트 개요

- 앱 이름: 메몽
- 패키지: `com.avatye.haru.memo`
- 루트 프로젝트명: `memog2`
- 주요 플랫폼: Android
- 공통 모듈: Kotlin Multiplatform `shared`
- Android UI 방식: XML ViewBinding 기반
- 로컬 데이터베이스: Room
- 외부 연동: Firebase, Google Drive, AdCash, Play In-App Update, KMP Caffeine common SDK

## 주요 기능

- 텍스트/이미지 메모 작성, 수정, 삭제
- 메모 목록의 그리드/리스트 표시와 날짜/태그/섹션 기반 그룹핑
- 중요 메모, 비밀 메모, 휴지통 관리
- 태그 추출 및 메모-태그 매핑
- 그림 메모 작성 캔버스
- 메모 이미지 확대 보기, 저장, 공유
- 로컬 ZIP 백업 및 복원
- Google Drive 백업/복원 안내와 계정 연동 흐름
- 잠금화면 메모 서비스
- 홈 화면 위젯
- Firebase Remote Config 기반 메인 팝업, 스플래시 시간, 인앱 업데이트 설정
- Firebase AI 기반 오늘의 질문/추천 답변 기능
- Firebase Analytics/Crashlytics 및 AdCash 광고 연동

## 모듈 구조

```text
.
├── androidApp/              # Android 앱 본체
│   └── src/main/
│       ├── java/com/avatye/haru/memo/
│       │   ├── data/        # Room DAO, Entity, DB, util, preference, backup
│       │   ├── helper/      # 위젯/잠금화면 보조 로직
│       │   ├── receiver/    # 잠금화면, 위젯 관련 BroadcastReceiver
│       │   ├── service/     # 잠금화면 서비스, 위젯 RemoteViewsService
│       │   └── ui/          # Activity, Adapter, Custom View/Dialog/Header
│       └── res/             # XML layout, drawable, strings, widget, remote config defaults
├── shared/                  # KMP 공통 모듈
│   └── src/
│       ├── commonMain/      # 공통 API/모델/플랫폼 추상화
│       ├── androidMain/     # Android actual 구현
│       └── iosMain/         # iOS actual 구현
├── iosApp/                  # iOS 샘플/호스트 앱과 CocoaPods 연결
├── gradle/                  # Version catalog, Gradle wrapper
├── build.gradle.kts
└── settings.gradle.kts
```

## 핵심 코드 흐름

### 앱 초기화

`HaruMemoApplication`에서 앱 전역 초기화를 수행합니다.

- `PreferenceUtil` 초기화
- 로그 모듈 초기화
- Room DB 싱글턴 생성
- 메모 변경 감지 후 위젯 갱신
- ThreeTenABP 초기화
- Firebase App 및 Remote Config 초기화
- AdCash SDK 초기화
- Firebase AI `gemini-2.5-flash` 모델 초기화

### 메모 데이터

메모는 `MemoEntity`로 저장됩니다.

- `title`: 메모 제목
- `body`: 텍스트, 체크박스, 불릿 등으로 구성되는 본문 블록
- `imagePath`: 본문 위치별 이미지 경로 맵
- `isLocked`: 비밀 메모 여부
- `isImportant`: 중요 메모 여부
- `isDeleted`: 휴지통 이동 여부
- `bgColor`: 메모 배경색
- `category`: 텍스트/이미지 메모 구분

Room 데이터베이스는 `memopad.db`를 사용하며, 주요 테이블은 `memo`, `tag`, `tagging`입니다.

### 메모 작성/수정

`MemoDetailActivity`와 `MemoDetailViewModel`이 메모 편집을 담당합니다.

- 제목/본문 블록을 `BodyItem` 목록으로 변환
- 이미지 존재 여부에 따라 메모 카테고리 결정
- 본문에서 태그를 추출하고 `tag`, `tagging` 테이블 갱신
- 중요/잠금/삭제/배경색 상태 변경

### 메모 목록

`MemoListActivity`가 메인 화면입니다.

- 그리드/리스트 모드 전환
- 날짜/태그/고정 섹션 그룹핑
- 정렬 기준 저장
- 선택 모드, 복사, 삭제, 공유, PDF/이미지 공유 흐름
- Remote Config 메인 팝업 노출
- AI 질문 다이얼로그 호출
- 잠금화면 서비스 시작 조건 확인

### 백업

백업 기능은 `SettingBackupActivity`, `AutoBackupUtil`, `BackupEncrypt`가 중심입니다.

- 로컬 자동 백업은 `/Download/HaruMemo` 경로에 ZIP 파일을 생성합니다.
- DB 파일(`memopad.db`, WAL/SHM)과 설정에 따라 이미지 파일을 함께 압축합니다.
- 일부 레거시 복원 흐름은 AES 기반 암복호화 유틸을 사용합니다.
- Google Drive 관련 화면은 계정 연결, 백업 안내, Drive URL 변환 보조 로직을 포함합니다.

### 잠금화면/위젯

- `LockScreenService`는 foreground service로 동작하며 화면 켜짐 이벤트를 감지합니다.
- 알림, 오버레이, 위치 권한이 없으면 잠금화면 메모 기능을 비활성화합니다.
- `MemoWidgetUpdater`는 Room `Flow`를 구독해 메모 변경 시 위젯 리스트를 갱신합니다.
- 위젯은 small/medium 두 가지 provider XML을 갖습니다.

### shared 모듈

`shared`는 Android/iOS 공통으로 사용할 수 있는 KMP 모듈입니다.

- Android, iOS x64, iOS arm64, iOS simulator arm64 타깃 지원
- iOS 배포 타깃: 16.0
- CocoaPods `shared` framework 생성
- `APIWeather`에서 날씨 API 호출 및 `ResLSWeather` 파싱
- 플랫폼별 `Platform` actual 구현

## 기술 스택

- Kotlin 2.0.21
- Android Gradle Plugin 8.8.0
- Gradle Wrapper 8.10.2
- Kotlin Multiplatform
- Android ViewBinding
- Room 2.7.2
- Kotlinx Serialization
- Firebase Analytics, Remote Config, AI, Crashlytics
- Google Play In-App Update
- Google Drive API / Google Sign-In
- AdCash SDK 및 mediation adapters
- Glide, PhotoView, Material Components, RecyclerView, ConstraintLayout

## 개발 환경

권장 환경:

- Android Studio 최신 안정 버전
- JDK 17 이상
- Xcode 및 CocoaPods: iOS/shared framework 작업 시 필요

Android 빌드:

```bash
./gradlew :androidApp:assembleDebug
```

Android 설치:

```bash
./gradlew :androidApp:installDebug
```

전체 Gradle 검증:

```bash
./gradlew check
```

shared iOS dummy framework 생성:

```bash
./gradlew :shared:generateDummyFramework
```

iOS Pod 설치:

```bash
cd iosApp
pod install
```

## 설정 파일

Android 앱 실행에는 다음 설정 파일이 관련됩니다.

- `androidApp/google-services.json`: Firebase 설정
- `androidApp/src/main/res/xml/remote_config_defaults.xml`: Remote Config 기본값
- `androidApp/build.gradle.kts`: applicationId, signingConfig, BuildConfigField, 광고/업데이트 설정
- `settings.gradle.kts`: Google/MavenCentral/JitPack 및 사내 Maven 저장소 설정
- `iosApp/Podfile`: `shared` KMP framework CocoaPods 연결

릴리즈 빌드는 `androidApp/build.gradle.kts`의 signingConfig를 사용합니다. 로컬/CI 환경에서 keystore 경로와 보안 값 관리 방식을 별도로 확인해야 합니다.

## 권한

`AndroidManifest.xml` 기준으로 다음 권한을 사용합니다.

- 네트워크: `INTERNET`, `ACCESS_NETWORK_STATE`
- 알림/foreground service: `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`
- 잠금화면/오버레이: `SYSTEM_ALERT_WINDOW`
- 카메라/이미지: `CAMERA`, `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`
- 위치/날씨: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
- Google Drive 계정 흐름: `READ_CONTACTS`
- 진동: `VIBRATE`

## 빌드 변형과 운영 설정

현재 Android 모듈은 기본 `debug`/`release` build type을 사용합니다.

- `debug`
  - 난독화 비활성화
  - 디버그 가능
  - 인앱 업데이트 타입을 Gradle BuildConfigField로 오버라이드
- `release`
  - signingConfig 적용
  - 난독화 비활성화
  - 인앱 업데이트와 강제 업데이트는 Remote Config 기반

광고 placement, 앱 ID, secret, 업데이트 관련 값은 `BuildConfigField`로 주입됩니다. 운영 저장소에서는 민감 값의 노출 여부를 반드시 점검하세요.

## 개발 시 주의사항

- 현재 워크트리에는 `.gradle`, `build`, `.idea`, `.kotlin` 같은 생성물이 많이 생길 수 있습니다. 커밋 전 `.gitignore`와 staged 파일을 확인하세요.
- Room DB 버전은 1이며 migration 정의가 없습니다. 스키마 변경 시 migration 추가가 필요합니다.
- `MemoListViewModel`은 현재 비어 있고, 메인 화면 로직 대부분은 `MemoListActivity`에 있습니다.
- 백업 경로 문자열은 UI 문구와 실제 자동 백업 경로가 다를 수 있으니 배포 전 확인이 필요합니다.
- Android 13 이상 알림 권한, Android 11 이상 저장소 권한, 오버레이 권한은 기능별 UX에 직접 영향을 줍니다.
- Remote Config JSON 형식이 깨지면 기본값으로 fallback되도록 되어 있지만, 운영 전 key/value 검증이 필요합니다.

## 빠른 진입점

- 앱 시작: `androidApp/src/main/java/com/avatye/haru/memo/ui/SplashActivity.kt`
- 메인 목록: `androidApp/src/main/java/com/avatye/haru/memo/ui/MemoListActivity.kt`
- 메모 상세/편집: `androidApp/src/main/java/com/avatye/haru/memo/ui/MemoDetailActivity.kt`
- 메모 DB: `androidApp/src/main/java/com/avatye/haru/memo/data/database/MemoPadDatabase.kt`
- 메모 DAO: `androidApp/src/main/java/com/avatye/haru/memo/data/dao/MemoDao.kt`
- 메모 엔티티: `androidApp/src/main/java/com/avatye/haru/memo/data/entity/MemoEntity.kt`
- 앱 설정 저장소: `androidApp/src/main/java/com/avatye/haru/memo/data/utils/PreferenceUtil.kt`
- Remote Config: `androidApp/src/main/java/com/avatye/haru/memo/data/utils/RemoteConfigUtil.kt`
- 자동 백업: `androidApp/src/main/java/com/avatye/haru/memo/data/utils/AutoBackupUtil.kt`
- 잠금화면 서비스: `androidApp/src/main/java/com/avatye/haru/memo/service/LockScreenService.kt`
- 위젯 갱신: `androidApp/src/main/java/com/avatye/haru/memo/helper/MemoWidgetUpdater.kt`
- shared 날씨 API: `shared/src/commonMain/kotlin/com/avatye/haru/network/api/APIWeather.kt`
