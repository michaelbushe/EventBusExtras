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
    @WTKX(id="detail.rootPane") private Container detailRootPane;
    @WTKX(id="detail.changeLabel") private Label detailChangeLabel;

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

        // Wire up event handlers
        stocksTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                refreshDetail();
            }
        });

        stocksTableView.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE) {
                    removeSelectedSymbols();
                }

                return false;
            }
        });

        window.open(display);

        refreshTable();

        ApplicationContext.scheduleRecurringCallback(new Runnable() {
            @Override
            public void run() {
                refreshTable();
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

    @SuppressWarnings("unchecked")
    private void refreshTable() {
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

                 refreshDetail();

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

    @SuppressWarnings("unchecked")
    private void refreshDetail() {
        int firstSelectedIndex = stocksTableView.getFirstSelectedIndex();

        StockQuote stockQuote = null;

        if (firstSelectedIndex != -1) {
            int lastSelectedIndex = stocksTableView.getLastSelectedIndex();

            if (firstSelectedIndex == lastSelectedIndex) {
                List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
                stockQuote = tableData.get(firstSelectedIndex);
            } else {
                stockQuote = new StockQuote();
            }
        } else {
            stockQuote = new StockQuote();
        }
        symbolPane.setSelectedStockQuote(stockQuote);

        StockQuoteView stockQuoteView = new StockQuoteView(stockQuote);
        detailRootPane.load(stockQuoteView);

        float change = stockQuote.getChange();
        if (!Float.isNaN(change)
            && change < 0) {
            Form.setFlag(detailChangeLabel, new Form.Flag(MessageType.ERROR));
        } else {
            Form.setFlag(detailChangeLabel, (Form.Flag)null);
        }
    }


    @Override
    public void onEvent(SymbolListChangeEvent event) {
        if (event.getChangeType() == SymbolListChangeEvent.ChangeType.ADDED) {
            addSymbol(event);
        } else {
            removeSelectedSymbols();
        }
    }

    @SuppressWarnings("unchecked")
    private void addSymbol(SymbolListChangeEvent event) {
        String symbol = event.getSymbol();
        if (symbols.indexOf(symbol) == -1) {
            symbols.add(symbol);

            List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
            StockQuote stockQuote = new StockQuote();
            stockQuote.setSymbol(symbol);
            int index = tableData.add(stockQuote);

            stocksTableView.setSelectedIndex(index);
        }

        refreshTable();
    }

    private void removeSelectedSymbols() {
        int selectedIndex = stocksTableView.getFirstSelectedIndex();
        int selectionLength = stocksTableView.getLastSelectedIndex() - selectedIndex + 1;
        stocksTableView.getTableData().remove(selectedIndex, selectionLength);
        symbols.remove(selectedIndex, selectionLength);

        if (selectedIndex >= symbols.getLength()) {
            selectedIndex = symbols.getLength() - 1;
        }

        stocksTableView.setSelectedIndex(selectedIndex);

        if (selectedIndex == -1) {
            refreshDetail();
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(StockTracker.class, args);
    }
}
