package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.*;
import org.apache.pivot.wtk.text.TextNode;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;

/**
 * Component for typing in stock symbols with add and remove buttons
 */
public class SymbolPane extends BoxPane implements Bindable {
    @WTKX private TextInput symbolTextInput;
    @WTKX private Button addSymbolButton;
    @WTKX private Button removeSymbolsButton;
    ArrayList<SymbolListChangeEventListener> listenerList = new ArrayList<SymbolListChangeEventListener>();
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

    public void addChangeListener(SymbolListChangeEventListener symbolListChangeEventListener) {
        listenerList.add(symbolListChangeEventListener);
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
        notifyListeners(addedEvent);
        symbolTextInput.setText("");
    }

    private void removeSelectedSymbols(String symbol) {
        SymbolListChangeEvent removedEvent = new SymbolListChangeEvent(symbol,
                SymbolListChangeEvent.ChangeType.REMOVED);
        notifyListeners(removedEvent);
    }

    private void notifyListeners(SymbolListChangeEvent removedEvent) {
        for (int i = 0; i < listenerList.getLength(); i++) {
            SymbolListChangeEventListener symbolListChangeEventListener = listenerList.get(i);
            symbolListChangeEventListener.symbolChanged(removedEvent);
        }
    }

}

