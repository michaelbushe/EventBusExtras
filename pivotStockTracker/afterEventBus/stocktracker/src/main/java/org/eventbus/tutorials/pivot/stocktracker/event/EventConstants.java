package org.eventbus.tutorials.pivot.stocktracker.event;

import org.apache.pivot.collections.List;
import org.bushe.swing.event.generics.TypeReference;
import org.eventbus.tutorials.pivot.stocktracker.StockQuote;

import java.lang.reflect.Type;

public class EventConstants {
    public static final String TOPIC_STOCK_QUOTE = "Quote";
    public static final Type SUPER_TYPE_TOKEN_LIST_OF_STOCK_QUOTE = new TypeReference<List<StockQuote>>() {}.getType();
}
