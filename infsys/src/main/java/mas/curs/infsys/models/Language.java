package mas.curs.infsys.models;

public enum Language {
    RUSSIAN("Русский"),
    ENGLISH("English"),
    GERMAN("Deutsch"),
    FRENCH("Français"),
    SPANISH("Español"),
    ITALIAN("Italiano"),
    CHINESE("中文"),
    JAPANESE("日本語"),
    KOREAN("한국어"),
    OTHER("Другое");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}


