function MineralOccurrencesML(webFeatureService) {

    /**
     * Returns the names of mines that a service is providing. There may be many upon many of these
     */
    function getMineNames() {
        //query all of the mines then return a list of names
        var mineResponse = webFeatureService.doGetRequest("");

        var mines = parseMineResponse(mineResponse);

    }

    /**
     * Takes a mineResponse mo:Mine coolection from a WebFeatureService and returns a list/array of Mine objects
     * @param mineResponse
     */
    function parseMineResponse(mineResponse) {
           
    }

}