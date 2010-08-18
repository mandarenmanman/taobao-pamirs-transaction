package com.taobao.pamirs.unittest.test;

public class UserCheck {
  public boolean checkUser(long userId,long productId){
	  return userId %10 == productId%10;
  }
}
