package org.eventbus.tutorials.pivot.stocktracker;

/**
 *
 */
public class SymbolListChangeEvent {

    public enum ChangeType {
        ADDED, REMOVED
    }

    private String symbol;
    private ChangeType changeType;

    public SymbolListChangeEvent(String symbol, ChangeType changeType) {
        this.symbol = symbol;
        this.changeType = changeType;
    }

    
    public String getSymbol() {
        return symbol;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

}
