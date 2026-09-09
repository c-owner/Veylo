# Veylo

**Make your screen alive.**

Veylo는 사용자가 기기의 이미지와 동영상을 선택해 미리보고 Android Live Wallpaper로 적용할 수 있는 로컬 우선 Android 앱입니다. V1은 서버, 계정, 광고, 분석, 원격 저장소를 사용하지 않습니다.

## 현재 구현된 기능

- 공식 Storage Access Framework를 통한 이미지 및 동영상 선택과 읽기 권한 유지 시도
- Room 기반 WallpaperProject 저장, 최근 항목 및 My Wallpapers 그리드
- 이미지/동영상 Compose 미리보기와 동영상 무음 반복 재생
- 이름, Center Crop/Fit, 밝기, 동영상 재생 속도 설정 저장
- MediaMetadataRetriever 기반 동영상 프레임 썸네일 캐시
- DataStore에 현재 적용할 Wallpaper ID 저장
- `WallpaperService`에서 이미지 정적 렌더링 및 Media3 동영상 Surface 직접 출력
- Android 시스템 Live Wallpaper 변경 화면으로 이동
- 원본 URI 접근 실패 시 앱이 중단되지 않는 Missing Asset 상태

## 기술 선택

단일 `app` 모듈의 pragmatic MVVM/clean-ish 구조입니다. V1에서는 Hilt 대신 `VeyloApplication`의 작은 애플리케이션 컨테이너를 사용합니다. 이 방식은 UI ViewModel과 Android가 직접 생성하는 `WallpaperService`가 같은 Room/DataStore/Repository를 명확하고 단순하게 공유하게 하며, 과도한 DI 설정을 피합니다.

- Kotlin, Gradle Kotlin DSL, Jetpack Compose, Material 3, Navigation Compose
- Room, DataStore, Coroutines/Flow
- AndroidX Media3 ExoPlayer
- Coil Compose

`compileSdk`와 `targetSdk`는 Android 16(API 36), `minSdk`는 Android 8.0(API 26)입니다. API 36은 현재 안정 Android 플랫폼 기준이고, API 26부터 지원해 `WallpaperService`, Storage Access Framework, Media3의 현대적 사용과 유지보수성의 균형을 잡았습니다. 패키지명은 소유권을 주장하지 않는 중립 네임스페이스 `app.veylo`를 사용합니다.

## 구조

```text
app/src/main/java/app/veylo/
├── data/              # Room, DataStore, URI/thumbnail repository 구현
├── domain/            # WallpaperProject 및 repository 계약
├── ui/                # Compose 화면, navigation, ViewModel, theme
├── wallpaper/         # WallpaperService, Media3 player, image renderer
└── util/              # 개발 로그
```

## 빌드

1. Android Studio에서 프로젝트를 엽니다.
2. Android SDK Platform 36, Build-Tools 36.x, JDK 17을 설치합니다.
3. SDK 경로를 `local.properties`의 `sdk.dir`에 설정합니다.
4. `./gradlew.bat testDebugUnitTest assembleDebug`를 실행합니다.

## Live Wallpaper 수동 테스트

1. 실제 기기에 debug APK를 설치합니다.
2. Create에서 MP4/H.264 동영상을 고르고 Preview에서 무음 반복을 확인합니다.
3. Save 후 My Wallpapers에서 해당 항목을 열고 Apply Wallpaper를 누릅니다.
4. 시스템 미리보기에서 Veylo Live Wallpaper를 적용합니다.
5. 홈 화면에서 재생되는지, 다른 앱으로 전환했을 때 재생이 멈추는지, 홈으로 돌아올 때 재개되는지 확인합니다.
6. 앱을 종료·재시작해 프로젝트가 남아 있는지 확인합니다.
7. 원본 파일을 삭제하거나 URI 접근을 끊은 뒤 상세 화면이 Missing Asset을 표시하고 앱이 중단되지 않는지 확인합니다.

## 로컬 asset 처리

기본값은 원본을 복사하지 않는 `content://` URI 참조입니다. 선택 시 `takePersistableUriPermission`을 시도하며, 제공자가 영구 권한을 지원하지 않는 URI도 예외 없이 처리합니다. 동영상 목록용 프레임만 앱 캐시에 저장하며, 원본 미디어는 자동 복제하지 않습니다. `SourceType`은 이후 `APP_ASSET`, `APP_COPY`를 지원하도록 이미 모델링되어 있습니다.

## 제한 사항

- 실제 Live Wallpaper 적용 및 제조사별 홈/잠금 화면 선택지는 기기 수동 테스트가 필요합니다.
- V1은 기본 `REPEAT_MODE_ONE`만 제공하며 loop 구간·crossfade·seamless 분석은 아직 제공하지 않습니다.
- 원본 미디어를 잃었을 때 파일 교체 UI와 내부 복사 보관은 아직 없습니다.
- 미리보기 밝기 효과는 저장되며, 동영상 Wallpaper의 밝기 필터 렌더링은 V2 효과 파이프라인에서 확장할 예정입니다.

## Roadmap

### V1

- Local image/video import
- Preview
- Video live wallpaper
- Save wallpaper project
- My Wallpapers
- Wallpaper application

### V2

- Image parallax, particles, additional crop controls
- Custom loop point, crossfade loop, seamless loop editor
- Internal asset copy, video optimization/transcoding

### Future

- Downloadable Veylo wallpaper library and optional CDN/backend
- Creator content, AI generated wallpapers, automatic loop detection
- Dynamic weather/time-aware effects
