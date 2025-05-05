package my.zettelkasten;

import java.io.*;
import java.util.Properties;

public class AppConfig {
    private static final File CONFIG_FILE = new File("myzettelkasten.properties");
    private static final Properties props = new Properties();

    static {
        if (CONFIG_FILE.exists()) {
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Erreur de lecture du fichier de configuration : " + e.getMessage());
            }
        }
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public static void set(String key, String value) {
        props.setProperty(key, value);
    }

    public static void save() {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "MyZettelkasten configuration");
        } catch (IOException e) {
            System.err.println("Erreur d’écriture dans le fichier de configuration : " + e.getMessage());
        }
    }
}
