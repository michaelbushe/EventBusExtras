package org.eventbus.tutorials.pivot.stocktracker.event;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.bushe.swing.event.PublicationStatus;
import org.bushe.swing.event.PublicationStatusTracker;


/**
 * Event fired when the user adds or removes a symbol from the working list
 */
public class SymbolListChangeEvent implements PublicationStatusTracker {
    private PublicationStatus publicationStatus = PublicationStatus.Unpublished;

    @Override
    public PublicationStatus getPublicationStatus() {
        return publicationStatus;
    }

    @Override
    public void setPublicationStatus(PublicationStatus publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public enum ChangeType {
        ADDED, REMOVED
    }

    private List<String> symbols;
    private ChangeType changeType;

    public SymbolListChangeEvent(String symbol, ChangeType changeType) {
        this.symbols = new ArrayList<String>(symbol);
        this.changeType = changeType;
    }

    public SymbolListChangeEvent(List<String> symbols, ChangeType changeType) {
        this.symbols = symbols;
        this.changeType = changeType;
    }

    public List<String> getSymbols() {
        return symbols;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

}
