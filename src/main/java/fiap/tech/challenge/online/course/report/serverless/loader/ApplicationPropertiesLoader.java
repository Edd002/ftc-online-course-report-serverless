package fiap.tech.challenge.online.course.report.serverless.loader;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.util.Base64;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
                String envVarValue = System.getenv(envVarName) != null ? decryptAWSEnvironmentKey(envVarName) : Dotenv.load().get(envVarName);
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

    private static String decryptAWSEnvironmentKey(String envVarName) {
        byte[] encryptedKey = Base64.decode(System.getenv(envVarName));
        Map<String, String> encryptionContext = new HashMap<>();
        encryptionContext.put("LambdaFunctionName", System.getenv("AWS_LAMBDA_FUNCTION_NAME"));
        AWSKMS client = AWSKMSClientBuilder.defaultClient();
        DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedKey)).withEncryptionContext(encryptionContext);
        ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();
        return new String(plainTextKey.array(), StandardCharsets.UTF_8);
    }
}