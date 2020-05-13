// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::redacap::data::service::RedcapDataService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.redacap.data.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import bw.ub.ehealth.dhislink.redacap.data.RedcapData;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;

/**
 * @see bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService
 */
 @Transactional(propagation = Propagation.REQUIRED, readOnly=false)
@Service("redcapDataService")
public class RedcapDataServiceImpl
    extends RedcapDataServiceBase
{

    /**
     * @see bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService#searchByCriteria(RedcapDataSearchCriteria)
     */
    @Override
    protected  Collection<RedcapDataVO> handleSearchByCriteria(RedcapDataSearchCriteria searchCriteria)
        throws Exception
    {
    	
    	List<RedcapDataVO> vos = new ArrayList<RedcapDataVO>();

    	for(RedcapData e : getRedcapDataDao().findByCriteria(searchCriteria)) {
    		vos.add(getRedcapDataDao().toRedcapDataVO(e));
    	}
    	
    	return vos;
    }

	@Override
	protected RedcapDataVO handleSaveRedcapData(RedcapDataVO redcapDataVO) throws Exception {
		
		if(redcapDataVO.getEventId() == null) {
			redcapDataVO.setEventId(getRedcapDataDao().findMaxEvent(redcapDataVO.getProjectId()));
		}
		
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
    	criteria.setEventId(redcapDataVO.getEventId());
    	criteria.setFieldName(redcapDataVO.getFieldName());
    	criteria.setProjectId(redcapDataVO.getProjectId());
    	criteria.setRecord(redcapDataVO.getRecord());
    	
    	/// Try to find the data in redcap_data table
		System.out.println("Find this ........................... ");
    	List<RedcapData> dt = (List<RedcapData>) getRedcapDataDao().findByCriteria(criteria);    	
		RedcapData data = getRedcapDataDao().redcapDataVOToEntity(redcapDataVO);
		System.out.println("=========== Data is " + data.toString());
    	
    	if(dt == null || dt.size() == 0) /// The data does not exist
        {
            data = getRedcapDataDao().create(data);
        } else if(dt.size() == 1) { /// The data exists
        	getRedcapDataDao().update(data);
        } else { // Anything else
        	return null;
        }
		
		return getRedcapDataDao().toRedcapDataVO(data);
	}

	@Override
	protected Long handleFindMaxEvent(Long projectId) throws Exception {
		
		return getRedcapDataDao().findMaxEvent(projectId);
	}

}