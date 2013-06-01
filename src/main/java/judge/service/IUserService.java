// ========================================================================
// 文件名：IUserService.java
//
// 文件说明：
//     本文件主要定义用户管理模块的功能接口，本接口继承IBaseService。
//
// ========================================================================
package judge.service;

import judge.bean.User;

public interface IUserService extends IBaseService{
	public boolean checkUsername(String username);
	public boolean checkPassword(String username,String password);
	public User getByUsername(String username);
}
