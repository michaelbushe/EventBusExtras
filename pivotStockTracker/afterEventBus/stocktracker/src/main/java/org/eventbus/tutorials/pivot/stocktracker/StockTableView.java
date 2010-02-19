package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.content.TableViewRowComparator;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.eventbus.tutorials.pivot.stocktracker.event.EventConstants;
import org.eventbus.tutorials.pivot.stocktracker.event.StockQuoteSelection;
import org.eventbus.tutorials.pivot.stocktracker.event.SymbolListChangeEvent;

import java.util.Comparator;

/**
 * Refactored out from Pivot's StockTracker.java
 */
public class StockTableView extends TableView implements EventSubscriber<List<StockQuote>> {

    public StockTableView() {
        EventBus.subscribe(EventConstants.SUPER_TYPE_TOKEN_LIST_OF_STOCK_QUOTE, this);
        AnnotationProcessor.process(this);
        getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                int firstSelectedIndex = getFirstSelectedIndex();

                StockQuote stockQuote = null;

                if (firstSelectedIndex != -1) {
                    int lastSelectedIndex = getLastSelectedIndex();

                    if (firstSelectedIndex == lastSelectedIndex) {
                        List<StockQuote> tableData = (List<StockQuote>)getTableData();
                        stockQuote = tableData.get(firstSelectedIndex);
                    } else {
                        stockQuote = new StockQuote();
                    }
                } else {
                    stockQuote = new StockQuote();
                }
                EventBus.publish(new StockQuoteSelection(new ArrayList<StockQuote>(stockQuote)));
            }
        });

        getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE) {
                    int selectedIndex = getFirstSelectedIndex();
                    int selectionLength = getLastSelectedIndex() - selectedIndex + 1;
                    List<String> symbols = new ArrayList<String>();
                    for (int i = 0; i < selectionLength; i++) {
                        StockQuote quote = (StockQuote) getTableData().get(selectedIndex+i);
                        symbols.add(quote.getSymbol());
                    }
                    SymbolListChangeEvent removedEvent = new SymbolListChangeEvent(
                            symbols, SymbolListChangeEvent.ChangeType.REMOVED);
                    EventBus.publish(removedEvent);
                }
                return false;
            }
        });

        getTableViewRowListeners().add(new TableViewRowListener.Adapter() {
            @Override
            public void rowsSorted(TableView tableView) {
                List<?> tableData = getTableData();
                if (tableData.getLength() > 0) {
                    setSelectedIndex(0);
                }
            }
        });

        getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView) {
                List<Object> tableData = (List<Object>)tableView.getTableData();
                tableData.setComparator(new TableViewRowComparator(tableView));
            }
        });
    }

    @org.bushe.swing.event.annotation.EventSubscriber
    public void addOrRemoveSymbols(SymbolListChangeEvent event) {
        int selectedIndex = getFirstSelectedIndex();
        int selectionLength = getLastSelectedIndex() - selectedIndex + 1;
        List<StockQuote> tableData = (List<StockQuote>)getTableData();
        boolean first = true;
        for (String symbol : event.getSymbols()) {
            StockQuote stockQuote = new StockQuote();
            stockQuote.setSymbol(symbol);
            if (event.getChangeType() == SymbolListChangeEvent.ChangeType.ADDED) {
                selectedIndex = tableData.add(stockQuote);
                if (first) {
                    //Only select the first one since we don't know the order
                    selectionLength = 1;
                    EventBus.publish(stockQuote);
                }
            } else {
                int index = tableData.remove(stockQuote);
                if (index >= selectedIndex && index <= selectedIndex+selectionLength) {
                    if (index == selectedIndex) {
                        if (index != 0) {
                            selectedIndex--;
                        }
                    }
                    selectionLength--;    
                }
            }
            first = false;
        }
        setSelectedIndex(selectedIndex);
    }

    public void onEvent(List<StockQuote> quotes) {
        // Preserve any existing sort and selection
        Sequence<?> selectedStocks = getSelectedRows();

        List<StockQuote> tableData = (List<StockQuote>)getTableData();
        Comparator<StockQuote> comparator = tableData.getComparator();
        quotes.setComparator(comparator);

        setTableData(quotes);

        if (selectedStocks.getLength() > 0) {
            // Select current indexes of selected stocks
            for (int i = 0, n = selectedStocks.getLength(); i < n; i++) {
                StockQuote selectedStock = (StockQuote)selectedStocks.get(i);

                int index = 0;
                for (StockQuote stock : (List<StockQuote>)getTableData()) {
                    if (stock.getSymbol().equals(selectedStock.getSymbol())) {
                        addSelectedIndex(index);
                        break;
                    }

                    index++;
                }
            }
        } else {
            if (quotes.getLength() > 0) {
                setSelectedIndex(0);
            }
        }
    }
}
