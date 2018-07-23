package com.atguigu.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atguigu.utils.VerifyCodeConfig;

import redis.clients.jedis.Jedis;

//验证手机验证码
public class CodeVerifyServlet extends HttpServlet {
	
 
	private static final long serialVersionUID = 1L;
       
     /*
      *  3. 输入验证码，点击验证，返回成功或失败
	  	 		思路： ①接受前台传入的参数：
	  	 				phone_no: 手机号
	  	 				verify_code： 验证码
	  	 			②判空
	  	 			③获取jedis，
	  	 				key: phone_no
	  	 			String	Code_database=jedis.get(phone_no);
	  	 			Code_database比较verify_code
	  	 			④如果验证成功，相应true
      */
    public CodeVerifyServlet() {
        
    }

 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
			//获取页面提交的参数
		String phone_no = request.getParameter("phone_no");
		String code_front = request.getParameter("verify_code");
		
		if (phone_no==null || code_front==null || phone_no.equals("") || code_front.equals("")) {
			
			System.out.println("非法的数据！");
			return ;
		}
		
		//生成key:
		 String phone_key=VerifyCodeConfig.PHONE_PREFIX+phone_no+VerifyCodeConfig.PHONE_SUFFIX;
		 
		 Jedis jedis = new Jedis(VerifyCodeConfig.HOST, VerifyCodeConfig.PORT);
		 
		 String code_back = jedis.get(phone_key);
		 
		 if (code_back.equals(code_front)) {
			
			 System.out.println(phone_no+" 验证成功！");
			 
			 jedis.close();
			 
			 response.getWriter().print(true);
		}
	}

}
