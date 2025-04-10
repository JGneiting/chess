package websocket.messages;

public class ErrorMessage extends ServerMessage {

    private final String error;

    public ErrorMessage(String error) {
        super(ServerMessageType.ERROR);
        this.error = error;
    }
}
