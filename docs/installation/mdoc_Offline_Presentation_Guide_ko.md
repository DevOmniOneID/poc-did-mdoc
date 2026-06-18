# ISO 18013-5 오프라인 프레젠테이션 설치 및 테스트 가이드

본 문서는 Wallet(Holder)과 mDoc Reader(Verifier) 앱을 설치하고, BLE/NFC 등을 통한 근접 오프라인 프레젠테이션을 테스트하는 방법을 설명합니다. Android와 iOS 환경 모두를 다룹니다.

> **사전 조건**: OID4VCI를 통한 mDoc 발급 과정이 포함되므로, 백엔드 서버(Issuer Server)가 사전에 구동되어 있어야 합니다. 서버 구동 방법은 [OID4VC 설치 가이드](oid4vc_Installation_Guide_ko.md)의 4장을 참조하세요.

---

## 1. 시스템 요구 사항

### 1.1. Android

| 구분 | 내용 |
| :--- | :--- |
| **OS** | Android 8.0 (API 26) 이상 |
| **Language** | Java 21 |
| **IDE** | Android Studio Hedgehog (2023.1.1) 이상 |
| **Build System** | Gradle 8.2 / AGP 8.2.1 |
| **테스트 환경** | **실물 디바이스 2대** (BLE/NFC 필수) |

| 구분 | Wallet (Holder) | Reader (Verifier) |
| :--- | :--- | :--- |
| **앱 경로** | `source/apps/android-app` | `source/apps/android-mdoc-reader` |
| **필수 하드웨어** | BLE, NFC (HCE) | BLE, NFC, Camera |
| **선택 하드웨어** | WiFi Aware | WiFi Aware |

### 1.2. iOS

| 구분 | 내용 |
| :--- | :--- |
| **OS** | iOS 15.0 이상 |
| **Language** | Swift 5.0 |
| **IDE** | Xcode 16.2 이상 |
| **Package Manager** | Swift Package Manager (SPM) |
| **테스트 환경** | **iPhone 실물 디바이스 2대** (BLE 필수) |

| 구분 | Wallet (Holder) | Reader (Verifier) |
| :--- | :--- | :--- |
| **앱 경로** | `source/apps/ios-app` | `source/apps/ios-mdoc-reader` |
| **Xcode 프로젝트** | `oid4vc/oid4vc.xcodeproj` | `mdocReader-ios/mdocReader.xcodeproj` |
| **필수 하드웨어** | BLE | BLE, Camera |

> **중요**: 에뮬레이터/시뮬레이터에서는 BLE/NFC를 사용할 수 없으므로, 오프라인 프레젠테이션 테스트에는 **실물 디바이스**가 반드시 필요합니다. 단, OID4VCI 발급 단계는 에뮬레이터/시뮬레이터에서도 수행 가능합니다.

---

## 2. Wallet 앱 설치 (Holder)

### 2.1. Android

#### 2.1.1. 프로젝트 열기

1. Android Studio를 실행합니다.
2. **File → Open**을 선택하여 `source/apps/android-app` 폴더를 엽니다.
3. Gradle 동기화가 완료될 때까지 기다립니다.

#### 2.1.2. 빌드 및 설치

**IDE 사용 (권장)**:
1. Holder 역할의 Android 디바이스를 USB로 연결합니다.
2. 상단 메뉴에서 초록색 ▶ 버튼을 클릭하여 빌드 및 설치합니다.

**콘솔 명령어**:
```bash
cd source/apps/android-app

# Gradle Wrapper 실행 권한 부여
chmod 755 ./gradlew

# 빌드
./gradlew assembleDebug

# 연결된 디바이스에 설치
./gradlew installDebug
```

#### 2.1.3. 권한 허용

앱 최초 실행 시 다음 권한을 허용해야 합니다:
- **카메라** — QR 코드 스캔
- **위치** — BLE / WiFi Aware 사용
- **블루투스** — BLE 통신
- **근처 기기** — Android 13 이상

### 2.2. iOS

#### 2.2.1. 프로젝트 열기

1. Xcode를 실행합니다.
2. **File → Open**을 선택하여 `source/apps/ios-app/oid4vc/oid4vc.xcodeproj` 파일을 엽니다.
3. Xcode가 SPM 의존성을 자동으로 다운로드할 때까지 기다립니다.

#### 2.2.2. 빌드 및 설치

1. Holder 역할의 iPhone을 USB로 연결합니다.
2. Xcode 상단에서 대상 디바이스를 선택합니다.
3. **Product → Run** (또는 `Cmd + R`)을 눌러 빌드 및 설치합니다.

> **Signing**: 첫 빌드 시 Xcode에서 Signing & Capabilities 설정이 필요할 수 있습니다. Team을 본인의 Apple Developer 계정으로 변경하고, Bundle Identifier를 필요에 따라 수정하세요.

#### 2.2.3. 권한 허용

앱 최초 실행 시 다음 권한을 허용해야 합니다:
- **카메라** — QR 코드 스캔
- **블루투스** — BLE 오프라인 프레젠테이션

#### 2.2.4. SPM 의존성

| 패키지 | 버전 | 설명 |
| :--- | :--- | :--- |
| CodeScanner | 2.5.2 | QR 코드 스캔 (SwiftUI) |
| SwiftCBOR | master | CBOR 인코딩/디코딩 (ISO 18013-5) |

#### 2.2.5. 서버 연결 설정

Wallet 앱의 API 서버 주소는 `Network/APIService.swift`의 `baseURL`에서 설정합니다.

> **주의**: 시뮬레이터에서는 `localhost`를 직접 사용할 수 있지만, 실물 디바이스에서는 동일 네트워크의 IP 주소를 사용해야 합니다.

---

## 3. mDoc Reader 앱 설치 (Verifier)

### 3.1. Android

#### 3.1.1. 프로젝트 열기

1. 별도의 Android Studio 창에서 `source/apps/android-mdoc-reader` 폴더를 엽니다.
2. Gradle 동기화가 완료될 때까지 기다립니다.

#### 3.1.2. 빌드 및 설치

**IDE 사용 (권장)**:
1. Verifier 역할의 Android 디바이스를 USB로 연결합니다.
2. 상단 메뉴에서 초록색 ▶ 버튼을 클릭하여 빌드 및 설치합니다.

**콘솔 명령어**:
```bash
cd source/apps/android-mdoc-reader

# Gradle Wrapper 실행 권한 부여
chmod 755 ./gradlew

# 빌드
./gradlew assembleDebug

# 연결된 디바이스에 설치
./gradlew installDebug
```

#### 3.1.3. 권한 허용

앱 최초 실행 시 다음 권한을 허용해야 합니다:
- **카메라** — QR 코드 스캔
- **위치** — BLE 사용
- **블루투스** — BLE 통신
- **근처 기기** — Android 13 이상

#### 3.1.4. Issuer 인증서 구성

Reader 앱은 `app/src/main/assets/certs/` 디렉토리에 포함된 Root CA 인증서를 기반으로 발급자 인증서 체인을 검증합니다.

| 인증서 파일 | 용도 |
| :--- | :--- |
| `x509_rootca.crt` | 기본 Root CA |
| `x509_eudi_age_verification_issuer_ca01_test_rootca.crt` | EUDI 테스트 Root CA |
| `x509_oidf_test_cert.crt` | OIDF 테스트 인증서 |

> 커스텀 Issuer를 사용하는 경우, 해당 Issuer의 Root CA 인증서를 이 디렉토리에 추가해야 합니다.

#### 3.1.5. DID 신뢰 발급자 설정

Reader 앱은 X.509 인증서 체인 검증 외에 **DID 기반 신뢰 발급자 캐시**를 지원합니다. 앱 시작 시 백그라운드에서 자동으로 캐시를 최신화하며, Settings 화면의 버튼으로 수동 최신화도 가능합니다.

**구성 개요** — 이 캐시는 역할이 분리된 **두 출처**가 협력합니다.

| 출처 | 제공하는 것 | 의미 | 비고 |
|------|------------|------|------|
| **mock 서버** (PoC용, 직접 구동) | 신뢰 발급자 **DID 목록** | "누구를 신뢰하는가" | 실제 운영에선 서명된 신뢰목록 관리자로 대체 |
| **API-gateway** (기존 발급 인프라) | DID **Document**(공개키) | "그 발급자의 키" | 실제 인프라 그대로 사용 |

```
mock 서버 ──(신뢰 DID 목록)──▶ Reader 앱
                                   │  목록의 각 DID에 대해
                                   ▼
API-gateway ──(DID Document=키)──▶ Reader 앱 ──▶ trusted_issuers.json (로컬 캐시)
```

즉 **mock 서버는 "신뢰 목록"만** 주고, 실제 키(DID Document)는 **API-gateway**에서 받아 하나의 로컬 캐시로 합칩니다. "캐시에 그 DID Document가 있다 = 신뢰 목록에 있다 + 키도 있다"가 되어, BLE/NFC 오프라인 상태에서도 발급자 서명을 검증할 수 있습니다.

##### 3.1.5.1. reader_config.yml 설정

`app/src/main/assets/reader_config.yml` 파일에서 아래 두 URL을 실제 환경에 맞게 수정합니다.

```yaml
# 신뢰 발급자 DID 목록을 제공하는 mock 서버 URL
trustedIssuerListUrl: "http://<서버_IP>:9090/trusted-issuers"
# DID Document를 조회하는 API-gateway base URL
didDocGatewayUrl: "http://<서버_IP>:8098"
```

> **에뮬레이터 사용 시**: 호스트 PC에서 서버를 구동하는 경우 IP 대신 `10.0.2.2`를 사용합니다.
> ```yaml
> trustedIssuerListUrl: "http://10.0.2.2:9090/trusted-issuers"
> didDocGatewayUrl: "http://10.0.2.2:8098"
> ```

##### 3.1.5.2. Mock 신뢰 발급자 목록 서버 구동

`source/apps/android-mdoc-reader/tools/MockIssuerListServer.java`는 신뢰 DID 목록을 제공하는 간단한 HTTP 서버입니다. JDK 21만 있으면 빌드 없이 바로 실행할 수 있습니다.

```bash
cd source/apps/android-mdoc-reader/tools
java MockIssuerListServer.java
# 출력: Mock trusted-issuer list server: http://0.0.0.0:9090/trusted-issuers
```

신뢰 발급자 DID를 추가하려면 `MockIssuerListServer.java` 상단의 `DIDS` 배열을 수정합니다.

```java
private static final String[] DIDS = {"did:omn:issuer", "did:omn:issuer2"};
```

##### 3.1.5.3. 신뢰 발급자 캐시 최신화

Reader 앱은 **시작 시 자동으로** 신뢰 발급자 목록을 최신화합니다. 수동으로 최신화하려면:

1. Reader 앱에서 **Settings** 화면으로 이동합니다.
2. **Refresh Trusted Issuers** 버튼을 누릅니다.
3. 완료 시 Toast 메시지와 함께 캐시된 발급자 목록이 화면에 표시됩니다.

> **참고**: 이 최신화 과정(DID 목록 수신 → DID Document fetch → 로컬 저장)은 BLE/NFC가 없는 **에뮬레이터에서도 테스트 가능**합니다. 오프라인 BLE 프레젠테이션 자체는 실물 디바이스가 필요합니다.

### 3.2. iOS

#### 3.2.1. 프로젝트 열기

1. 별도의 Xcode 창에서 `source/apps/ios-mdoc-reader/mdocReader-ios/mdocReader.xcodeproj` 파일을 엽니다.
2. SPM 의존성이 자동으로 다운로드될 때까지 기다립니다.

#### 3.2.2. 빌드 및 설치

1. Verifier 역할의 iPhone을 USB로 연결합니다.
2. Xcode 상단에서 대상 디바이스를 선택합니다.
3. **Product → Run** (또는 `Cmd + R`)을 눌러 빌드 및 설치합니다.

> **Signing**: Wallet 앱과 마찬가지로 Team 및 Bundle Identifier 설정이 필요할 수 있습니다.

#### 3.2.3. 권한 허용

앱 최초 실행 시 다음 권한을 허용해야 합니다:
- **카메라** — Device Engagement QR 코드 스캔
- **블루투스** — BLE 통신

#### 3.2.4. SPM 의존성

| 패키지 | 버전 | 설명 |
| :--- | :--- | :--- |
| CodeScanner | 2.5.2 | QR 코드 스캔 |
| SwiftCBOR | master | CBOR 인코딩/디코딩 |

---

## 4. mDoc 발급 (OID4VCI)

오프라인 프레젠테이션을 수행하려면 먼저 Wallet 앱에 mDoc Credential을 발급받아야 합니다.

> **참고**: 이 단계는 [OID4VC 설치 가이드](oid4vc_Installation_Guide_ko.md)의 6.1절과 동일한 OID4VCI 흐름입니다.

### 4.1. Credential Offer 생성 (Issuer Server)

1. 브라우저에서 Issuer Server 테스트 페이지에 접속합니다.

```
http://<IP>:8080/oid4vci/test
```

2. `User ID`를 입력한 후 **Generate QR Code**를 눌러 Credential Offer QR 코드를 생성합니다.

### 4.2. Wallet 앱에서 발급 수행

#### Android

1. 앱 메인 화면에서 **Scan QR** 버튼을 눌러 QR 코드를 스캔합니다.
   - 에뮬레이터 또는 QR 스캔이 어려운 경우, `Full Offer URI`를 복사하여 하단의 `Enter QR Code Data` 입력란에 붙여넣기 후 **Issue**를 누릅니다.
2. PIN 입력 화면이 나타나면 Issuer 테스트 페이지의 4자리 `Tx Code`를 입력합니다.
3. `Select Issued Credential` 팝업에서 **mDL** 또는 **PID**를 선택한 후 **CONFIRM**을 누릅니다.
4. 발급이 완료되면 **View VC**에서 발급된 mDoc을 확인할 수 있습니다.

#### iOS

1. 앱 메인 화면에서 **Start Issuance** 버튼을 눌러 QR 코드를 스캔합니다.
   - 시뮬레이터에서는 하단 입력란에 `Full Offer URI`를 붙여넣기합니다.
2. PIN 입력 화면이 나타나면 Issuer 테스트 페이지의 4자리 `Tx Code`를 입력합니다.
3. Credential 선택 화면에서 **mDL** 또는 **PID**를 선택합니다.
4. 발급이 완료되면 **View VC**에서 발급된 mDoc을 확인할 수 있습니다.

---

## 5. 오프라인 프레젠테이션 테스트

발급이 완료되면 인터넷 연결 없이 두 대의 디바이스 간 근접 프레젠테이션을 테스트합니다.

> **참고**: Android Wallet ↔ iOS Reader, iOS Wallet ↔ Android Reader 등 크로스 플랫폼 조합도 BLE 기반으로 동작합니다.

### 5.1. [Wallet] Device Engagement QR 생성

1. Wallet 앱에서 **View VC**를 눌러 발급된 mDoc을 확인합니다.
2. **Offline Present** 버튼을 눌러 Device Engagement QR 코드를 생성합니다.
3. QR 코드가 화면에 표시되면 앱은 BLE Peripheral 서버를 자동으로 시작하고 Reader의 연결을 대기합니다.

> Android의 경우 BLE, NFC HCE, WiFi Aware 서버가 동시에 시작됩니다. iOS의 경우 BLE 서버만 시작됩니다.

### 5.2. [Reader] QR 스캔 및 문서 요청

1. mDoc Reader 앱에서 요청할 문서 유형(mDL, PID 등)과 클레임을 선택합니다.
2. **QR Scan** (Android) 또는 **SCAN QR CODE** (iOS) 버튼을 눌러 카메라를 활성화하고, Wallet에 표시된 Device Engagement QR 코드를 스캔합니다.

> **NFC 사용 시 (Android만 해당)**: QR 스캔 대신 Reader 디바이스를 Wallet 디바이스에 탭하여 NFC Device Engagement를 수행할 수도 있습니다.

### 5.3. 세션 수립 및 데이터 전송

QR 스캔(또는 NFC 탭) 후 다음 과정이 자동으로 수행됩니다:

1. **BLE 연결 수립** — Reader(Central)와 Wallet(Peripheral) 간 BLE 연결이 설정됩니다.
2. **세션 암호화** — ISO 18013-5 표준에 따른 P256 ECDH 키 합의 및 AES-256-GCM 세션 암호화가 수행됩니다.
3. **DeviceRequest 전송** — Reader에서 선택한 문서/클레임 요청이 암호화되어 Wallet으로 전송됩니다.
4. **DeviceResponse 전송** — Wallet이 요청에 해당하는 데이터를 Device Authentication과 함께 암호화하여 응답합니다.

### 5.4. [Reader] 검증 결과 확인

전송이 완료되면 Reader 앱에서 다음 항목을 검증하고 결과를 표시합니다:

- **발급자 인증서 체인 / Issuer 서명 검증**
- **DeviceAuth 서명 검증** (COSE_Sign1)
- **데이터 무결성 검증** (MSO / Data Integrity)
- **유효 기간 확인** (validFrom, validUntil)
- **클레임 데이터 표시** (이름, 생년월일, 운전면허 정보 등)

---

## 6. 전송 방식별 참고 사항

| 전송 방식 | Wallet 역할 | Reader 역할 | Android | iOS |
| :--- | :--- | :--- | :--- | :--- |
| **BLE** | Peripheral (GATT Server) | Central (GATT Client) | 지원 | 지원 |
| **NFC** | HCE (Host Card Emulation) | Reader Mode | 지원 | 미지원 |
| **WiFi Aware** | Publisher | Subscriber | 지원 | 미지원 |

- **BLE**: 기본 전송 방식이며, Android/iOS 모두 지원합니다.
- **NFC**: Android에서만 지원됩니다. AID: `A0000002480400`, 기기 탭이 필요합니다.
- **WiFi Aware**: Android에서만 지원되며, 모든 디바이스에서 사용 가능하지 않을 수 있습니다.

---

## 7. 트러블슈팅

### 7.1. 공통

| 증상 | 원인 | 해결 방법 |
| :--- | :--- | :--- |
| BLE 연결이 수립되지 않음 | 블루투스 비활성화 또는 권한 미허용 | 두 디바이스의 블루투스를 켜고, 앱 권한(블루투스, 위치)을 확인합니다. |
| QR 코드 스캔 실패 | 카메라 권한 미허용 또는 QR 인식 불량 | 카메라 권한을 허용하고, QR 코드가 선명하게 보이도록 거리를 조절합니다. |
| 전송 중 타임아웃 | 디바이스 간 거리가 너무 멀거나 간섭 | 두 디바이스를 가까이 놓고 (1m 이내) 다시 시도합니다. |
| 발급자 인증서 검증 실패 | Root CA 인증서 불일치 | Android Reader의 `assets/certs/` 디렉토리에 올바른 Root CA 인증서가 포함되어 있는지 확인합니다. |
| mDoc 발급 실패 | Issuer Server 미구동 또는 네트워크 연결 불가 | Issuer Server가 구동 중이고 디바이스에서 접근 가능한지 확인합니다. |
| 에뮬레이터/시뮬레이터에서 오프라인 제출 불가 | BLE/NFC 미지원 | 오프라인 프레젠테이션은 실물 디바이스에서만 테스트 가능합니다. |
| 신뢰 발급자 캐시 최신화 실패 (Toast에 0 표시) | mock 서버 미구동 또는 네트워크 차단 | mock 서버가 실행 중인지 확인하고 `reader_config.yml`의 URL이 실제 환경과 일치하는지 검토합니다. logcat에서 `MDR/TrustRefresh E` 태그로 상세 오류를 확인할 수 있습니다. |

### 7.2. Android

| 증상 | 원인 | 해결 방법 |
| :--- | :--- | :--- |
| NFC 탭이 인식되지 않음 | NFC 비활성화 또는 HCE 미지원 | 두 디바이스의 NFC를 켜고, Wallet 앱이 포그라운드에서 실행 중인지 확인합니다. |

### 7.3. iOS

| 증상 | 원인 | 해결 방법 |
| :--- | :--- | :--- |
| Signing 오류로 빌드 실패 | Apple Developer 계정 미설정 | Xcode → Signing & Capabilities에서 Team을 설정하고, Bundle Identifier를 고유한 값으로 변경합니다. |
| SPM 의존성 다운로드 실패 | 네트워크 문제 또는 캐시 오류 | Xcode → File → Packages → Reset Package Caches를 수행합니다. |
| Issuer Server에 연결 불가 | 서버 주소 설정 오류 | `APIService.swift`의 `baseURL`을 현재 네트워크 환경에 맞게 수정합니다. |

---

## 8. 관련 문서

| 문서 | 설명 |
| :--- | :--- |
| [OID4VC 설치 가이드](oid4vc_Installation_Guide_ko.md) | 백엔드 서버 구동 및 OID4VCI/OID4VP 테스트 절차 |
| [OID4VC Android 앱 README](../../source/apps/android-app/README_ko.md) | Android Wallet 앱 개요 |
| [OID4VC iOS 앱 README](../../source/apps/ios-app/README_ko.md) | iOS Wallet 앱 개요 |
| [mDoc Reader Android README](../../source/apps/android-mdoc-reader/README.md) | Android Reader 앱 개요 |
| [mDoc Reader iOS README](../../source/apps/ios-mdoc-reader/README_ko.md) | iOS Reader 앱 개요 |

---
