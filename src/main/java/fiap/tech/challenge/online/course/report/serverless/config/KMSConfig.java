package fiap.tech.challenge.online.course.report.serverless.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;

import java.time.Duration;
import java.util.Base64;

public class KMSConfig {

    public String encrypt(String plainText) {
        EncryptResponse encryptResponse;
        byte[] base64EncodedValue;
        try (KmsClient kmsClient = KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_SECRET_ACCESS_KEY")))).httpClient(ApacheHttpClient.builder().maxConnections(100).socketTimeout(Duration.ofSeconds(60)).connectionTimeout(Duration.ofSeconds(60)).build()).region(Region.US_EAST_2).build()) {
            EncryptRequest encryptRequest = EncryptRequest.builder().keyId("arn:aws:kms:us-east-2:045221533960:key/0b721003-b45e-4ebb-a5a5-973c1c386a87").plaintext(SdkBytes.fromUtf8String(plainText)).build();
            encryptResponse = kmsClient.encrypt(encryptRequest);
            SdkBytes cipherTextBytes = encryptResponse.ciphertextBlob();
            base64EncodedValue = Base64.getEncoder().encode(cipherTextBytes.asByteArray());
        } catch (Exception e) {
            System.err.println("Erro de criptografia do texto: " + plainText);
            throw new RuntimeException(e);
        }
        return new String(base64EncodedValue);
    }

    public String decrypt(String encryptedText) {
        DecryptResponse decryptResponse;
        try (KmsClient kmsClient = KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_SECRET_ACCESS_KEY")))).httpClient(ApacheHttpClient.builder().maxConnections(100).socketTimeout(Duration.ofSeconds(60)).connectionTimeout(Duration.ofSeconds(60)).build()).region(Region.US_EAST_2).build()) {
            byte[] ciphertextBlob = Base64.getDecoder().decode(encryptedText);
            DecryptRequest decryptRequest = DecryptRequest.builder().keyId("arn:aws:kms:us-east-2:045221533960:key/0b721003-b45e-4ebb-a5a5-973c1c386a87").ciphertextBlob(SdkBytes.fromByteArray(ciphertextBlob)).build();
            decryptResponse = kmsClient.decrypt(decryptRequest);
        } catch (Exception e) {
            System.err.println("Erro de descriptografia do texto: " + encryptedText);
            throw new RuntimeException(e);
        }
        return new String(decryptResponse.plaintext().asByteArray());
    }
}