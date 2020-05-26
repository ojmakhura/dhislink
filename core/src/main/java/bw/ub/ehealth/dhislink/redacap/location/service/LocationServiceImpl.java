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
        // TODO implement protected  LocationVO handleCreateLocation(LocationVO locationVO)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.location.service.LocationService.handleCreateLocation(LocationVO locationVO) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#updateLocation(LocationVO)
     */
    @Override
    protected  void handleUpdateLocation(LocationVO locationVO)
        throws Exception
    {
        // TODO implement protected  void handleUpdateLocation(LocationVO locationVO)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.location.service.LocationService.handleUpdateLocation(LocationVO locationVO) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#deleteLocation(Long)
     */
    @Override
    protected  void handleDeleteLocation(Long id)
        throws Exception
    {
        // TODO implement protected  void handleDeleteLocation(Long id)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.location.service.LocationService.handleDeleteLocation(Long id) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#findById(Long)
     */
    @Override
    protected  LocationVO handleFindById(Long id)
        throws Exception
    {
        // TODO implement protected  LocationVO handleFindById(Long id)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.location.service.LocationService.handleFindById(Long id) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#searchByName(String)
     */
    @Override
    protected  Collection<LocationVO> handleSearchByName(String name)
        throws Exception
    {
        // TODO implement protected  Collection<LocationVO> handleSearchByName(String name)
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.location.service.LocationService.handleSearchByName(String name) Not implemented!");
    }

    /**
     * @see bw.ub.ehealth.dhislink.redacap.location.service.LocationService#findAll()
     */
    @Override
    protected  Collection<LocationVO> handleFindAll()
        throws Exception
    {
        // TODO implement protected  Collection<LocationVO> handleFindAll()
        throw new UnsupportedOperationException("bw.ub.ehealth.dhislink.redacap.location.service.LocationService.handleFindAll() Not implemented!");
    }

}