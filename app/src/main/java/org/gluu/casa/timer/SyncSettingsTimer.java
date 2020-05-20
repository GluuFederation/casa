package org.gluu.casa.timer;

import org.gluu.casa.core.inmemory.IStoreService;
import org.gluu.casa.conf.MainSettings;
import org.gluu.casa.core.TimerService;
import org.quartz.JobExecutionContext;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author jgomer
 */
@ApplicationScoped
public class SyncSettingsTimer extends JobListenerSupport {

    private static final int SCAN_INTERVAL = 90;    //sync config file every 90sec

    @Inject
    private Logger logger;

    @Inject
    private IStoreService storeService;

    @Inject
    private TimerService timerService;

    @Inject
    private MainSettings mainSettings;

    private String jobName;

    public void activate(int gap) {

        jobName = getClass().getSimpleName() + "_syncfile";
        try {
            timerService.addListener(this, jobName);
            //Start in 90 seconds and repeat indefinitely
            timerService.schedule(jobName,  gap, -1, SCAN_INTERVAL);
        } catch (Exception e) {
            logger.warn("Automatic synchronization of config file won't be available");
            logger.error(e.getMessage(), e);
        }

    }

    @Override
    public String getName() {
        return jobName;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {

        try {
            logger.debug("SyncSettingsTimer timer running...");
            //Do a test retrieval, if it passes, the actual update is made
            storeService.put(2, "casa-dummy", true);
            Thread.sleep(1000);	//CB safer?
            
            if (storeService.get("casa-dummy") == null) {
            	logger.warn("It seems it is not safe to update config file right now. Skipping");
            } else {
            	mainSettings.updateConfigFile();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
