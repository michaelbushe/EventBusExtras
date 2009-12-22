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

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.apache.pivot.web.GetQuery;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewRowListener;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputTextListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TableViewRowComparator;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class StockTracker implements Application {
    private ArrayList<String> symbols = new ArrayList<String>();

    private Window window = null;

    @WTKX private TableView stocksTableView;
    @WTKX private TextInput symbolTextInput;
    @WTKX private Button addSymbolButton;
    @WTKX private Button removeSymbolsButton;
    @WTKX private Label lastUpdateLabel;
    @WTKX private Button yahooFinanceButton;
    @WTKX(id="detail.rootPane") private Container detailRootPane;
    @WTKX(id="detail.changeLabel") private Label detailChangeLabel;

    private GetQuery getQuery = null;

    public static final String LANGUAGE_PROPERTY_NAME = "language";
    public static final String SERVICE_HOSTNAME = "download.finance.yahoo.com";
    public static final String SERVICE_PATH = "/d/quotes.csv";
    public static final long REFRESH_INTERVAL = 15000;
    public static final String YAHOO_FINANCE_HOME = "http://finance.yahoo.com";

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
        symbols.add("JAVA");
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
        stocksTableView.getTableViewRowListeners().add(new TableViewRowListener.Adapter() {
            @Override
            public void rowsSorted(TableView tableView) {
                List<?> tableData = stocksTableView.getTableData();
                if (tableData.getLength() > 0) {
                    stocksTableView.setSelectedIndex(0);
                }
            }
        });

        stocksTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                refreshDetail();
            }
        });

        stocksTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView) {
                List<Object> tableData = (List<Object>)tableView.getTableData();
                tableData.setComparator(new TableViewRowComparator(tableView));
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

        symbolTextInput.getTextInputTextListeners().add(new TextInputTextListener() {
            @Override
            public void textChanged(TextInput textInput) {
                TextNode textNode = textInput.getTextNode();
                addSymbolButton.setEnabled(textNode.getCharacterCount() > 0);
            }
        });

        symbolTextInput.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    addSymbol();
                }

                return false;
            }
        });

        addSymbolButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                addSymbol();
            }
        });

        removeSymbolsButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                removeSelectedSymbols();
            }
        });

        yahooFinanceButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Desktop desktop = Desktop.getDesktop();

                try {
                    desktop.browse(new URL(YAHOO_FINANCE_HOME).toURI());
                } catch(MalformedURLException exception) {
                    throw new RuntimeException(exception);
                } catch(URISyntaxException exception) {
                    throw new RuntimeException(exception);
                } catch(IOException exception) {
                    System.out.println("Unable to open "
                        + YAHOO_FINANCE_HOME + " in default browser.");
                }
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

        symbolTextInput.requestFocus();
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
        getQuery = new GetQuery(SERVICE_HOSTNAME, SERVICE_PATH);

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

        getQuery.execute(new TaskAdapter<Object>(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                if (task == getQuery) {
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

                    getQuery = null;
                }
            }

            @Override
            public void executeFailed(Task<Object> task) {
                if (task == getQuery) {
                    System.err.println(task.getFault());
                    getQuery = null;
                }
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private void refreshDetail() {
        int firstSelectedIndex = stocksTableView.getFirstSelectedIndex();
        removeSymbolsButton.setEnabled(firstSelectedIndex != -1);

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

    @SuppressWarnings("unchecked")
    private void addSymbol() {
        String symbol = symbolTextInput.getText().toUpperCase();
        if (symbols.indexOf(symbol) == -1) {
            symbols.add(symbol);

            List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
            StockQuote stockQuote = new StockQuote();
            stockQuote.setSymbol(symbol);
            int index = tableData.add(stockQuote);

            stocksTableView.setSelectedIndex(index);
        }

        symbolTextInput.setText("");
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
