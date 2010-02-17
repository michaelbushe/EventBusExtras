package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;
import org.bushe.swing.event.EventBus;

/**
 * Component for typing in stock symbols with add and remove buttons
 */
public class SymbolPane extends BoxPane implements Bindable {
    @WTKX private TextInput symbolTextInput;
    @WTKX private Button addSymbolButton;
    @WTKX private Button removeSymbolsButton;
    private String lastSymbol;

    public SymbolPane() {
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
                if (lastSymbol != null) {
                    removeSelectedSymbols(lastSymbol);
                }
            }
        });
        symbolTextInput.requestFocus();

    }

    @Override
    public boolean requestFocus() {
        return symbolTextInput.requestFocus();
    }

    public void setSelectedStockQuote(StockQuote stockQuote) {
        boolean enabled = stockQuote.getSymbol() == null || "".equals(stockQuote.getSymbol());
        addSymbolButton.setEnabled(enabled);
        removeSymbolsButton.setEnabled(!enabled);
    }

    private void addSymbol() {
        String symbol = symbolTextInput.getText().toUpperCase();
        symbolTextInput.setText("");
        SymbolListChangeEvent addedEvent = new SymbolListChangeEvent(symbol,
                SymbolListChangeEvent.ChangeType.ADDED);
        EventBus.publish(addedEvent);
        symbolTextInput.setText("");
    }

    private void removeSelectedSymbols(String symbol) {
        SymbolListChangeEvent removedEvent = new SymbolListChangeEvent(symbol,
                SymbolListChangeEvent.ChangeType.REMOVED);
        EventBus.publish(removedEvent);
    }
}

