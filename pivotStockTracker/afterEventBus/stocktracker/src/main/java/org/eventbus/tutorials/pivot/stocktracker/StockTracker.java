/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class StockTracker implements Application, EventSubscriber<SymbolListChangeEvent> {
    private ArrayList<String> symbols = new ArrayList<String>();

    private Window window = null;

    @WTKX(id="table.stockTablePane")  private StockTableView stocksTableView;
    @WTKX(id="symbol.symbolPane")  private SymbolPane symbolPane;
    @WTKX(id="yahooFinance.yahooPane")  private YahooPane yahooPane;
    @WTKX(id="yahooFinance.lastUpdateLabel")  private Label lastUpdateLabel;

    public static final String LANGUAGE_PROPERTY_NAME = "language";

    public static final long REFRESH_INTERVAL = 15000;

    public StockTracker() {
        symbols.setComparator(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        symbols.add("EBAY");
        symbols.add("AAPL");
        symbols.add("MSFT");
        symbols.add("AMZN");
        symbols.add("GOOG");
        symbols.add("ORCL");
    }

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        // Set the locale
        String language = properties.get(LANGUAGE_PROPERTY_NAME);
        if (language != null) {
            Locale.setDefault(new Locale(language));
        }

        // Load and bind to the WTKX source
        Resources resources = new Resources(getClass().getName(), "UTF-8");
        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        window = (Window)wtkxSerializer.readObject(this, "stocktracker.wtkx");
        wtkxSerializer.bind(this, StockTracker.class);

        window.open(display);

        refreshData();

        ApplicationContext.scheduleRecurringCallback(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        }, REFRESH_INTERVAL);

        EventBus.subscribe(SymbolListChangeEvent.class, this);
        symbolPane.requestFocus();
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    private void refreshData() {
        StockQuery.runQuery(symbols, createTaskListener());
    }

    private TaskListener createTaskListener() {
        TaskListener taskListener = new TaskAdapter<Object>(new TaskListener<Object>() {
             @Override
             public void taskExecuted(Task<Object> task) {
                 List<Object> quotes = (List<Object>)task.getResult();

                 // Preserve any existing sort and selection
                 Sequence<?> selectedStocks = stocksTableView.getSelectedRows();

                 List<Object> tableData = (List<Object>)stocksTableView.getTableData();
                 Comparator<Object> comparator = tableData.getComparator();
                 quotes.setComparator(comparator);

                 stocksTableView.setTableData(quotes);

                 if (selectedStocks.getLength() > 0) {
                     // Select current indexes of selected stocks
                     for (int i = 0, n = selectedStocks.getLength(); i < n; i++) {
                         StockQuote selectedStock = (StockQuote)selectedStocks.get(i);

                         int index = 0;
                         for (StockQuote stock : (List<StockQuote>)stocksTableView.getTableData()) {
                             if (stock.getSymbol().equals(selectedStock.getSymbol())) {
                                 stocksTableView.addSelectedIndex(index);
                                 break;
                             }

                             index++;
                         }
                     }
                 } else {
                     if (quotes.getLength() > 0) {
                         stocksTableView.setSelectedIndex(0);
                     }
                 }

                 DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                     DateFormat.MEDIUM, Locale.getDefault());
                 lastUpdateLabel.setText(dateFormat.format(new Date()));
             }

             @Override
             public void executeFailed(Task<Object> task) {
                     System.err.println(task.getFault());
             }
         });
        return taskListener;
    }


    @Override
    public void onEvent(SymbolListChangeEvent event) {
        if (event.getChangeType() == SymbolListChangeEvent.ChangeType.ADDED) {
            addSymbols(event);
        } else {
            removeSymbols(event);
        }
    }

    private void addSymbols(SymbolListChangeEvent event) {
        List<String> symbols = event.getSymbols();
        for (String symbol : symbols) {
            if (this.symbols.indexOf(symbol) == -1) {
                this.symbols.add(symbol);
            }
        }
        refreshData();
    }

    private void removeSymbols(SymbolListChangeEvent event) {
        for (String symbol : event.getSymbols()) {
            symbols.remove(symbol);
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(StockTracker.class, args);
    }
}
