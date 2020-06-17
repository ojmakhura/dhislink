// Generated by hibernate/SpringHibernateDaoImpl.vsl in andromda-spring-cartridge on 05/26/2020 14:22:31+0200.
// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package bw.ub.ehealth.dhislink.redacap.location;

import bw.ub.ehealth.dhislink.redacap.location.vo.LocationVO;

import java.util.Collection;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

/**
 * @see Location
 */
@Repository("locationDao")
public class LocationDaoImpl
    extends LocationDaoBase
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void toLocationVO(
        Location source,
        LocationVO target)
    {
        // TODO verify behavior of toLocationVO
        super.toLocationVO(source, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocationVO toLocationVO(final Location entity)
    {
        // TODO verify behavior of toLocationVO
        return super.toLocationVO(entity);
    }

    /**
     * Retrieves the entity object that is associated with the specified value object
     * from the object store. If no such entity object exists in the object store,
     * a new, blank entity is created
     */
    private Location loadLocationFromLocationVO(LocationVO locationVO)
    {
        if (locationVO.getId() == null)
        {
            return  Location.Factory.newInstance();
        }
        else
        {
            return this.load(locationVO.getId());
        }
        
    }

    /**
     * {@inheritDoc}
     */
    public Location locationVOToEntity(LocationVO locationVO)
    {
        // TODO verify behavior of locationVOToEntity
        Location entity = this.loadLocationFromLocationVO(locationVO);
        this.locationVOToEntity(locationVO, entity, true);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void locationVOToEntity(
        LocationVO source,
        Location target,
        boolean copyIfNull)
    {
        // TODO verify behavior of locationVOToEntity
        super.locationVOToEntity(source, target, copyIfNull);
    }

	@Override
	protected Collection<Location> handleSearchByName(String name) throws Exception {
		String queryStr = "select location from Location location where name like :name or code like :name";
    	Query query = entityManager.createQuery(queryStr);
    	query.setParameter("name", "%" + name + "%");
		// TODO Auto-generated method stub
		return query.getResultList();
	}
}