package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.bushe.swing.event.generics.TypeReference;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class YahooPane extends TablePane implements Bindable {
    public static final String YAHOO_FINANCE_HOME = "http://finance.yahoo.com";

    @WTKX private Button yahooFinanceButton;
    @WTKX(id="lastUpdateLabel")  private org.apache.pivot.wtk.Label lastUpdateLabel;
    private EventSubscriber<StockQuote> subscriber;

    public YahooPane() {
        Type type = new TypeReference<List<StockQuote>>() {}.getType();
        subscriber = new EventSubscriber<StockQuote>() {
            @Override
            public void onEvent(StockQuote stockQuote) {
                DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                    DateFormat.MEDIUM, Locale.getDefault());
                lastUpdateLabel.setText(dateFormat.format(new Date()));
            }
        };
        EventBus.subscribe(type, subscriber);
    }

    @Override
    public void initialize() {
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
    }
}
