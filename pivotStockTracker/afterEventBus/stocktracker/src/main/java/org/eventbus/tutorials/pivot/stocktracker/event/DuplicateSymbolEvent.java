package org.eventbus.tutorials.pivot.stocktracker.event;

/**
 * Fired when a SymbolListChangeEvent is vetoed because it would add a symbol that already exists.
 */
public class DuplicateSymbolEvent {
    private String symbol;

    public DuplicateSymbolEvent(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
