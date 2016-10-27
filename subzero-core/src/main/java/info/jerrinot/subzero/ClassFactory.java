package info.jerrinot.subzero;

/**
 * Factory for creating domain classes
 *
 * This is useful when a domain class cannot be instantiated directly,
 * but a factory method has to be used.
 *
 */
public interface ClassFactory {
    Class createClass();
}
