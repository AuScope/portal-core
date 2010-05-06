package org.auscope.portal.csw;

import java.util.concurrent.Executor;

import org.springframework.stereotype.Repository;

/**
 * User: Mathew Wyatt
 * Date: 12/07/2009
 * @version $Id$
 */
@Repository
public class CSWThreadExecutor implements Executor {
    public void execute(Runnable runnable) {
        new Thread(runnable).start();
    }
}
