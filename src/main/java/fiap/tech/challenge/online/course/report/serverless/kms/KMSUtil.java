package fiap.tech.challenge.online.course.report.serverless.kms;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
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

    public static String encrypt(String plainText) {
        EncryptResponse encryptResponse;
        byte[] base64EncodedValue;
        try (KmsClient kmsClient = KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_ACCESS_KEY_ID")))).httpClient(UrlConnectionHttpClient.builder().connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(30)).build()).region(Region.US_EAST_2).build()) {
            EncryptRequest encryptRequest = EncryptRequest.builder().keyId(System.getenv("KMS_ARN")).plaintext(SdkBytes.fromUtf8String(plainText)).build();
            encryptResponse = kmsClient.encrypt(encryptRequest);
            SdkBytes cipherTextBytes = encryptResponse.ciphertextBlob();
            base64EncodedValue = Base64.getEncoder().encode(cipherTextBytes.asByteArray());
        } catch (Exception e) {
            System.err.println("Erro de criptografia do texto: " + plainText);
            throw new RuntimeException(e);
        }
        return new String(base64EncodedValue);
    }

    public static String decrypt(String encryptedText) {
        DecryptResponse decryptResponse;
        try (KmsClient kmsClient = KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_ACCESS_KEY_ID")))).httpClient(UrlConnectionHttpClient.builder().connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(30)).build()).region(Region.US_EAST_2).build()) {
            byte[] ciphertextBlob = Base64.getDecoder().decode(encryptedText);
            DecryptRequest decryptRequest = DecryptRequest.builder().keyId(System.getenv("KMS_ARN")).ciphertextBlob(SdkBytes.fromByteArray(ciphertextBlob)).build();
            decryptResponse = kmsClient.decrypt(decryptRequest);
        } catch (Exception e) {
            System.err.println("Erro de descriptografia do texto: " + encryptedText);
            throw new RuntimeException(e);
        }
        return new String(decryptResponse.plaintext().asByteArray());
    }
}