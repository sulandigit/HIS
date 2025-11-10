/**
 * 数据验证(表单验证)
 * 来自 grace.hcoder.net 
 * 作者 hcoder 深海
 */
module.exports = {
	// 错误信息存储
	error:'',
	
	/**
	 * 数据验证主函数
	 * @param {Object} data - 需要验证的数据对象
	 * @param {Array} rule - 验证规则数组
	 * @returns {Boolean} 验证通过返回true,失败返回false
	 */
	check : function (data, rule){
		// 遍历所有验证规则
		for(var i = 0; i < rule.length; i++){
			// 验证规则的必要字段检查
			if (!rule[i].checkType){return true;}  // 没有验证类型,跳过
			if (!rule[i].name) {return true;}       // 没有字段名,跳过
			if (!rule[i].errorMsg) {return true;}   // 没有错误信息,跳过
			
			// 检查数据字段是否存在
			if (!data[rule[i].name]) {this.error = rule[i].errorMsg; return false;}
			
			// 根据不同的验证类型进行相应的验证
			switch (rule[i].checkType){
				// 字符串长度验证 checkRule格式: '最小长度,最大长度' 如 '6,20'
				case 'string':
					var reg = new RegExp('^.{' + rule[i].checkRule + '}$');
					if(!reg.test(data[rule[i].name])) {this.error = rule[i].errorMsg; return false;}
				break;
				// 整数验证 checkRule格式: 数字位数(不含符号) 如 '1,10'表示1-10位
				case 'int':
					var reg = new RegExp('^(-[1-9]|[1-9])[0-9]{' + rule[i].checkRule + '}$');
					if(!reg.test(data[rule[i].name])) {this.error = rule[i].errorMsg; return false;}
					break;
				break;
				// 数字范围验证(包含整数和浮点数) checkRule格式: '最小值,最大值' 如 '1,100'
				case 'between':
					// 先验证是否为数字
					if (!this.isNumber(data[rule[i].name])){
						this.error = rule[i].errorMsg;
						return false;
					}
					// 解析最小值和最大值
					var minMax = rule[i].checkRule.split(',');
					minMax[0] = Number(minMax[0]);
					minMax[1] = Number(minMax[1]);
					// 判断是否在范围内
					if (data[rule[i].name] > minMax[1] || data[rule[i].name] < minMax[0]) {
						this.error = rule[i].errorMsg;
						return false;
					}
				break;
				// 整数范围验证(betweenD - D代表Decimal整数) checkRule格式: '最小值,最大值'
				case 'betweenD':
					// 验证是否为1-2位整数(可带负号)
					var reg = /^-?[1-9][0-9]?$/;
					if (!reg.test(data[rule[i].name])) { this.error = rule[i].errorMsg; return false; }
					// 解析最小值和最大值
					var minMax = rule[i].checkRule.split(',');
					minMax[0] = Number(minMax[0]);
					minMax[1] = Number(minMax[1]);
					// 判断是否在范围内
					if (data[rule[i].name] > minMax[1] || data[rule[i].name] < minMax[0]) {
						this.error = rule[i].errorMsg;
						return false;
					}
				break;
				// 浮点数范围验证(betweenF - F代表Float浮点数) checkRule格式: '最小值,最大值'
				case 'betweenF': 
					// 验证是否为浮点数格式(可带负号)
					var reg = /^-?[0-9][0-9]?.+[0-9]+$/;
					if (!reg.test(data[rule[i].name])){this.error = rule[i].errorMsg; return false;}
					// 解析最小值和最大值
					var minMax = rule[i].checkRule.split(',');
					minMax[0] = Number(minMax[0]);
					minMax[1] = Number(minMax[1]);
					// 判断是否在范围内
					if (data[rule[i].name] > minMax[1] || data[rule[i].name] < minMax[0]) {
						this.error = rule[i].errorMsg;
						return false;
					}
				break;
				// 相等验证 checkRule为期望的值
				case 'same':
					if (data[rule[i].name] != rule[i].checkRule) { this.error = rule[i].errorMsg; return false;}
				break;
				// 不相等验证 checkRule为不期望的值
				case 'notsame':
					if (data[rule[i].name] == rule[i].checkRule) { this.error = rule[i].errorMsg; return false; }
				break;
				// 邮箱格式验证
				case 'email':
					var reg = /^\w+([-+.']\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
					if (!reg.test(data[rule[i].name])) { this.error = rule[i].errorMsg; return false; }
				break;
				// 手机号码验证(中国大陆11位手机号)
				case 'phoneno':
					var reg = /^1[0-9]{10,10}$/;
					if (!reg.test(data[rule[i].name])) { this.error = rule[i].errorMsg; return false; }
				break;
				// 邮政编码验证(6位数字)
				case 'zipcode':
					var reg = /^[0-9]{6}$/;
					if (!reg.test(data[rule[i].name])) { this.error = rule[i].errorMsg; return false; }
				break;
				// 自定义正则表达式验证 checkRule为正则表达式字符串
				case 'reg':
					var reg = new RegExp(rule[i].checkRule);
					if (!reg.test(data[rule[i].name])) { this.error = rule[i].errorMsg; return false; }
				break;
				// 枚举值验证 checkRule为包含所有允许值的数组或字符串
				case 'in':
					if(rule[i].checkRule.indexOf(data[rule[i].name]) == -1){
						this.error = rule[i].errorMsg; return false;
					}
				break;
				// 非空验证(不能为null或空字符串)
				case 'notnull':
					if(data[rule[i].name] == null || data[rule[i].name].length < 1){this.error = rule[i].errorMsg; return false;}
				break;
			}
		}
		// 所有验证规则通过
		return true;
	},
	
	/**
	 * 判断是否为数字(整数或浮点数)
	 * @param {*} checkVal - 需要验证的值
	 * @returns {Boolean} 是数字返回true,否则返回false
	 */
	isNumber : function (checkVal){
		// 匹配整数或浮点数(可带负号)
		var reg = /^-?[1-9][0-9]?.?[0-9]*$/;
		return reg.test(checkVal);
	}
}