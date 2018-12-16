//登录服务层
app.service('loginService',function($http){
	    	
	//获取登录服务层人名
	this.loginName=function(){
		return $http.get('../login/name.do');		
	}
	
});
