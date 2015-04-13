package exceptions;

public class DAOException extends Exception {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DAOException() {
        super();
    }

    public DAOException(String message) {
        super(message);
    }
    
    public DAOException(Throwable e) {
        super(e);
    }

}
