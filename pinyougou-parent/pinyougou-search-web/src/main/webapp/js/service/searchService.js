app.service("searchService", function($http) {

	// 复制域查询
	this.search = function(searchMap) {
		return $http.post("itemSearch/search.do", searchMap);
	}

});