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
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.VetoSubscriber;
import org.eventbus.tutorials.pivot.stocktracker.event.EventConstants;
import org.eventbus.tutorials.pivot.stocktracker.event.SymbolListChangeEvent;

import java.util.Comparator;
import java.util.Locale;

public class StockTracker implements Application, EventSubscriber<SymbolListChangeEvent> {
    public static final String LANGUAGE_PROPERTY_NAME = "language";
    public static final long REFRESH_INTERVAL = 15000;

    @WTKX(id="symbol.symbolPane")  private SymbolPane symbolPane;
    private ArrayList<String> symbols = new ArrayList<String>();
    private Window window = null;


    public StockTracker() {
        AnnotationProcessor.process(this);
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
        TaskListener taskListener = new TaskAdapter<StockQuote>(new TaskListener<StockQuote>() {
             @Override
             public void taskExecuted(Task<StockQuote> task) {
                 List<StockQuote> quotes = (List<StockQuote>)task.getResult();
                 for (StockQuote quote : quotes) {
                     EventBus.publish(EventConstants.TOPIC_STOCK_QUOTE+"."+quote.getSymbol(), quote);
                 }
                 EventBus.publish(EventConstants.SUPER_TYPE_TOKEN_LIST_OF_STOCK_QUOTE, quotes);
             }

             @Override
             public void executeFailed(Task<StockQuote> task) {
                 System.err.println(task.getFault());
             }
         });
        return taskListener;
    }

    @VetoSubscriber
    public boolean disallowDuplicateAdd(SymbolListChangeEvent event) {
        if (event.getChangeType() == SymbolListChangeEvent.ChangeType.ADDED) {
            if (event != null && event.getSymbols() != null) {
                for (String symbol : event.getSymbols()) {
                    for (String existingSymbol : this.symbols) {
                        if (existingSymbol.equals(symbol)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
            if (symbol != null) {
                symbols.remove(symbol);
            }
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(StockTracker.class, args);
    }
}
