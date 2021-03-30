function toCsv(list){
	var toIsoDateString = function(date){
    	var pad = function(number) {
	      var r = String(number);
	      if ( r.length === 1 ) {
	        r = '0' + r;
	      }
	      return r;
	    };
	    
	    return date.getUTCFullYear()
	    + '-' + pad( date.getUTCMonth() + 1 )
	    + '-' + pad( date.getUTCDate() )
	    + 'T' + pad( date.getUTCHours() )
	    + ':' + pad( date.getUTCMinutes() )
	    + ':' + pad( date.getUTCSeconds() )
	    + '.' + String( (date.getUTCMilliseconds()/1000).toFixed(3) ).slice( 2, 5 )
	    + 'Z';
	};

	var lines = [];
	
	var getHeader = function(prefix, item){
		var header = [];
		var addHeader = function(key){
			if (prefix){
				header.push(prefix + "." + key);
			} else {
				header.push(key);
			}
		};

		for(var key in item){
			if (!item.hasOwnProperty(key)){
				continue;
			}
			if (item[key] instanceof Date){
				addHeader(key);
			} else if (typeof item[key] === 'object'){
				header.push(getHeader(key, item[key]));
			} else {
				addHeader(key);
			}			
		}
		return header.join(";");
	};
	
	var getRow = function(item){
		var values = [];
		for(var key in item){
			if (!item.hasOwnProperty(key)){
				continue;
			}
			if (item[key] instanceof Date){
				values.push(toIsoDateString(item[key]));
			} else if (typeof(item[key]) === 'object'){
				values.push(getRow(item[key]));
			} else {
				values.push(item[key]);
			}			
		}
		return values.join(";");
	};
	
	var headerPrinted = false;	

	list.forEach(function(item){
		if(!headerPrinted){
			lines.push(getHeader("", item));
			headerPrinted = true;
		}	
		lines.push(getRow(item));
	});
	
	return lines.join('\n');
}

//db = db.getSiblingDB("api-definition-external-test")

var c = db.api.aggregate([{
    $unwind: "$versions"
},{
    $match:{
        "versions.status" : {
            $nin : ["DEPRECATED", "RETIRED"]
        },
        "versions.access.type" : {
           $ne :  "PRIVATE"
        }
    }
},{
     $project: {
         _id: 0,
         serviceName : "$serviceName",
         name: "$name",
         description: "$description",
         version : "$versions.version"
     }
},{
    $group: {
        _id: {
            "serviceName" : "$serviceName",
            "name" : "$name",
            "description": "$description",
        },
        versions: {
            $max : "$version"
        }
    }
}

,{
 	 $sort: {
 		 "_id.serviceName": 1,
 	 }
  }
])

// c
print(toCsv(c))
