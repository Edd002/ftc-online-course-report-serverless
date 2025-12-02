package fiap.tech.challenge.online.course.report.serverless.loader;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;

import java.io.IOException;
import java.io.InputStream;
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
                String envVarValue = System.getenv(envVarName) != null ? decrypt(System.getenv(envVarName)) : Dotenv.load().get(envVarName);
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

    private static String decrypt(String envVarValue) {
        System.out.println("ApplicationPropertiesLoader::decrypt: START");
        String decryptTest;
        try (KmsClient kmsClient = KmsClient.builder().httpClient(ApacheHttpClient.builder().build()).region(Region.US_EAST_2).build()) {
            DecryptRequest decryptRequest = buildDecryptRequest(envVarValue);
            DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
            decryptTest = decryptResponse.plaintext().asUtf8String();
            System.out.println("ApplicationPropertiesLoader::decrypt: END");
        }
        return decryptTest;
    }

    private static DecryptRequest buildDecryptRequest(String base64EncodedValue) {
        System.out.println("ApplicationPropertiesLoader::buildDecryptRequest: START");
        SdkBytes encryptBytes = SdkBytes.fromByteArray(Base64.getDecoder().decode(base64EncodedValue));
        DecryptRequest decryptRequest = DecryptRequest.builder().keyId("arn:aws:kms:us-east-2:045221533960:key/0b721003-b45e-4ebb-a5a5-973c1c386a87").ciphertextBlob(encryptBytes).build();
        System.out.println("ApplicationPropertiesLoader::buildDecryptRequest: END");
        return decryptRequest;
    }

    private static String decryptAWSEnvironmentKey(String envVarValue) {
        DecryptResponse decryptResponse;
        try (KmsClient kmsClient = KmsClient.builder().httpClient(UrlConnectionHttpClient.builder().connectionTimeout(Duration.ofSeconds(5)).socketTimeout(Duration.ofSeconds(30)).build()).region(Region.US_EAST_2).build()) {
            byte[] ciphertextBlob = Base64.getDecoder().decode(envVarValue);
            DecryptRequest decryptRequest = DecryptRequest.builder().ciphertextBlob(SdkBytes.fromByteArray(ciphertextBlob)).build();
            decryptResponse = kmsClient.decrypt(decryptRequest);
        }
        return new String(decryptResponse.plaintext().asByteArray());
    }
}