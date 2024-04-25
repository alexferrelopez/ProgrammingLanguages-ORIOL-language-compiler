package ErrorHandlers;

public record Report<T extends MessageType>(MessageType warningTypeInteger, Integer optionalLine,
                     Integer optionalColumn, String word,
                     String message) {
}
