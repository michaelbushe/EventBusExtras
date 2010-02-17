package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.TaskAdapter;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Responsible for querying for Stock Data.
 * Refactored from the StockTracker
 */
public class StockQuery {
    public static final String SERVICE_HOSTNAME = "download.finance.yahoo.com";
    public static final String SERVICE_PATH = "/d/quotes.csv";
    public static GetQuery GET_QUERY;


    /**
     * Run a stock query for the provided symbols, calling the callback when finished.
     * @param symbols the symbols to query for
     * @param callback a callback
     */
    public static void runQuery(ArrayList<String> symbols, TaskListener callback) {
        GetQuery getQuery = new GetQuery(SERVICE_HOSTNAME, SERVICE_PATH);

        StringBuilder symbolsArgumentBuilder = new StringBuilder();
        for (int i = 0, n = symbols.getLength(); i < n; i++) {
            if (i > 0) {
                symbolsArgumentBuilder.append(",");
            }

            symbolsArgumentBuilder.append(symbols.get(i));
        }

        // Format:
        // s - symbol
        // n - company name
        // l1 - most recent value
        // o - opening value
        // h - high value
        // g - low value
        // c1 - change percentage
        // v - volume
        String symbolsArgument = symbolsArgumentBuilder.toString();
        getQuery.getParameters().put("s", symbolsArgument);
        getQuery.getParameters().put("f", "snl1ohgc1v");

        CSVSerializer quoteSerializer = new CSVSerializer();
        quoteSerializer.getKeys().add("symbol");
        quoteSerializer.getKeys().add("companyName");
        quoteSerializer.getKeys().add("value");
        quoteSerializer.getKeys().add("openingValue");
        quoteSerializer.getKeys().add("highValue");
        quoteSerializer.getKeys().add("lowValue");
        quoteSerializer.getKeys().add("change");
        quoteSerializer.getKeys().add("volume");

        quoteSerializer.setItemClass(StockQuote.class);
        getQuery.setSerializer(quoteSerializer);

        GET_QUERY = getQuery;

        GET_QUERY.execute(new QueryTaskListener(callback));
     }

     private static class QueryTaskListener implements TaskListener {
        private TaskListener callback;

        public QueryTaskListener(TaskListener callback) {
            this.callback = callback;
        }

        @Override
        public void taskExecuted(Task task) {
            callback.taskExecuted(task);
            if (task == GET_QUERY) {
                GET_QUERY = null;
            }
        }

        @Override
        public void executeFailed(Task task) {
            if (task == GET_QUERY) {
                GET_QUERY = null;
            }
            callback.executeFailed(task);
         }
     }
}
