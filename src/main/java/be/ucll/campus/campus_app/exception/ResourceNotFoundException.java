package be.ucll.campus.campus_app.exception;
//Spring heeft geen standaard ResourceNotFoundException, dus die maken we zelf. (error 404)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
