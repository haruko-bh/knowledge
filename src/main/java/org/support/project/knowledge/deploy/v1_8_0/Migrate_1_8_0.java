package org.support.project.knowledge.deploy.v1_8_0;

import org.support.project.knowledge.deploy.Migrate;
import org.support.project.ormapping.tool.dao.InitializeDao;
import org.support.project.web.logic.DBConnenctionLogic;

public class Migrate_1_8_0 implements Migrate {

    public static Migrate_1_8_0 get() {
        return org.support.project.di.Container.getComp(Migrate_1_8_0.class);
    }

    @Override
    public boolean doMigrate() throws Exception {
        if (DBConnenctionLogic.get().connectCustomConnection()) {
            // PostgreSQLの場合だけ、式インデックスを張る
            InitializeDao initializeDao = InitializeDao.get();
            String[] sqlpaths = {
                "/org/support/project/knowledge/deploy/v1_8_0/migrate.sql",
            };
            initializeDao.initializeDatabase(sqlpaths);
        }
        return true;
    }
}