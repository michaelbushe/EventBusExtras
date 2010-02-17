package org.eventbus.tutorials.pivot.stocktracker;

import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewRowListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.content.TableViewRowComparator;

/**
 * Refactored out from Pivot's StockTracker.java
 */
public class StockTableView extends TableView {

    public StockTableView() {
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
}
