package be.ucll.campus.campus_app.exception;
//Spring heeft geen standaard DuplicateResourceException, dus die maken we zelf. (409 conflict)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
