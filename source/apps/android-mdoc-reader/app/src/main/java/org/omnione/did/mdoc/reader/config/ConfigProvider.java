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
package org.omnione.did.mdoc.reader.config;

import android.content.Context;

import org.omnione.did.sdk.mdoc.proximity.reader.utility.CertificateLoader;
import org.omnione.did.sdk.mdoc.proximity.reader.datamodel.common.enums.*;
import org.omnione.did.sdk.mdoc.proximity.reader.datamodel.document.*;

import java.util.*;

public class ConfigProvider {
    private final Context context;

    public ConfigProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<ClaimItem> getDocumentClaims(AttestationType type) {
        return type.getClaims();
    }

    public List<DocumentMode> getDocumentModes(AttestationType type) {
        return Arrays.asList(DocumentMode.FULL, DocumentMode.CUSTOM);
    }

    public List<String> getCertificates() {
        return CertificateLoader.loadCertificates(context);
    }

    private java.util.Map<String, Object> readerConfig() {
        try (java.io.InputStream in = context.getAssets().open("reader_config.yml")) {
            return new org.yaml.snakeyaml.Yaml().load(in);
        } catch (Exception e) {
            android.util.Log.w("ConfigProvider", "reader_config.yml load failed", e);
            return java.util.Collections.emptyMap();
        }
    }

    public String getTrustedIssuerListUrl() {
        Object v = readerConfig().get("trustedIssuerListUrl");
        return v != null ? v.toString() : null;
    }

    public String getDidDocGatewayUrl() {
        Object v = readerConfig().get("didDocGatewayUrl");
        return v != null ? v.toString() : null;
    }
}
