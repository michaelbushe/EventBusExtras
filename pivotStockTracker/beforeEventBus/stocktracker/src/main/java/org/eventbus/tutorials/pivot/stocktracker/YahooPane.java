package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class YahooPane extends TablePane implements Bindable {
    public static final String YAHOO_FINANCE_HOME = "http://finance.yahoo.com";

    @WTKX private Button yahooFinanceButton;

    public YahooPane() {
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
