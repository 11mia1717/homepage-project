## 네트워크 연결 가이드: Windows <-> Ubuntu

이 가이드는 Windows Server (백엔드)와 Ubuntu (프론트엔드) 간의 네트워크 연결을 설정하고 확인하는 방법을 설명합니다. 특히 Windows 방화벽에서 Spring Boot 애플리케이션의 포트를 개방하고, 두 서버 간의 통신을 테스트하는 데 중점을 둡니다.

### 1. Windows 방화벽 8080 포트 개방 (Spring Boot 백엔드)

Spring Boot 애플리케이션은 기본적으로 8080 포트를 사용합니다. Ubuntu 프론트엔드에서 Windows 백엔드로 API 요청을 보내려면 Windows Server의 방화벽에서 8080 포트에 대한 인바운드 연결을 허용해야 합니다.

#### 1.1. 제어판을 통한 설정

1.  **Windows Server에서:** `제어판` -> `시스템 및 보안` -> `Windows Defender 방화벽` -> `고급 설정`을 엽니다.
2.  좌측 패널에서 `인바운드 규칙`을 클릭합니다.
3.  우측 패널에서 `새 규칙...`을 클릭합니다.
4.  `규칙 종류`로 `포트(Port)`를 선택하고 `다음`을 클릭합니다.
5.  `프로토콜 및 포트`에서 `TCP`를 선택하고 `특정 로컬 포트`에 `8080`을 입력한 후 `다음`을 클릭합니다.
6.  `작업`으로 `연결 허용(Allow the connection)`을 선택하고 `다음`을 클릭합니다.
7.  `프로필`에서 `도메인(Domain)`, `개인(Private)`, `공용(Public)`을 모두 선택하거나, 서버 환경에 맞게 필요한 프로필만 선택하고 `다음`을 클릭합니다.
8.  `이름`에 `Spring Boot 8080 Port`와 같이 식별하기 쉬운 이름을 입력하고 `마침`을 클릭합니다.

#### 1.2. PowerShell을 통한 설정

관리자 권한으로 PowerShell을 열고 다음 명령어를 실행합니다.

```powershell
New-NetFirewallRule -DisplayName "Spring Boot 8080 Port" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow -Enabled True
```

### 2. Ubuntu에서 Windows로 Ping 연결 확인

Windows 방화벽 설정이 완료되면, Ubuntu 서버에서 Windows Server로 네트워크 연결이 가능한지 `ping` 명령어를 사용하여 확인합니다.

*   **Windows Server IP 주소:** `192.168.59.1`
*   **Ubuntu VM IP 주소:** `192.168.59.138`

1.  **Ubuntu Server에서 터미널을 엽니다.**

2.  **Ping 테스트 실행:**
    다음 명령어를 사용하여 Windows Server IP 주소로 ping을 보냅니다.
    ```bash
    ping 192.168.59.1
    ```

3.  **결과 확인:**
    *   `64 bytes from 192.168.59.1: icmp_seq=1 ttl=128 time=X.X ms` 와 같은 응답이 계속 나타나면 연결이 성공한 것입니다.
    *   `Destination Host Unreachable` 또는 `Request timed out` 과 같은 메시지가 나타나면 연결에 문제가 있는 것입니다.

### 3. 연결 문제 발생 시 체크리스트 (IP 연결 오류 방지)

*   **IP 주소 정확성 확인:** Windows Server와 Ubuntu VM의 IP 주소가 정확한지 다시 한번 확인합니다.
*   **네트워크 설정:** 두 서버가 동일한 네트워크 서브넷에 있는지 확인합니다. VM 환경이라면 네트워크 어댑터 설정 (NAT, Bridge 등)을 점검합니다.
*   **방화벽 (Windows):** 8080 포트 외에 다른 필수 포트 (예: ICMP Echo Request에 대한 응답을 허용하려면 '파일 및 프린터 공유 (에코 요청 - ICMPv4 인바운드)' 규칙 활성화)가 차단되어 있는지 확인합니다. `ping` 테스트가 실패한다면 Windows 방화벽에서 ICMP를 차단하고 있을 수 있습니다.
*   **방화벽 (Ubuntu):** Ubuntu에 `ufw` (Uncomplicated Firewall)와 같은 방화벽이 활성화되어 있고, Windows Server로의 아웃바운드 연결이 차단되어 있지는 않은지 확인합니다.
*   **Spring Boot 애플리케이션 실행 여부:** Windows Server에서 Spring Boot 애플리케이션이 8080 포트에서 실제로 실행 중인지 확인합니다. (예: `netstat -ano | findstr :8080` 명령어를 PowerShell에서 실행하여 8080 포트를 사용하는 프로세스 확인)
*   **라우팅 문제:** 복잡한 네트워크 환경에서는 라우팅 테이블을 확인해야 할 수도 있습니다.

이 가이드를 통해 Windows 백엔드와 Ubuntu 프론트엔드 간의 기본적인 네트워크 연결을 설정하고 문제 해결하는 데 도움이 될 것입니다.
