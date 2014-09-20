package judge.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

@SuppressWarnings("unchecked")
public interface IBaseDao {
    public void addOrModify(Object entity);

    public void delete(Object entity);
    public void delete(Class entityClass, Serializable id);

    public Object query(Class entityClass, Serializable id);
    public List query(String hql);
    public List query(String queryString, int FirstResult, int MaxResult);
    public List query(String hql, Map parMap);
    public List query(String hql, Map parMap, int FirstResult, int MaxResult);

    public void execute(String statement);
    public void execute(String statement, Map parMap);

    public Session createSession();
    public void closeSession(Session session);
}
