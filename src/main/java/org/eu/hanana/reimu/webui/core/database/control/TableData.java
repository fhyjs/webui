package org.eu.hanana.reimu.webui.core.database.control;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class TableData {
    protected final String table;
    protected List<ColumnData> columns=new ArrayList<>();
    public TableData addColumns(ColumnData... columns){
        this.columns.addAll(Arrays.stream(columns).toList());
        return this;
    }
}
