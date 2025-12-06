package mas.curs.infsys.models;

public enum BookStatus {
    SOON("Скоро"),
    RELEASED("Релиз");

    private final String displayName;

    BookStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

