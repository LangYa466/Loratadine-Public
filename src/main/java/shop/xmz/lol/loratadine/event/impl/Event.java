package shop.xmz.lol.loratadine.event.impl;

/**
 * Marker interface for events.
 */
public interface Event {
    // This interface doesn't contain any methods or fields.
    // It's used to mark classes as events.
    enum Side {
        PRE,
        POST;
    }
}