/*
 * Copyright 2026 OmniOne. Apache-2.0. Ported from did-mso-mdoc-sdk-server MDocVerifier.decodeEcPoint.
 */
package org.omnione.did.sdk.mdoc.proximity.reader.did;

import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.ECFieldFp;
import java.security.spec.EllipticCurve;
import java.util.Base64;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

/** Decodes a base64 (std or url) EC P-256 point (compressed or uncompressed) into an ECPublicKey. */
public final class EcPointDecoder {
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private EcPointDecoder() {}

    public static ECPublicKey decode(String base64Point) throws Exception {
        byte[] pt;
        try {
            pt = Base64.getDecoder().decode(base64Point.trim());
        } catch (IllegalArgumentException e) {
            String t = base64Point.trim();
            int pad = (4 - (t.length() % 4)) % 4;
            // "=".repeat(int) is API 33+; substring of a 3-'=' literal is minSdk-26 safe (pad is 0..3).
            pt = Base64.getUrlDecoder().decode(t + "===".substring(0, pad));
        }
        ECNamedCurveParameterSpec bc = ECNamedCurveTable.getParameterSpec("secp256r1");
        org.bouncycastle.math.ec.ECPoint q = bc.getCurve().decodePoint(pt).normalize();
        ECParameterSpec ecSpec = jceSpec(bc);
        ECPoint jcePoint = new ECPoint(
            q.getAffineXCoord().toBigInteger(), q.getAffineYCoord().toBigInteger());
        // Android 시스템 "BC" provider는 EC KeyFactory를 지원하지 않는다(API 28+에서 제거).
        // device key 경로(DeviceResponseParser: KeyFactory.getInstance("EC"))와 동일하게 기본
        // provider를 쓴다. 압축점 해제는 위의 BC ECCurve.decodePoint(클래스 직접 호출, provider
        // 무관)로 이미 끝났고, ecSpec/jcePoint는 표준 JCE 타입이라 기본 provider로 키 생성이 된다.
        return (ECPublicKey) KeyFactory.getInstance("EC")
            .generatePublic(new ECPublicKeySpec(jcePoint, ecSpec));
    }

    private static ECParameterSpec jceSpec(ECNamedCurveParameterSpec bcSpec) {
        return new ECParameterSpec(
            new EllipticCurve(
                new ECFieldFp(bcSpec.getCurve().getField().getCharacteristic()),
                bcSpec.getCurve().getA().toBigInteger(),
                bcSpec.getCurve().getB().toBigInteger()),
            new ECPoint(
                bcSpec.getG().getAffineXCoord().toBigInteger(),
                bcSpec.getG().getAffineYCoord().toBigInteger()),
            bcSpec.getN(),
            bcSpec.getH().intValue());
    }
}
