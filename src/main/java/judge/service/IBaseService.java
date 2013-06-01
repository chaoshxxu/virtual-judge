package judge.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

@SuppressWarnings("unchecked")
public interface IBaseService {
	/**
	 * 添加实体
	 * @param entity 实体的类
	 */
	public void addOrModify(Object entity);

	/**
	 * 删除实体
	 * @param entity
	 */
	public void delete(Object entity);

	/**
	 * 按照ID删除
	 * @param entityClass
	 * @param id
	 */
	public void delete(Class entityClass, Serializable id);

	public Object query(Class entityClass, Serializable id);

	public List query(String queryString);
	public List list(String queryString, int FirstResult, int MaxResult);
	public long count(String hql);

	public List query(String queryString, Map parMap) ;
	public List list(String hql, Map parMap, int FirstResult, int MaxResult);
	public long count(String hql, Map parMap);

	public void execute(String hql);
	public void execute(String hql, Map parMap);

	/**
	 * 从baseDao获取一个session，以方便完成原子操作
	 * @return session
	 */
	public Session getSession();

	/**
	 * 释放getSession()获取的session
	 * @param session
	 */
	public void releaseSession(Session session);

}
