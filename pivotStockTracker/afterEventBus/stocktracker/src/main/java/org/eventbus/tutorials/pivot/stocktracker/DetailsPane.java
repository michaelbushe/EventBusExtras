package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.wtk.*;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;

/**
 * Component for display a stocks pricing details
 */
public class DetailsPane extends BoxPane implements Bindable {
    @WTKX private Label changeLabel;

    public DetailsPane() {
        AnnotationProcessor.process(this);
    }

    @EventSubscriber
    public void showStockQuoteDetails(StockQuoteSelection selection) {
        StockQuote stockQuote = selection.getStockQuotes().get(0);
        StockQuoteView stockQuoteView = new StockQuoteView(stockQuote);
        load(stockQuoteView);

        float change = stockQuote.getChange();
        if (!Float.isNaN(change)
            && change < 0) {
            Form.setFlag(changeLabel, new Form.Flag(MessageType.ERROR));
        } else {
            Form.setFlag(changeLabel, (Form.Flag)null);
        }
    }

    @Override
    public void initialize() {
    }
}
