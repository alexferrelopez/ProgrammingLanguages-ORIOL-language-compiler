package errorHandlers;

public record Report<T extends MessageType>(T messageType, Integer optionalLine,
                                            Integer optionalColumn, String word,
                                            String message) {

    @Override
    public String toString() {
        return message;
    }
}