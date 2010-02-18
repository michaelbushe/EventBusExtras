package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;


/**
 * Event fired when the user adds or removes a symbol from the working list
 */
public class SymbolListChangeEvent {

    public enum ChangeType {
        ADDED, REMOVED
    }

    private List<String> symbols;
    private ChangeType changeType;

    public SymbolListChangeEvent(String symbol, ChangeType changeType) {
        this.symbols = new ArrayList<String>(symbol);
        this.changeType = changeType;
    }

    public SymbolListChangeEvent(List<String> symbols, ChangeType changeType) {
        this.symbols = symbols;
        this.changeType = changeType;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

}
