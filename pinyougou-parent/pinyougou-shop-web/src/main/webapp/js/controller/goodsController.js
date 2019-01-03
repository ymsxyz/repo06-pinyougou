//控制层 $location.search()['id']获取url的请求参数值
app.controller('goodsController', function($scope, $controller,$location,goodsService,
		uploadService, itemCatService, typeTemplateService) {

	$controller('baseController', {$scope : $scope});// 继承

	// 读取列表数据绑定到表单中
	$scope.findAll = function() {
		goodsService.findAll().success(function(response) {
			$scope.list = response;
		});
	}
	
	//定义status数组,页面状态友好显示
	$scope.status=['未审核','已审核','审核未通过','关闭'];
	
	// 分页
	$scope.findPage = function(page, rows) {
		goodsService.findPage(page, rows).success(function(response) {
			$scope.list = response.rows;
			$scope.paginationConf.totalItems = response.total;// 更新总记录数
		});
	}

	// 查询实体
	$scope.findOne = function(id) {
		goodsService.findOne(id).success(function(response) {
			$scope.entity = response;
		});
	}

	// 新增
	$scope.add = function() {
		$scope.entity.goodsDesc.introduction = editor.html();// 提交富文本信息
		goodsService.add($scope.entity).success(function(response) {

			if (response.success) {

				// 成功清空页面
				$scope.entity = {};
				editor.html("");// 清空
			} else {
				alert(response.message);
			}
		});
	}

	// 批量删除
	$scope.dele = function() {
		// 获取选中的复选框
		goodsService.dele($scope.selectIds).success(function(response) {
			if (response.success) {
				$scope.reloadList();// 刷新列表
				$scope.selectIds = [];
			}
		});
	}

	$scope.searchEntity = {};// 定义搜索对象

	// 搜索:bug全是模糊查询
	$scope.search = function(page, rows) {
		goodsService.search(page, rows, $scope.searchEntity).success(
				function(response) {
					$scope.list = response.rows;
					$scope.paginationConf.totalItems = response.total;// 更新总记录数
				});
	}
	/** * 上传图片 */
	$scope.uploadFile = function() {
		uploadService.uploadFile().success(function(response) {
			// alert(response.message);
			if (response.success) {// 如果上传成功，取出 url
				$scope.image_entity.url = response.message;// 设置文件地址
				// alert(response.message);
				// alert($scope.image_entity.url);
			} else {
				alert(response.message);
			}
		}).error(function() {
			alert("上传发生错误");
		});
	}
	// 定义组合实体类:Goods
	$scope.entity = {
		goods : {},
		goodsDesc : {
			itemImages : [],
			specificationItems : []
		}
	};// 定义页面实体结构

	// 添加图片列表
	$scope.add_image_entity = function() {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	// 列表中移除图片
	$scope.remove_image_entity = function(index) {
		$scope.entity.goodsDesc.itemImages.splice(index, 1);
	}

	// 初始化变量

	// 读取一级目录
	$scope.selectItemCat1List = function() {
		// alert(parentId);
		itemCatService.findByParentId(0).success(function(response) {
			$scope.itemCat1List = response;//
		});
	}
	// 读取二级目录:监视entity.goods.category1Id值,作为参数
	$scope.$watch('entity.goods.category1Id', function(newValue, oldValue) {

		itemCatService.findByParentId(newValue).success(function(response) {
			$scope.itemCat2List = response;
		});
	});
	// 读取三级目录:监视entity.goods.category2Id值,作为参数
	$scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {

		itemCatService.findByParentId(newValue).success(function(response) {
			$scope.itemCat3List = response;
		});
	});

	// 读取itemCat表模板id赋值给goods表的模板id,参数为id
	$scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {

		itemCatService.findOne(newValue).success(function(response) {
			$scope.entity.goods.typeTemplateId = response.typeId;
		});
	});
	// 根据goods表typeTemplateId从typeTemplate表读brandsIds
	$scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {

		typeTemplateService.findOne(newValue).success(
				function(response) {
					$scope.typeTemplate = response;

					// [{{"id":15,"text":"飞利浦"},{"id":22,"text":"LG"}]后端传过来的字符串转json对象,需要faston依赖
					$scope.typeTemplate.brandIds = JSON
							.parse($scope.typeTemplate.brandIds);// 品牌列表
					
					//读取模板中的扩展属性赋给商品的扩展属性
					//如果url中没有 ID，则加载模板中的扩展数据,有则修改
					if($location.search()['id']==null){
						$scope.entity.goodsDesc.customAttributeItems = JSON
							.parse($scope.typeTemplate.customAttributeItems);
					}
					
				});

		/*
		 * 查询规格列表
		 * [{"id":27,"text":"网络","options":[{"id":98,"optionName":"移动3G","orders":1,"specId":27},
		 * {"id":99,"optionName":"移动4G","orders":2,"specId":27}]},{"id":32,"text":"机身内存","options":[]}]
		 */
		typeTemplateService.findSpecList(newValue).success(function(response) {
			$scope.specList = response;
		});
	});

	/*
	 * [{“attributeName”:”规格名称”,”attributeValue”:[“规格选项 1”,“规格选项 2”.... ] } ,
	 * .... ]
	 */
	$scope.updateSpecAttribute = function($event, name, value) {
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,
				'attributeName',name);
		/*
		 * specificationItems-->[{“attributeName”:”规格名称”,”attributeValue”:[“规格选项
		 * 1”,“规格选项 2”.... ] } , .... ] 'attributeName'-->'attributeName'
		 * name-->”规格名称”
		 */
		if (object != null) {
			if ($event.target.checked) {
				object.attributeValue.push(value);
			} else {// 取消勾选
				object.attributeValue.splice(object.attributeValue
						.indexOf(value), 1);// 移除选项
				// 如果选项都取消了，将此条记录移除
				if (object.attributeValue.length == 0) {
					$scope.entity.goodsDesc.specificationItems.splice(
							$scope.entity.goodsDesc.specificationItems
									.indexOf(object), 1);
				}
			}
		} else {
			$scope.entity.goodsDesc.specificationItems.push({
				"attributeName" : name,
				"attributeValue" : [ value ]
			});
		}
	}

	// 创建 SKU 列表
	$scope.createItemList = function() {
		$scope.entity.itemList = [ {
			spec : {},
			price : 0,
			num : 99999,
			status : '0',
			isDefault : '0'
		} ];// 初始
		
		/* 
		 * items---[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},
		 *{"attributeName":"屏幕尺寸","attributeValue":["5.5寸","5寸"]}]
		*/
		var items = $scope.entity.goodsDesc.specificationItems;
		for (var i = 0; i < items.length; i++) {
			$scope.entity.itemList = addColumn($scope.entity.itemList,
					items[i].attributeName, items[i].attributeValue);
		}
	}
	// 添加列值
	addColumn = function(list, columnName, conlumnValues) {
		var newList = [];// 新的集合
		for (var i = 0; i < list.length; i++) {
			var oldRow = list[i];
			for (var j = 0; j < conlumnValues.length; j++) {
				var newRow = JSON.parse(JSON.stringify(oldRow));// 深克隆
				newRow.spec[columnName] = conlumnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	//查询所有itemCat赋值给变量,用于友好显示页面三级分类字段
	$scope.itemCatList=[];
	
	$scope.findItemCatList = function() {
		
		itemCatService.findAll().success(function(response) {
			for(var i=0;i<response.length;i++){
				//将id与name的值建立对应关系
				$scope.itemCatList[response[i].id] = response[i].name;
			}
			
		});
	}
	
	/*
	 * 通过id查询Goods对象
	 * 通过id==null,新增;否则修改
	 */
	$scope.findOne = function(id) {
		
		//获取url中的参数
		var id=$location.search()['id'];
		goodsService.findOne(id).success(function(response) {
			//新增
			if(id==null){
				return;
			}
			//修改:赋值给html用
			$scope.entity= response;
			//富文本
			editor.html($scope.entity.goodsDesc.introduction);
			//显示图片属性
			$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
			//显示扩展属性:被覆盖,需修改方法-->$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
			$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
			//读取商品规格属性
			$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
			//SKU 列表规格列转换
			for( var i=0;i<$scope.entity.itemList.length;i++ ){
				$scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
			}
		});
	}
	
	//根据规格名称和选项名称返回是否被勾选
	//[{“attributeName”:”规格名称1”,”attributeValue”:[“规格选项 1(option1)”,“规格选项 2(option2)”.... ] } ,{“attributeName”:”规格名称2”,”attributeValue”:[“规格选项 1”,“规格选项 2”.... ] } .... ]
	////规格名,属性名:注意别注释到未完语句(无;或右括号)后
	$scope.checkAttributeValue=function(specName,optionName){
		//定义规格条目
		var items=$scope.entity.goodsDesc.specificationItems;
		//从集合中按照key查询对象,若不为空(有对应key,可能value不匹配),且有对应key-value都匹配,则baseController中定义的方法
		var object=$scope.searchObjectByKey(items,'attributeName',specName);
		
		if(object==null){
			return false;//对应checkbox复选框表示未勾选:ng-checked="false"
		}else{
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					location.href="goods.html";//跳转到商品列表
				}else{
					alert(response.message);
				}
			}		
		);				
	}
});
