# DID-native mDoc PoC

Welcome to the DID-native mDoc PoC repository.
This repository contains a Proof of Concept (PoC) project for testing the DID-native mDoc in integration with OpenDID.

## Key Features

- OID4VCI / OID4VP credential issuance and verification
- ISO 18013-5 mDoc proximity presentation (BLE/NFC)
- DID-native trusted issuer verification for mDoc Reader — verifies IssuerAuth using a DID-resolved public key instead of X.509 x5chain
- Trusted issuer cache (`trusted_issuers.json`) with online refresh from a configurable mock server and API-gateway
- EUDI Wallet interoperability (SD-JWT VC PID, mDL)

## Repository Layout

```
source/
  servers/issuer-server         # Spring Boot OID4VCI issuer
  servers/verifier-server       # Spring Boot OID4VP verifier
  sdks/poc-mso-mdoc-sdk-aos     # Android ISO 18013-5/-7 mDoc SDK
  sdks/poc-sd-jwt-vc-sdk-aos    # Android SD-JWT VC SDK
  apps/android-app              # Android wallet sample
  apps/ios-app                  # iOS wallet sample
  apps/android-mdoc-reader      # Android mDoc proximity verifier. Includes DID-native trusted issuer cache with online refresh.
  apps/ios-mdoc-reader          # iOS mDoc proximity verifier
```

## Documentation

- [Installation Guide (EN)](docs/installation/oid4vc_Installation_Guide.md)
- [Offline Presentation Guide (EN)](docs/installation/mdoc_Offline_Presentation_Guide.md)
- [MSO mDoc SDK API (EN)](docs/api/poc-mso-mdoc-sdk-aos/MsoMdoc_SDK_API.md)

## Contributing

For detailed information on contribution procedures and the code of conduct, please refer to [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

[Apache 2.0](LICENSE)
