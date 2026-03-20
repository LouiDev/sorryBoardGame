package exceptions;

public class InvalidTreeStructureException extends RuntimeException {
    public InvalidTreeStructureException(String message) {
        super("Ungültige Baumstruktur: " + message);
    }
}
