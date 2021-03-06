/*
 * Copyright 2014-2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.openddal.command.ddl;

import com.openddal.command.CommandInterface;
import com.openddal.dbobject.schema.Schema;
import com.openddal.engine.Session;
import com.openddal.excutor.effects.DropTableExecutor;

/**
 * This class represents the statement
 * DROP TABLE
 *
 * @author <a href="mailto:jorgie.mail@gmail.com">jorgie li</a>
 */
public class DropTable extends SchemaCommand {

    private boolean ifExists;
    private String tableName;
    private DropTable next;
    private int dropAction;
    private DropTableExecutor executor;

    public DropTable(Session session, Schema schema) {
        super(session, schema);
        dropAction = session.getDatabase().getSettings().dropRestrict ?
                AlterTableAddConstraint.RESTRICT :
                AlterTableAddConstraint.CASCADE;
        executor = new DropTableExecutor(this);
    }

    /**
     * Chain another drop table statement to this statement.
     *
     * @param drop the statement to add
     */
    public void addNextDropTable(DropTable drop) {
        if (next == null) {
            next = drop;
        } else {
            next.addNextDropTable(drop);
        }
    }

    @Override
    public int getType() {
        return CommandInterface.DROP_TABLE;
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean b) {
        ifExists = b;
        if (next != null) {
            next.setIfExists(b);
        }
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getDropAction() {
        return dropAction;
    }

    public void setDropAction(int dropAction) {
        this.dropAction = dropAction;
        if (next != null) {
            next.setDropAction(dropAction);
        }
    }

    public DropTable getNext() {
        return next;
    }

    @Override
    public DropTableExecutor getExecutor() {
        if(executor == null) {
            executor = new DropTableExecutor(this);
        }
        return executor;
    }

    
}