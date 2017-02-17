package edu.umn.amicus;

/**
 * todo: flesh this out a little more (and document)
 *
 * Created by gpfinley on 12/14/16.
 */
public class AmicusException extends RuntimeException {

    public AmicusException() {
        super();
    }

    public AmicusException(String message) {
        super(message);
    }

    public AmicusException(String message, String... vars) {
        super(String.format(message, vars));
    }

    // todo: test and possibly develop more
    public AmicusException(Throwable e) {
        super(e);
        e.printStackTrace();
    }
}