package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventTopicSubscriber;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.eventbus.tutorials.pivot.stocktracker.event.EventConstants;
import org.eventbus.tutorials.pivot.stocktracker.event.StockQuoteSelection;

/**
 * Component for display a stocks pricing details
 */
public class DetailsPane extends BoxPane implements Bindable {
    @WTKX private Label changeLabel;
    StockQuoteView stockQuoteView  = new StockQuoteView();
    EventTopicSubscriber<StockQuote> dataUpdateSubscriber = new EventTopicSubscriber<StockQuote>() {
        @Override
        public void onEvent(String topic, StockQuote stockQuote) {
            stockQuoteView.setBean(stockQuote);
            load(stockQuoteView);
        }
    };
    
    public DetailsPane() {
        AnnotationProcessor.process(this);
        load(stockQuoteView);
    }

    @EventSubscriber
    public void showStockQuoteDetails(StockQuoteSelection selection) {
        StockQuote currentQuote = (StockQuote) stockQuoteView.getBean();
        if (currentQuote != null) {
            EventBus.unsubscribe(EventConstants.TOPIC_STOCK_QUOTE+"."+currentQuote.getSymbol(), dataUpdateSubscriber);
        }
        StockQuote stockQuote = selection.getStockQuotes().get(0);
        stockQuoteView.setBean(stockQuote);
        load(stockQuoteView);
        EventBus.subscribe(EventConstants.TOPIC_STOCK_QUOTE+"."+stockQuote.getSymbol(), dataUpdateSubscriber);

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
