// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::redacap::location::service::LocationService
 * STEREOTYPE:  Service
 * STEREOTYPE:  WebService
 */
package bw.ub.ehealth.dhislink.redacap.location.service;

import bw.ub.ehealth.dhislink.redacap.location.Location;
import bw.ub.ehealth.dhislink.redacap.location.vo.LocationVO;
import java.util.Collection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService
 */
 @Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("locationService")
public class LocationServiceImpl
    extends LocationServiceBase
{

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#createLocation(LocationVO)
     */
    @Override
    protected  LocationVO handleCreateLocation(LocationVO locationVO)
        throws Exception
    {
    	// If the id is not null, then delegate to the update method
    	if(locationVO.getId() != null) {
    		this.updateLocation(locationVO);
    		return locationVO;
    	}
    	
    	Location location = getLocationDao().locationVOToEntity(locationVO);
    	
    	location = getLocationDao().create(location);
    	return getLocationDao().toLocationVO(location);    	
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#updateLocation(LocationVO)
     */
    @Override
    protected  void handleUpdateLocation(LocationVO locationVO)
        throws Exception
    {
    	System.out.println("=================== " + locationVO.toString());
    	if(locationVO.getId() != null) {
    		getLocationDao().update(getLocationDao().locationVOToEntity(locationVO));
    	}
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#deleteLocation(Long)
     */
    @Override
    protected  void handleDeleteLocation(Long id)
        throws Exception
    {
    	this.getLocationDao().remove(id);
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#findById(Long)
     */
    @Override
    protected  LocationVO handleFindById(Long id)
        throws Exception
    {
    	return getLocationDao().toLocationVO(getLocationDao().load(id));
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#searchByName(String)
     */
    @Override
    protected  Collection<LocationVO> handleSearchByName(String name)
        throws Exception
    {
    	return getLocationDao().toLocationVOCollection(getLocationDao().searchByName(name));
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#findAll()
     */
    @Override
    protected  Collection<LocationVO> handleFindAll()
        throws Exception
    {
        return getLocationDao().toLocationVOCollection(getLocationDao().loadAll());
    }

}