package org.eventbus.tutorials.pivot.stocktracker.event;

import org.apache.pivot.collections.List;
import org.eventbus.tutorials.pivot.stocktracker.StockQuote;

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
