package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.collections.List;

/**
 * Event published when the user's selection changes
 */
public class StockQuoteSelection {
    List<StockQuote> stockQuotes;

    public StockQuoteSelection(List<StockQuote> stockQuotes) {
        this.stockQuotes = stockQuotes;
    }

    public List<StockQuote> getStockQuotes() {
        return stockQuotes;
    }
}
