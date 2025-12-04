package fiap.tech.challenge.online.course.report.serverless.kms;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;

import java.time.Duration;
import java.util.Base64;

public class KMSUtil {

    public static String encryptAWSEnvironmentKey(String plainText) {
        EncryptResponse encryptResponse;
        byte[] base64EncodedValue;
        try (KmsClient kmsClient = KmsClient.builder().httpClient(UrlConnectionHttpClient.builder().connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(30)).build()).region(Region.US_EAST_2).build()) {
            EncryptRequest encryptRequest = EncryptRequest.builder().plaintext(SdkBytes.fromUtf8String(plainText)).build();
            encryptResponse = kmsClient.encrypt(encryptRequest);
            SdkBytes cipherTextBytes = encryptResponse.ciphertextBlob();
            base64EncodedValue = Base64.getEncoder().encode(cipherTextBytes.asByteArray());
        }
        return new String(base64EncodedValue);
    }

    public static String decryptAWSEnvironmentKey(String envVarValue) {
        DecryptResponse decryptResponse;
        try (KmsClient kmsClient = KmsClient.builder().httpClient(UrlConnectionHttpClient.builder().connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(30)).build()).region(Region.US_EAST_2).build()) {
            byte[] ciphertextBlob = Base64.getDecoder().decode(envVarValue);
            DecryptRequest decryptRequest = DecryptRequest.builder().ciphertextBlob(SdkBytes.fromByteArray(ciphertextBlob)).build();
            decryptResponse = kmsClient.decrypt(decryptRequest);
        }
        return new String(decryptResponse.plaintext().asByteArray());
    }
}