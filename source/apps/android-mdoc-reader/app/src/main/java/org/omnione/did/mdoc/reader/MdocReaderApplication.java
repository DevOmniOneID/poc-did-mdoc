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
package org.omnione.did.mdoc.reader;

import android.app.Application;

import org.omnione.did.mdoc.reader.config.ConfigProvider;
import org.omnione.did.mdoc.reader.di.DependencyProvider;
import org.omnione.did.mdoc.reader.settings.PreferencesManager;
import org.omnione.did.sdk.mdoc.proximity.reader.core.PlatformController;
import org.omnione.did.sdk.mdoc.proximity.reader.core.TransferController;
import org.omnione.did.sdk.mdoc.proximity.reader.utility.ProtocolLogger;

public class MdocReaderApplication extends Application implements DependencyProvider {
    private static MdocReaderApplication instance;

    private ConfigProvider configProvider;
    private PreferencesManager preferencesManager;
    private PlatformController platformController;
    private TransferController transferController;
    private org.omnione.did.mdoc.reader.trust.TrustedIssuerStore trustedIssuerStore;

    public static org.omnione.did.mdoc.reader.trust.TrustedIssuerStore getTrustedIssuerStore() {
        return instance.trustedIssuerStore;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        String versionName = "1.0.0";
        try {
            versionName = getPackageManager()
                .getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {}

        configProvider = new ConfigProvider(this);
        preferencesManager = new PreferencesManager(this);
        platformController = new PlatformController(versionName, "debug", "public");
        transferController = new TransferController(this);

        ProtocolLogger.setCaptureMode(preferencesManager.isCaptureIssuerAuth());

        // 신뢰 발급자 캐시: store 생성 → SDK 주입(키 공급 + allowlist)
        trustedIssuerStore = new org.omnione.did.mdoc.reader.trust.TrustedIssuerStore(this);
        org.omnione.did.sdk.mdoc.proximity.reader.did.DidIssuerKeyResolver
            .setSupplier(trustedIssuerStore);

        // 부팅 시 1회 최신화 (백그라운드, 기회적)
        new Thread(() -> {
            String listUrl = configProvider.getTrustedIssuerListUrl();
            String gwUrl = configProvider.getDidDocGatewayUrl();
            if (listUrl == null || gwUrl == null) return;
            new org.omnione.did.mdoc.reader.trust.TrustListRefresher(
                new org.omnione.did.mdoc.reader.trust.IssuerListClient(listUrl),
                new org.omnione.did.mdoc.reader.trust.DidDocClient(gwUrl),
                trustedIssuerStore).refresh();
        }, "trust-refresh-boot").start();
    }

    public static MdocReaderApplication getInstance() {
        return instance;
    }

    public static ConfigProvider getConfigProvider() {
        return instance.configProvider;
    }

    public static PreferencesManager getPreferencesManager() {
        return instance.preferencesManager;
    }

    public static PlatformController getPlatformController() {
        return instance.platformController;
    }

    public static TransferController getTransferController() {
        return instance.transferController;
    }

    // DependencyProvider implementation
    @Override public ConfigProvider configProvider() { return configProvider; }
    @Override public PreferencesManager preferencesManager() { return preferencesManager; }
    @Override public PlatformController platformController() { return platformController; }
    @Override public TransferController transferController() { return transferController; }
}
