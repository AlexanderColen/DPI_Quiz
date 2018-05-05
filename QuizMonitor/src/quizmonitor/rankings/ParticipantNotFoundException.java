package quizmonitor.rankings;

/**
 *
 * @author Alexander
 */
public class ParticipantNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>ParticipantNotFoundException</code>
     * without detail message.
     */
    public ParticipantNotFoundException() {
        super("Participant not found within the rankings.");
    }

    /**
     * Constructs an instance of <code>ParticipantNotFoundException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public ParticipantNotFoundException(String msg) {
        super(msg);
    }
}