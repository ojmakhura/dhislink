// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 * TEMPLATE:    SpringServiceImpl.vsl in andromda-spring cartridge
 * MODEL CLASS: NewModel::bw.ub.ehealth.dhislink::redacap::data::service::RedcapDataService
 * STEREOTYPE:  Service
 */
package bw.ub.ehealth.dhislink.redacap.data.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import bw.ub.ehealth.dhislink.batch.BatchAuthorityStage;
import bw.ub.ehealth.dhislink.instrument.vo.InstrumentVO;
import bw.ub.ehealth.dhislink.location.vo.LocationVO;
import bw.ub.ehealth.dhislink.redacap.data.RedcapData;
import bw.ub.ehealth.dhislink.redacap.data.vo.BatchVO;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;
import bw.ub.ehealth.dhislink.specimen.vo.SpecimenVO;
import io.jsonwebtoken.lang.Collections;

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
    	List<RedcapData> entities = (List<RedcapData>) getRedcapDataDao().findByCriteria(searchCriteria);
    	
    	for(RedcapData e : entities) {
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
    	List<RedcapData> dt = (List<RedcapData>) getRedcapDataDao().findByCriteria(criteria);    	
		RedcapData data = getRedcapDataDao().redcapDataVOToEntity(redcapDataVO);
    	
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

	@Override
	protected Collection<RedcapDataVO> handleSearchRedcapData(Map fields) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	Map<String, RedcapData> getDataMap(Collection<RedcapData> data) {
		HashMap<String, RedcapData> map = new HashMap<String, RedcapData>();
		
		for(RedcapData d : data) {
			map.put(d.getFieldName(), d);
		}
		
		return map;
	}
	
	/**
     * Create a map of the RedcapDataVO using the batch ids as the keys
     * 
     * @param data
     * @return
     */
    private Map<String, List<RedcapDataVO>> getRedcapDataBatchMap(Collection<RedcapDataVO> data) {
    	
    	Map<String, List<RedcapDataVO>> map = new HashMap<String, List<RedcapDataVO>>();
    	
    	// First find the batch ids
    	for(RedcapDataVO d : data) {
    		List<RedcapDataVO> tmp = map.get(d.getRecord());
    		
    		if(tmp == null) {
    			tmp = new ArrayList<RedcapDataVO>();
    			map.put(d.getRecord(), tmp);
    		}
    		
    		tmp.add(d);
    	}
    	
    	return map;
    }

	@Override
	protected BatchVO handleFindBatch(String batchId, Long projectId, Boolean fetchSpecimen, BatchAuthorityStage stage) throws Exception {
		
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		criteria.setRecord(batchId);
		criteria.setProjectId(projectId);
		
		Collection<RedcapData> redcapData = getRedcapDataDao().findByCriteria(criteria);
		
		if(Collections.isEmpty(redcapData)) {
			return null;
		}
		
		Map<String, RedcapData> map = this.getDataMap(redcapData);
		BatchVO batch = new BatchVO();
		batch.setAssayBatchId(batchId);
		batch.setBatchId(batchId);
				
		if(stage == BatchAuthorityStage.RESULTING && map.containsKey("test_assay_batchsize")) {
			Long size = Long.parseLong(map.get("test_assay_batchsize").getValue());
			batch.setInstrumentBatchSize(size);
			
			if(map.containsKey("test_assay_datetime")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				try {
					Date dt = format.parse(map.get("test_assay_datetime").getValue());
					format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
					batch.setResultingDateTime(format.format(dt));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
			if(map.containsKey("test_assay_personnel")) {
				batch.setResultingPersonnel(map.get("test_assay_personnel").getValue());
			}
			
			if(map.containsKey("resulting_complete")) {
				batch.setResultingStatus(map.get("resulting_complete").getValue());
			}
		}
		
		if(stage == BatchAuthorityStage.DETECTION && map.containsKey("test_det_id")) {
			Long size = Long.parseLong(map.get("test_det_batchsize").getValue());
			batch.setDetectionSize(size);
			batch.setDetectionBatchId(batchId);
			
			if(map.containsKey("test_det_datetime")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				try {
					Date dt = format.parse(map.get("test_det_datetime").getValue());
					format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
					batch.setDetectionDateTime(format.format(dt));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
			if(map.containsKey("test_det_personnel")) {
				batch.setDetectionPersonnel(map.get("test_det_personnel").getValue());
			}
			
			if(map.containsKey("test_det_instrument")) {
				
				InstrumentVO instrument = new InstrumentVO(map.get("test_det_personnel").getValue(), "");
				batch.setInstrument(instrument);
			}

			if(map.containsKey("testing_detection_complete")) {
				batch.setDetectionStatus(map.get("testing_detection_complete").getValue());
			}

			if(map.containsKey("detection_lab")) {
				LocationVO loc = getLocationDao().toLocationVO(getLocationDao().searchUniqueCode(map.get("detection_lab").getValue()));
				batch.setLab(loc);
			}
			
		}
		
		if(stage == BatchAuthorityStage.AUTHORISATION && map.containsKey("test_verify_batch_id")) {
			Long size = Long.parseLong(map.get("test_verify_batchsize").getValue());
			batch.setDetectionSize(size);
			batch.setDetectionBatchId(batchId);
			
			if(map.containsKey("test_verify_batch_id")) {
				batch.setVerifyBatchId(batchId);
			}
			
			if(map.containsKey("test_verify_datetime")) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				try {
					Date dt = format.parse(map.get("test_verify_datetime").getValue());
					format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
					batch.setDetectionDateTime(format.format(dt));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
			if(map.containsKey("test_verify_personnel")) {
				batch.setDetectionPersonnel(map.get("test_verify_personnel").getValue());
			}
			
			if(map.containsKey("verification_complete")) {
				batch.setVerificationStatus(map.get("verification_complete").getValue());
			}
		}
		
		return batch;
	}

	@Override
	protected Collection<SpecimenVO> handleFindBatchSpecimen(String batchId, BatchAuthorityStage stage)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}