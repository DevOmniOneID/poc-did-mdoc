/*
 * Copyright 2026 OmniOne.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Mock trusted-issuer list server (테스트용).
 *
 * <p>{@code GET /trusted-issuers -> {"dids": [...]}} 만 응답하는 가짜 서버.
 * mDoc Reader 의 신뢰 발급자 캐시 최신화 흐름에서 "신뢰 did 목록"을 제공한다.
 * 빌드/의존성 없이 JDK 21 single-file 실행으로 바로 구동:
 *
 * <pre>
 *   java MockIssuerListServer.java          # 0.0.0.0:9090 바인딩
 *   java MockIssuerListServer.java 9091     # 포트 변경
 * </pre>
 *
 * reader_config.yml 의 {@code trustedIssuerListUrl} 과 포트를 맞춰 사용한다.
 */
public final class MockIssuerListServer {

    /** 신뢰 발급자 did 목록 (필요 시 추가). */
    private static final String[] DIDS = {"did:omn:issuer"};

    private static final int DEFAULT_PORT = 9090;

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/trusted-issuers", exchange -> {
            byte[] body = trustedIssuersJson().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("Mock trusted-issuer list server: http://0.0.0.0:" + port + "/trusted-issuers");
    }

    /** {"dids": ["did:omn:issuer", ...]} JSON 직렬화. */
    private static String trustedIssuersJson() {
        StringBuilder sb = new StringBuilder("{\"dids\":[");
        for (int i = 0; i < DIDS.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(DIDS[i]).append('"');
        }
        return sb.append("]}").toString();
    }

    private MockIssuerListServer() {}
}
