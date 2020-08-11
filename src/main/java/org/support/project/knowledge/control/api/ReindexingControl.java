package org.support.project.knowledge.control.api;

import java.io.File;

import org.support.project.common.bat.AsyncJavaJob;
import org.support.project.common.bat.BatListener;
import org.support.project.common.bat.ConsoleListener;
import org.support.project.common.bat.JobResult;
import org.support.project.common.log.Log;
import org.support.project.common.log.LogFactory;
import org.support.project.common.util.StringUtils;
import org.support.project.di.DI;
import org.support.project.di.Instance;
import org.support.project.knowledge.bat.ReIndexingBat;
import org.support.project.knowledge.config.AppConfig;
import org.support.project.knowledge.config.SystemConfig;
import org.support.project.web.boundary.Boundary;
import org.support.project.web.common.HttpStatus;
import org.support.project.web.control.ApiControl;
import org.support.project.web.control.service.Post;
import org.support.project.web.dao.SystemConfigsDao;
import org.support.project.web.entity.SystemConfigsEntity;

@DI(instance = Instance.Prototype)
public class ReindexingControl extends ApiControl {

    /** ログ */
    private static final Log LOG = LogFactory.getLog(ReindexingControl.class);

    private Thread thread;

    /**
     * invoke reindexing bat
     */
    @Post(path="api/reindexing", subscribeToken="", checkReferer=false)
    public Boundary index() {
        // 管理者権限チェック
        if (!super.getLoginedUser().isAdmin()) {
            return sendError(HttpStatus.SC_403_FORBIDDEN, "FORBIDDEN");
        }
        if (!reserve()) {
            return send(getResource("message.allready.started"));
        }
        begin();
        return send(getResource("knowledge.reindexing.msg.start"));
    }

    /**
     * reserve reindexing job
     */
    public boolean reserve() {
        SystemConfigsEntity entity = SystemConfigsDao.get().selectOnKey(SystemConfig.RE_INDEXING, AppConfig.get().getSystemName());
        if (entity != null) {
            return false;
        }

        Long start = 1L;
        Long end = 10000L;
        String startStr = getParam("start");
        if (StringUtils.isLong(startStr)) {
            start = Long.parseLong(startStr);
        }
        String endStr = getParam("end");
        if (StringUtils.isInteger(endStr)) {
            end = Long.parseLong(endStr);
        }
        String val = "start=" + start + ",end=" + end;

        entity = new SystemConfigsEntity();
        entity.setSystemName(AppConfig.get().getSystemName());
        entity.setConfigName(SystemConfig.RE_INDEXING);
        entity.setConfigValue(val);
        SystemConfigsDao.get().save(entity);
        return true;
    }

    /**
     * begin reindexing job
     */
    public void begin() {
        AppConfig appConfig = AppConfig.get();

        LOG.info(appConfig.getWebRealPath());

        AsyncJavaJob job = new AsyncJavaJob();
        job.addjarDir(new File(appConfig.getWebRealPath().concat("/WEB-INF/lib/")));
        job.addClassPathDir(new File(appConfig.getWebRealPath().concat("/WEB-INF/classes/")));
        job.setMainClass(ReIndexingBat.class.getName());
        job.setConsoleListener(new ConsoleListener() {
            @Override
            public void write(String message) {
                LOG.info(message);
            }
        });
        job.addListener(new BatListener() {
            @Override
            public void finish(JobResult result) {
                LOG.info("Reindexing is ended. [status]" + result.getResultCode());
            }
        });
        thread = new Thread(job);
        thread.start();
    }
}
