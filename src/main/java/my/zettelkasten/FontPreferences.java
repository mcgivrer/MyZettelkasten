package my.zettelkasten;

import java.awt.*;
import java.util.prefs.Preferences;

public class FontPreferences {
    private static final Preferences prefs = Preferences.userRoot().node("texteditor/fonts");

    public static String getFontFamily() {
        return AppConfig.get("fontFamily", "Monospaced");
    }

    public static int getFontSize() {
        return Integer.parseInt(AppConfig.get("fontSize", "14"));
    }

    public static void setFont(String family, int size) {
        AppConfig.set("fontFamily", family);
        AppConfig.set("fontSize", String.valueOf(size));
        AppConfig.save(); // ← très important
    }
    public static Font getFont() {
        return new Font(FontPreferences.getFontFamily(), Font.PLAIN, getFontSize());
    }

}
