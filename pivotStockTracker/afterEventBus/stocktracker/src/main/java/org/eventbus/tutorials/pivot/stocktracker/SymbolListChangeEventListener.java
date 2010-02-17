package org.eventbus.tutorials.pivot.stocktracker;

public interface SymbolListChangeEventListener {
    void symbolChanged(SymbolListChangeEvent event);
}
