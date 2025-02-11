package org.eu.hanana.reimu.webui.core.database.control;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ColumnData {
    public final String name;
    public final String type;
    public boolean primaryKey;
    public boolean autoIncrement;
    public boolean notNull;
    public boolean unique;

    public ColumnData setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }
    public ColumnData setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }
    public ColumnData setNotNull(boolean notNull) {
        this.notNull = notNull;
        return this;
    }
    public ColumnData setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }
}
