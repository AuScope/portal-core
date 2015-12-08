/**
 * An enum representing the different kind of test results that can be returned
 */
Ext.define('admin.tests.TestStatus', {
    statics : {
        /**
         * The test was completely successful, there is nothing detectable that is going wrong
         */
        Success : 0,
        /**
         * The test experienced a partial failure (Eg - a web service is accessible, but returning errors)
         */
        Warning : 1,
        /**
         * The test completely failed
         */
        Error : 2,
        /**
         * The test wasn't able to initialise
         */
        Unavailable : 3,
        /**
         * The test is currently running
         */
        Running : 4,
        /**
         * The test has been created but is not yet running
         */
        Initialising : 5
    }
});