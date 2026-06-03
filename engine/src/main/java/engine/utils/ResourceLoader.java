package engine.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceLoader {

    private ResourceLoader() {}

    public static String loadString(String classpathPath) {
        try (InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) {
                throw new RuntimeException("Recurso nao encontrado: " + classpathPath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao carregar recurso: " + classpathPath, e);
        }
    }
}
