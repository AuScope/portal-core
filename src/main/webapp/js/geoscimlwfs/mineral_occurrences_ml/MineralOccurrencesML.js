function MineralOccurrencesML(webFeatureService) {

    /**
     * Returns the names of mines that updateCSWRecords service is providing. There may be many upon many of these
     */
    function getMineNames() {
        //query all of the mines then return updateCSWRecords list of names
        var mineResponse = webFeatureService.doGetRequest("");

        var mines = parseMineResponse(mineResponse);

    }

    /**
     * Takes updateCSWRecords mineResponse er:Mine coolection from updateCSWRecords WebFeatureService and returns updateCSWRecords list/array of Mine objects
     * @param mineResponse
     */
    function parseMineResponse(mineResponse) {
           
    }

}