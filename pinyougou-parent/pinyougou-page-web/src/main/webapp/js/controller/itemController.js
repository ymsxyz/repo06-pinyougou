//控制层
app.controller('itemController',function($scope,$controller){	
	
	//记录用户选择的规格
	$scope.specificationItems={};
	
	 //加入购物车数量
	$scope.addNum=function(n){
		
		$scope.num=$scope.num+n;
		
		//如果小于1,让其=1
		if($scope.num<1){
			$scope.num=1;
		}
	} 
	
	/*
	*记录用户选择的规格赋值
	*@Param 规格:名attributeName,值attributeValue
	*/
	$scope.selectSpecification=function(attributeName,attributeValue){
		
		$scope.specificationItems[attributeName]=attributeValue;
		//用户选择规格后触发
		searchSku();
	}
	
	//判断规格是否被选中
	$scope.isSelected=function(attributeName,attributeValue){
		if($scope.specificationItems[attributeName]==attributeValue){
			
			return true;
		}else{
			return false;
		}
	}
	
	$scope.sku={};//当前选择的SKU
	
	//加载默认SKU
	$scope.loadSku=function(){
		
		$scope.sku=skuList[0];
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec));//深客隆
	}
	
	//匹配两个对象是否相等
	matchObject=function(map1,map2){
		//判断map2中包含map1所有对象
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}
		}
		//反之
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}			
		}		
		return true;
		
	}
	
	//根据规格查询sku
	searchSku=function(){
		
		for(var i=0;i<skuList.length;i++){
			//遍历规格集合判断选中的规格
			 if(matchObject( skuList[i].spec ,$scope.specificationItems)){
				 $scope.sku=skuList[i];
				 return ;
			 }			
		}
		//无匹配时的显示
		$scope.sku={id:0,title:'-----',price:0};
	}
	
	//添加商品到购物车
	$scope.addToCart=function(){
		alert('skuid:'+$scope.sku.id);
	}
	
})	