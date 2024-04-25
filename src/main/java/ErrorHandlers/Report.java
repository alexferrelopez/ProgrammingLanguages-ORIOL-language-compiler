package ErrorHandlers;

public record Report<T extends MessageType>(T warningTypeInteger, Integer optionalLine,
                     Integer optionalColumn, String word,
                     String message) {
}
