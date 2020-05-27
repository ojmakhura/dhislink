// Generated by hibernate/SpringHibernateDaoImpl.vsl in andromda-spring-cartridge on 05/25/2020 23:03:05+0200.
// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package bw.ub.ehealth.dhislink.redacap.auth;

import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthVO;
import bw.ub.ehealth.dhislink.security.DhislinkPasswordEncoder;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @see RedcapAuth
 */
@Repository("redcapAuthDao")
public class RedcapAuthDaoImpl
    extends RedcapAuthDaoBase
{
	@Autowired
	private DhislinkPasswordEncoder encoder;
    /**
     * {@inheritDoc}
     */
    @Override
    protected String handleFindUsernameSalt(String username)
    {
    	String queryStr = "select ra.passwordSalt from RedcapAuth ra where username = :username";
    	Query query = entityManager.createQuery(queryStr);
    	query.setParameter("username", username);
    	
    	try {
			return (String) query.getSingleResult();
		} catch(NoResultException | NonUniqueResultException e) {
			logger.error(e.getMessage());
			return null;
		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RedcapAuth handleUserAuthentication(String username, String password)
    {
    	String salt = this.findUsernameSalt(username);
    	logger.info(String.format("The salt for user %s is %s", username, salt));
    	String queryStr = "select ra from bw.ub.ehealth.dhislink.redacap.auth.RedcapAuth ra "
    			+ "where username = :username and password = :password";
    	Query query = entityManager.createQuery(queryStr);
    	query.setParameter("username", username);
    	logger.info(String.format("Encoded passowrd for %s is %s", username, encoder.encode(password + salt)));
    	query.setParameter("password", encoder.encode(password + salt));

    	try {
			return (RedcapAuth) query.getSingleResult();
		} catch(NoResultException | NonUniqueResultException e) {
			logger.error(e.getMessage());
			return null;
		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toRedcapAuthVO(
        RedcapAuth source,
        RedcapAuthVO target)
    {
        // TODO verify behavior of toRedcapAuthVO
        super.toRedcapAuthVO(source, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RedcapAuthVO toRedcapAuthVO(final RedcapAuth entity)
    {
        // TODO verify behavior of toRedcapAuthVO
        return super.toRedcapAuthVO(entity);
    }

    /**
     * Retrieves the entity object that is associated with the specified value object
     * from the object store. If no such entity object exists in the object store,
     * a new, blank entity is created
     */
    private RedcapAuth loadRedcapAuthFromRedcapAuthVO(RedcapAuthVO redcapAuthVO)
    {
    	RedcapAuth auth = this.searchUniqueUsername(redcapAuthVO.getUsername());
    	
    	if(auth == null) {
    		auth = RedcapAuth.Factory.newInstance();
    	}
    	
    	return auth;
    }

    /**
     * {@inheritDoc}
     */
    public RedcapAuth redcapAuthVOToEntity(RedcapAuthVO redcapAuthVO)
    {
        // TODO verify behavior of redcapAuthVOToEntity
        RedcapAuth entity = this.loadRedcapAuthFromRedcapAuthVO(redcapAuthVO);
        this.redcapAuthVOToEntity(redcapAuthVO, entity, true);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redcapAuthVOToEntity(
        RedcapAuthVO source,
        RedcapAuth target,
        boolean copyIfNull)
    {
        // TODO verify behavior of redcapAuthVOToEntity
        super.redcapAuthVOToEntity(source, target, copyIfNull);
    }
}