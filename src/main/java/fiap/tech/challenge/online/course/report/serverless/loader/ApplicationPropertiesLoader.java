package fiap.tech.challenge.online.course.report.serverless.loader;

import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Base64;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationPropertiesLoader {

    public static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = ApplicationPropertiesLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo properties de propriedade do sistema: " + e.getMessage());
        }
        Pattern pattern = Pattern.compile("\\$\\{(\\w+)}");
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            Matcher matcher = pattern.matcher(value);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String envVarName =  matcher.group(1);
                String envVarValue = System.getenv(envVarName) != null ? decryptKey(System.getenv(envVarName)) : Dotenv.load().get(envVarName);
                if (envVarValue != null) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(envVarValue));
                } else {
                    System.err.println("Variável de ambiente " + envVarName + " não encontrada para a propriedade " + key + ".");
                    matcher.appendReplacement(sb, "");
                }
            }
            matcher.appendTail(sb);
            properties.setProperty(key, sb.toString());
        }
        return properties;
    }

    private static String decryptKey(String envVarValue) {
        try {
            ByteBuffer cipherTextBlob = ByteBuffer.wrap(Base64.getDecoder().decode(envVarValue));
            DecryptRequest decryptRequest = new DecryptRequest().withCiphertextBlob(cipherTextBlob);
            ByteBuffer plaintextBuffer = AWSKMSClientBuilder.standard().build().decrypt(decryptRequest).getPlaintext();
            byte[] plaintextBytes = new byte[plaintextBuffer.remaining()];
            plaintextBuffer.get(plaintextBytes);
            String decryptedPlaintext = new String(plaintextBytes);
            System.out.println("Decrypted plaintext: " + decryptedPlaintext);
            return decryptedPlaintext;
        } catch (Exception e) {
            System.out.println("Error during decryption: " + e.getMessage());
            throw new RuntimeException("KMS Decryption failed", e);
        }
    }

    private static String decryptAWSEnvironmentKey(String envVarValue) {
        DecryptResponse decryptResponse;
        try (KmsClient kmsClient = KmsClient.builder().httpClient(UrlConnectionHttpClient.builder().connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(30)).build()).region(Region.US_EAST_2).build()) {
            byte[] ciphertextBlob = Base64.getDecoder().decode(envVarValue);
            software.amazon.awssdk.services.kms.model.DecryptRequest decryptRequest = software.amazon.awssdk.services.kms.model.DecryptRequest.builder().ciphertextBlob(SdkBytes.fromByteArray(ciphertextBlob)).build();
            decryptResponse = kmsClient.decrypt(decryptRequest);
        }
        return new String(decryptResponse.plaintext().asByteArray());
    }
}