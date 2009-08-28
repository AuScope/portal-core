package org.auscope.portal.csw;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.concurrent.Executor;

/**
 * User: Mathew Wyatt
 * Date: 12/07/2009
 * Time: 9:07:53 PM
 */
@Repository
public class CSWThreadExecutor implements Executor {
    public void execute(Runnable runnable) {
        new Thread(runnable).start();
    }
}
