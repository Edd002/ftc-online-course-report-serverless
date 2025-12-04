package fiap.tech.challenge.online.course.report.serverless.kms;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class KMSUtil {

    public static String encrypt(String plainText) {
        EncryptResponse encryptResponse;
        byte[] base64EncodedValue;
        try (KmsClient kmsClient = KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(System.getenv("AWS_ID"), System.getenv("AWS_KEY")))).httpClient(ApacheHttpClient.builder().maxConnections(100).socketTimeout(Duration.ofSeconds(60)).connectionTimeout(Duration.ofSeconds(60)).build()).region(Region.US_EAST_2).build()) {
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
        try (KmsClient kmsClient = KmsClient.builder().credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(System.getenv("AWS_ID"), System.getenv("AWS_KEY")))).httpClient(ApacheHttpClient.builder().maxConnections(100).socketTimeout(Duration.ofSeconds(60)).connectionTimeout(Duration.ofSeconds(60)).build()).region(Region.US_EAST_2).build()) {
            byte[] ciphertextBlob = Base64.getDecoder().decode(encryptedText);
            DecryptRequest decryptRequest = DecryptRequest.builder().keyId(System.getenv("KMS_ARN")).ciphertextBlob(SdkBytes.fromByteArray(ciphertextBlob)).build();
            decryptResponse = kmsClient.decrypt(decryptRequest);
        } catch (Exception e) {
            System.err.println("Erro de descriptografia do texto: " + encryptedText);
            throw new RuntimeException(e);
        }
        return new String(decryptResponse.plaintext().asByteArray());
    }

    public static String oldDecrypt(String encryptedText) {
        ByteBuffer plainTextKey;
        try {
            byte[] encryptedKey = com.amazonaws.util.Base64.decode(encryptedText);
            AWSKMS client = AWSKMSClientBuilder.defaultClient();
            com.amazonaws.services.kms.model.DecryptRequest request = new com.amazonaws.services.kms.model.DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedKey));
            plainTextKey = client.decrypt(request).getPlaintext();
        } catch (Exception e) {
            System.err.println("Erro de descriptografia do texto: " + encryptedText);
            throw new RuntimeException(e);
        }
        return new String(plainTextKey.array(), StandardCharsets.UTF_8);
    }
}