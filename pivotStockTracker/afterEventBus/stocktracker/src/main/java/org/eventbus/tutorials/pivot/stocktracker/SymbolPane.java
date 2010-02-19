package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.effects.FadeDecorator;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.PublicationStatus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.eventbus.tutorials.pivot.stocktracker.event.StockQuoteSelection;
import org.eventbus.tutorials.pivot.stocktracker.event.SymbolListChangeEvent;

import java.awt.*;
import java.util.List;

/**
 * Component for typing in stock symbols with add and remove buttons
 */
public class SymbolPane extends BoxPane implements Bindable {
//Replaced in WTKX    private static final Pattern LEGAL_PATTERN = Pattern.compile("[A-Za-z]+");

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

        symbolTextInput.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyTyped(Component component, char keyCode) {
                //Undoes the validation color
                symbolTextInput.getStyles().put("backgroundColor", Color.WHITE);
                TextNode textNode = symbolTextInput.getTextNode();
                String symbol = textNode.getText();
                /*
                Replaced by WTKX validator
                if (!LEGAL_PATTERN.matcher(symbol).matches()) {

                    symbolTextInput.getDecorators().add(fadeDecorator);
                } else {
                    symbolTextInput.getDecorators().remove(fadeDecorator);
                }
                */
                if (symbol.length() < 1) {
                    return false;
                }
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

    private void addSymbol() {
        String symbol = symbolTextInput.getText().toUpperCase();
        SymbolListChangeEvent addedEvent = new SymbolListChangeEvent(symbol,
                SymbolListChangeEvent.ChangeType.ADDED);
        EventBus.publish(addedEvent);
        if (addedEvent.getPublicationStatus() == PublicationStatus.Vetoed) {
            String errorText = symbol + " already exists.";
            symbolTextInput.getTextNode().setText(errorText);
            symbolTextInput.getStyles().put("backgroundColor", Color.RED);
            symbolTextInput.setSelection(0, errorText.length());
        } else {
            symbolTextInput.setText("");
        }
    }

    private void removeSelectedSymbols(String symbol) {
        SymbolListChangeEvent removedEvent = new SymbolListChangeEvent(symbol,
                SymbolListChangeEvent.ChangeType.REMOVED);
        EventBus.publish(removedEvent);
    }
}

