package net.shortninja.staffplus.util.database.migrations.mysql;

import net.shortninja.staffplus.util.database.migrations.Migration;

public class V15_CreateDelayedActionsTableMigration implements Migration {
    @Override
    public String getStatement() {
        return "CREATE TABLE IF NOT EXISTS sp_delayed_actions (ID INT NOT NULL AUTO_INCREMENT,  command VARCHAR(255) NOT NULL,  Player_UUID VARCHAR(36) NOT NULL,  PRIMARY KEY (ID)) ENGINE = InnoDB;";
    }

    @Override
    public int getVersion() {
        return 15;
    }
}
