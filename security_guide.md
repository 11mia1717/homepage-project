## 서로 다른 OS 간 통신 보안 가이드: 취약점 및 HTTPS 적용 방안

Windows Server (백엔드)와 Ubuntu (프론트엔드) 간의 통신은 다양한 보안 취약점에 노출될 수 있습니다. 이 가이드는 주요 취약점을 이해하고, 이를 방어하기 위한 HTTPS 적용 방안을 설명합니다.

### 1. 서로 다른 OS 간 통신 시 발생할 수 있는 보안 취약점

1.  **데이터 가로채기 (Eavesdropping):**
    암호화되지 않은 HTTP 통신을 사용할 경우, 네트워크를 통해 전송되는 민감한 데이터 (사용자 정보, 금융 정보 등)가 중간에서 쉽게 가로채어질 수 있습니다. 공격자는 패킷 스니퍼(Packet Sniffer) 도구를 사용하여 데이터를 엿볼 수 있습니다.

2.  **데이터 변조 (Tampering):**
    공격자가 통신 흐름 중간에 개입하여 전송되는 데이터를 변경할 수 있습니다. 예를 들어, 프론트엔드에서 백엔드로 보내는 요청의 값을 변경하거나, 백엔드에서 프론트엔드로 보내는 응답을 조작하여 웹사이트의 동작을 왜곡시킬 수 있습니다.

3.  **중간자 공격 (Man-in-the-Middle, MITM):**
    공격자가 클라이언트와 서버 사이에 끼어들어 양쪽 모두에게 자신을 정당한 통신 상대로 속여 통신을 가로채는 공격입니다. 이 경우 공격자는 데이터를 가로채거나 변조하는 것이 가능해지며, 통신 당사자들은 자신들이 공격자와 통신하고 있다는 사실을 인지하기 어렵습니다.

4.  **권한 없는 접근 (Unauthorized Access):**
    적절한 인증 및 권한 부여 메커니즘이 없거나 약할 경우, 권한 없는 사용자가 시스템 또는 데이터에 접근할 수 있습니다. 이는 API 엔드포인트에 대한 무단 접근 시도 등으로 나타날 수 있습니다.

5.  **CORS (Cross-Origin Resource Sharing) 문제:**
    서로 다른 도메인, 프로토콜, 포트에서 리소스를 요청할 때 발생하는 보안 메커니즘입니다. 올바르게 설정되지 않으면 브라우저 보안 정책에 의해 프론트엔드에서 백엔드 API 호출이 차단될 수 있습니다. (이전에 Spring Boot의 `WebMvcConfigurer`를 통해 Ubuntu IP를 허용하여 이 문제를 해결했습니다.)

### 2. 보안 취약점 방어를 위한 HTTPS 적용 방안

HTTPS (Hypertext Transfer Protocol Secure)는 HTTP 프로토콜에 SSL/TLS 암호화 계층을 추가하여, 위에서 언급된 대부분의 통신 보안 취약점을 효과적으로 방어할 수 있습니다.

#### 2.1. HTTPS의 주요 이점

*   **데이터 암호화:** 클라이언트와 서버 간에 전송되는 모든 데이터를 암호화하여 데이터 가로채기 및 변조를 방지합니다.
*   **데이터 무결성:** 전송 중 데이터가 변조되지 않았음을 보장합니다.
*   **인증:** 서버의 신원을 확인하여 클라이언트가 올바른 서버와 통신하고 있음을 보장하고, 중간자 공격을 방지합니다.

#### 2.2. HTTPS 적용 단계

1.  **SSL/TLS 인증서 획득:**
    HTTPS를 적용하려면 신뢰할 수 있는 기관(CA)에서 발급한 SSL/TLS 인증서가 필요합니다. 다양한 방법으로 인증서를 획득할 수 있습니다.
    *   **Let's Encrypt (권장):** 무료로 인증서를 발급해주는 서비스입니다. `Certbot`과 같은 도구를 사용하여 쉽게 설치하고 자동으로 갱신할 수 있어 가장 널리 사용됩니다. 도메인 소유권을 확인하는 방식으로 인증서를 발급합니다.
    *   **유료 CA:** Comodo, DigiCert 등 상업용 CA에서 유료로 인증서를 구매할 수 있습니다. 추가적인 기능 (예: 조직 유효성 검사)을 제공합니다.
    *   **자체 서명 인증서:** 개발/테스트 목적으로는 자체 서명 인증서를 사용할 수 있지만, 브라우저에서 신뢰하지 않으므로 운영 환경에서는 적합하지 않습니다.

2.  **Nginx에 HTTPS 설정 (Ubuntu 프론트엔드 서버):
    Ubuntu 서버의 Nginx에서 HTTPS를 설정하여 프론트엔드로의 모든 트래픽을 암호화합니다. 이는 백엔드와의 통신을 포함합니다.

    *   **Nginx 설정 파일 수정 (`/etc/nginx/sites-available/your_domain.conf` 또는 `nginx-hybrid.conf` 복사본 수정):**
        기존 `listen 80;` 외에 `listen 443 ssl;`을 추가하고, SSL 인증서 및 키 파일의 경로를 지정합니다.
        ```nginx
        server {
            listen 80;
            listen 443 ssl;
            server_name your_domain.com www.your_domain.com;

            # HTTP를 HTTPS로 리다이렉션 (선택 사항이지만 권장)
            if ($scheme = http) {
                return 301 https://$server_name$request_uri;
            }

            ssl_certificate /etc/letsencrypt/live/your_domain.com/fullchain.pem;
            ssl_certificate_key /etc/letsencrypt/live/your_domain.com/privkey.pem;
            ssl_protocols TLSv1.2 TLSv1.3;
            ssl_ciphers 'TLS_AES_128_GCM_SHA256:TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES128-GCM-SHA256';
            ssl_prefer_server_ciphers off;

            # ... 기존 Nginx 설정 (root, index, location /api/ 등) ...
        }
        ```

    *   **Nginx 재시작:** 설정 변경 후 Nginx를 재시작합니다: `sudo systemctl restart nginx`

3.  **Spring Boot 백엔드에 HTTPS 적용 (선택 사항 또는 Nginx 프록시 뒤에):
    Nginx가 프론트엔드에서 백엔드로의 통신을 프록시하고 있다면, Nginx와 클라이언트 간에만 HTTPS를 적용하고 백엔드 (Spring Boot)는 HTTP (8080)로 통신해도 일반적으로 안전합니다 (내부 네트워크 통신이 신뢰할 수 있는 경우). 그러나 더 높은 보안 수준을 원한다면 Spring Boot 애플리케이션 자체에도 HTTPS를 적용할 수 있습니다. 이는 `application.properties` 파일에 SSL 설정을 추가하여 수행할 수 있습니다.

    ```properties
    server.port=8443 # HTTPS 포트
    server.ssl.key-store=classpath:keystore.p12
    server.ssl.key-store-password=password
    server.ssl.key-store-type=PKCS12
    server.ssl.key-alias=springboot
    ```

    이 경우 Nginx의 `proxy_pass` 설정도 `https://192.168.59.1:8443` 등으로 변경해야 합니다.

HTTPS를 적용함으로써 데이터의 기밀성, 무결성, 그리고 서버의 신뢰성을 확보하여 금융사 수탁사 사이트로서의 보안 요구사항을 충족할 수 있습니다.
