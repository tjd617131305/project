package com.atguigu.servlet;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;


//获取手机验证码的处理
public class CodeSenderServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
   /*
    * 1. 需求：
    * 	①输入手机号，点击发送后随机生成6位数字码，2分钟有效
		②输入验证码，点击验证，返回成功或失败
		③每个手机号每天只能输入3次
		
	  2.输入手机号，点击发送后随机生成6位数字码，2分钟有效
	  		①在页面输入手机号，点击发送验证码按钮，提交phone_no=xxx
	  		②在servlet中获取手机号：
	  				String phone_no=request.getParamter("phone_no");
	  	    ③ 为phone_no生成验证码
	  	    		String code=genCode(6);
	  	    ④将手机号验证码保存到redis：
	  	    		a) 获取连接 jedis
	  	    		b) key: 根据手机号生成
	  	    		   value: string
	  	    		   jedis.setex(key,120秒,code)
	  	    ⑤ 发送短信到指定的手机，告知其验证码
	  	    ⑥ 响应true
	  	    
	  4.  每个手机号每天只能输入3次:
	  			需要使用一个变量来统计每个手机号每天发送验证码的次数！
	  			在生成验证码前，判断一个手机号已经发送验证码的次数！
	  				次数：  
	  					count_key:  手机号生成
	  					value:  string
	  				在生成验证码之前，判断次数，获取jedis
	  					String count=jedis.get(count_key);
	  				判断：
	  					①null:   
	  						生成验证码，count_key的value ，生成count_key，设置为1：
	  								jedis.setex(count_key,一天，1)
	  				   ②不为null:
	  				   		count>=3 :  拒绝生成！
	  				   		count<3:  允许生成，jedis.incr(count_key)
	  			
	  	

    */
    public CodeSenderServlet() {
        
    }

	@SuppressWarnings("resource")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//①获取手机号
		 String phone_no = request.getParameter("phone_no");
		 
		 if (phone_no==null || phone_no.equals("")) {
			System.out.println("非法的手机号格式！");
			return ;
		}
		 
		 //生成计数器的key
		 String count_key=phone_no+VerifyCodeConfig.COUNT_SUFFIX;
		 
		 // 获取jedis
		 Jedis jedis = new Jedis(VerifyCodeConfig.HOST, VerifyCodeConfig.PORT);
		 
		 String count_str = jedis.get(count_key);
		 
		 if (count_str==null) {
			
			 jedis.setex(count_key, VerifyCodeConfig.SECONDS_PER_DAY, "1");
			 
		 }else {
			 
			 int count = Integer.parseInt(count_str);
			 
			 if (count>=3) {
				
				 System.out.println(phone_no+"已经超过3次！拒绝生成验证码！");
				 
				 jedis.close();
				 
				 return ;
			}else {
				
				jedis.incr(count_key);
				
			}
			 
		 }
		 
		 
		 
		 //生成验证码
		 String code = genCode(VerifyCodeConfig.CODE_LEN);
		 
		 //生成key
		 String phone_key=VerifyCodeConfig.PHONE_PREFIX+phone_no+VerifyCodeConfig.PHONE_SUFFIX;
		 
		 jedis.setex(phone_key, VerifyCodeConfig.CODE_TIMEOUT, code);
		 
		 //模拟向手机发送短信
		 System.out.println("尊敬的："+phone_no+"您的手机6位验证码是："+code+"！请勿告知他人！");
		 
		 System.out.println(phone_key+"======>"+code);
		 //响应true
		 jedis.close();
		 
		 response.getWriter().print(true);
		 
		
		
	} 
	
	
	//生成6位验证码
	private  String genCode(int len){
		 String code="";
		 for (int i = 0; i < len; i++) {
		     int rand=  new Random().nextInt(10);
		     code+=rand;
		 }
		 
		return code;
	}
	
	
 
}
