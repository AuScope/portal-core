/**
 * Utility class for dealing with provider names.
 * For example to generate a human-readable (and short) name based on the real name. 
 */
Ext.define('portal.util.ProviderNameTransformer', {    
    statics : {
        abbreviateName : function(providerName) {  
            
            var SERVICE_PROVIDERS = [
                'CSIRO',
                'Geoscience Australia',
                'Northern Territory',
                'Queensland',
                'South Australia',
                'Tasmania',
                'Victoria',
                'Western Australia'
            ];
            
            for (var i = 0; i < SERVICE_PROVIDERS.length; i++) {  
                if (providerName.indexOf(SERVICE_PROVIDERS[i]) != -1) {
                    providerName = SERVICE_PROVIDERS[i];
                    break;
                } else if (providerName.indexOf('NSW') != -1) {
                    providerName = 'New South Wales';  
                    break;
                }
            }
            
            return providerName;
        } 
    }
});