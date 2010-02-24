package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.effects.FadeDecorator;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.eventbus.tutorials.pivot.stocktracker.event.DuplicateSymbolEvent;
import org.eventbus.tutorials.pivot.stocktracker.event.StockQuoteSelection;
import org.eventbus.tutorials.pivot.stocktracker.event.SymbolListChangeEvent;

import java.awt.*;
import java.util.List;

/**
 * Component for typing in stock symbols with add and remove buttons
 */
public class SymbolPane extends BoxPane implements Bindable {

    @WTKX private TextInput symbolTextInput;
    @WTKX private Button addSymbolButton;
    @WTKX private Button removeSymbolsButton;
    private FadeDecorator fadeDecorator = new FadeDecorator();;

    public SymbolPane() {
        AnnotationProcessor.process(this);
        EventBus.setCacheSizeForEventClass(StockQuoteSelection.class, 1);
    }

    @Override
    public void initialize() {
        symbolTextInput.getTextInputTextListeners().add(new TextInputTextListener() {
            @Override
            public void textChanged(TextInput textInput) {
                TextNode textNode = textInput.getTextNode();
                addSymbolButton.setEnabled(textNode.getCharacterCount() > 0);
            }
        });

        
        removeSymbolsButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                List<StockQuoteSelection> stockQuoteSelections = EventBus.getCachedEvents(StockQuoteSelection.class);
                if (stockQuoteSelections == null || stockQuoteSelections.size() < 1) {
                    return;
                }
                StockQuoteSelection stockQuoteSelection = stockQuoteSelections.get(0);
                for (StockQuote quote : stockQuoteSelection.getStockQuotes()) {
                    removeSelectedSymbols(quote.getSymbol());
                }
            }
        });
        symbolTextInput.requestFocus();

    }

    @Override
    public boolean requestFocus() {
        return symbolTextInput.requestFocus();
    }

    @EventSubscriber
    public void setSelectedStockQuote(StockQuoteSelection stockQuoteSelection) {
        boolean symbolSelected = stockQuoteSelection.getStockQuotes() != null && !stockQuoteSelection.getStockQuotes().isEmpty();
        addSymbolButton.setEnabled(!symbolSelected);
        removeSymbolsButton.setEnabled(symbolSelected);
    }

    @EventSubscriber
    public void onEvent(DuplicateSymbolEvent dupSymbolEvent) {
        String errorText = dupSymbolEvent.getSymbol() + " already exists.";
        symbolTextInput.getTextNode().setText(errorText);
        symbolTextInput.getStyles().put("backgroundColor", Color.RED);
        symbolTextInput.setSelection(0, errorText.length());
    }

    private void addSymbol() {
        String symbol = symbolTextInput.getText().toUpperCase();
        SymbolListChangeEvent addedEvent = new SymbolListChangeEvent(symbol,
                SymbolListChangeEvent.ChangeType.ADDED);
        EventBus.publish(addedEvent);
    }

    private void removeSelectedSymbols(String symbol) {
        SymbolListChangeEvent removedEvent = new SymbolListChangeEvent(symbol,
                SymbolListChangeEvent.ChangeType.REMOVED);
        EventBus.publish(removedEvent);
    }
}

