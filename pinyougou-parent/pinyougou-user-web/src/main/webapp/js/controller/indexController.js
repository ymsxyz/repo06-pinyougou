//品牌控制层 
app.controller('indexController', function($scope,loginService) {

	// 展示登录名
	$scope.showName = function() {
		// 切换页码
		loginService.showName().success(function(response) {
			$scope.loginName = response.loginName;
		});
	}
	
});