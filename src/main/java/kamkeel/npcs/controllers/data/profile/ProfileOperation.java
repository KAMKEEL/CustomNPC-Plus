package kamkeel.npcs.controllers.data.profile;

public class ProfileOperation {
    private final EnumProfileOperation result;
    private final String message;

    public ProfileOperation(EnumProfileOperation result, String message) {
        this.result = result;
        this.message = message;
    }

    public EnumProfileOperation getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public static ProfileOperation success(String message) {
        return new ProfileOperation(EnumProfileOperation.SUCCESS, message);
    }

    public static ProfileOperation error(String message) {
        return new ProfileOperation(EnumProfileOperation.ERROR, message);
    }

    public static ProfileOperation locked(String message) {
        return new ProfileOperation(EnumProfileOperation.LOCKED, message);
    }
}
