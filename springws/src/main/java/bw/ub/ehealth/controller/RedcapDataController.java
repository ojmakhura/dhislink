package bw.ub.ehealth.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.redacap.data.vo.BatchItemVO;
import bw.ub.ehealth.dhislink.redacap.data.vo.BatchVO;
import bw.ub.ehealth.dhislink.redacap.data.vo.InstrumentBatchVO;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;
import bw.ub.ehealth.dhislink.specimen.service.SpecimenService;
import bw.ub.ehealth.dhislink.specimen.vo.SpecimenVO;
import bw.ub.ehealth.dhislink.vo.BatchSearchCriteria;

@RestController
@RequestMapping("/ddpcontroller/data")
public class RedcapDataController {

	private static final Logger logger = LoggerFactory.getLogger(RedcapDataController.class);

    @Value("${lab.report.pid}")
    private Long labReportPID;

    @Value("${lab.reception.pid}")
    private Long labReceptionPID;

    @Value("${lab.extraction.pid}")
    private Long labExtractionPID;

    @Value("${lab.resulting.pid}")
    private Long labResultingPID;
    
	@Autowired
	private RedcapDataService redcapDataService;
	
	@Autowired
	private SpecimenService specimenService;
	
	@Autowired
	private RedcapLink redcapLink;
	
	/**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.saveRedcapData
     * @param redcapDataVO TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.saveRedcapData(redcapDataVO)
     * @return RedcapDataVO
     */    
    @PostMapping("/saveone")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public RedcapDataVO saveRedcapData(RedcapDataVO redcapDataVO) {
    	
    	return redcapDataService.saveRedcapData(redcapDataVO);
    }
    
    @PostMapping("/saveall")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<RedcapDataVO> saveRedcapData(@RequestBody Collection<RedcapDataVO> data, @RequestBody Long projectId) {
    	
    	if(projectId == labReportPID) {
    		redcapLink.doPostRedcapData((List<RedcapDataVO>) data, "redcap.lab.report.token");
    	} else if(projectId == labExtractionPID) {
    		redcapLink.doPostRedcapData((List<RedcapDataVO>) data, "redcap.lab.extraction.token");
    	} else if(projectId == labResultingPID) {
    		redcapLink.doPostRedcapData((List<RedcapDataVO>) data, "redcap.lab.resulting.token");
    	}
     	
    	return data;
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.searchByCriteria
     * @param searchCriteria TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.searchByCriteria(searchCriteria)
     * @return Collection<RedcapDataVO>
     */
    @PostMapping("/search")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<RedcapDataVO> searchByCriteria(@RequestBody RedcapDataSearchCriteria searchCriteria) {
    	
    	return redcapDataService.searchByCriteria(searchCriteria);
    }
    
    /**
     * Create a map of the RedcapDataVO using the batch ids as the keys
     * 
     * @param data
     * @return
     */
    Map<String, List<RedcapDataVO>> getRedcapDataBatchMap(Collection<RedcapDataVO> data) {
    	
    	Map<String, List<RedcapDataVO>> map = new HashMap<String, List<RedcapDataVO>>();
    	List<RedcapDataVO> batchIds = new ArrayList<RedcapDataVO>();
    	
    	// First find the batch ids
    	for(RedcapDataVO d : data) {
    		if(d.getFieldName().equals("test_det_batch_id")) {
    			batchIds.add(d);
    		}    			
    	}
    	
    	for(RedcapDataVO d : batchIds) {
    		map.put(d.getValue(), new ArrayList<RedcapDataVO>());
    		for(RedcapDataVO dt : data) {
        		if(d.getEventId() == dt.getEventId() &&
        				d.getProjectId() == dt.getProjectId() &&
        				d.getRecord().equals(dt.getRecord())) {
        			
        			map.get(d.getValue()).add(dt);
        		}    			
        	}
    	}
    	
    	return map;
    }
    
    private BatchVO getBatchFromRedcapData(List<RedcapDataVO> data) {
    	BatchVO batch = new BatchVO();
    	List<RedcapDataVO> t2 = new ArrayList<RedcapDataVO>();
    	
    	for(RedcapDataVO rd : data) {
    		if(rd.getFieldName().equals("test_det_batch_id")) {
    			batch.setBatchId(rd.getValue());
    		} else if(rd.getFieldName().equals("test_det_batchsize")) {
    			batch.setDetectionSize(Long.parseLong(rd.getValue()));
    			
    		} else if(rd.getFieldName().equals("test_det_datetime")) {
    			
    			SimpleDateFormat format = new SimpleDateFormat("dd-mm-yyyy HH:mm");    			
    			try {
					batch.setDetectionDateTime(format.parse(rd.getValue()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		} else if(rd.getFieldName().equals("test_assay_datetime")) {
    			
    			SimpleDateFormat format = new SimpleDateFormat("dd-mm-yyyy HH:mm");    			
    			try {
					batch.setDetectionDateTime(format.parse(rd.getValue()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		} else if(rd.getFieldName().equals("test_verify_datetime")) {
    			
    			SimpleDateFormat format = new SimpleDateFormat("dd-mm-yyyy HH:mm");    			
    			try {
					batch.setDetectionDateTime(format.parse(rd.getValue()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		} else if(rd.getFieldName().equals("detection_lab")) {
    			
    			batch.setLab(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("testing_detection_complete")) {
    			
    			batch.setDetectionStatus(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("resulting_complete")) {
    			
    			batch.setResultingStatus(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("verification_complete")) {
    			
    			batch.setVerificationStatus(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_det_personnel")) {
    			
    			batch.setDetectionPersonnel(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_assay_personnel")) {
    			
    			batch.setResultingPersonnel(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_verify_personnel")) {
    			
    			batch.setVerificationPersonnel(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_verify_personnel")) {
    			
    			batch.setVerificationPersonnel(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_det_instrument")) {
    			
    			InstrumentBatchVO ib = new InstrumentBatchVO();
    			ib.setInstrument(rd.getValue());
    			batch.setDetectionBatch1(ib);
    			
    		} else if(rd.getFieldName().contains("test_det_barcode_")) {
    			
    			t2.add(rd);
    		}
    	}
    	
    	batch.getDetectionBatch1().setInstrumentBatchSize((long)t2.size());
    	batch.getDetectionBatch1().setBatchItems(new ArrayList<BatchItemVO>());
    	
    	ArrayList<BatchItemVO> items = (ArrayList<BatchItemVO>) batch.getDetectionBatch1().getBatchItems();
    	
    	// Get the specimen information
    	for(RedcapDataVO rd : t2) {
    		
    		String pos = rd.getValue().substring(17);
    		int idx = Integer.parseInt(pos) - 1;
    		BatchItemVO item = new BatchItemVO();
    		
    		SpecimenVO specimen = specimenService.findSpecimenByBarcode(rd.getValue());
    		    		
    		Calendar cal = Calendar.getInstance();
			cal.setTime(specimen.getPatient().getDateOfBirth());
			
			int year = cal.get(Calendar.YEAR);
			int month =  cal.get(Calendar.MONTH);
			
			if(month == 0 || month > 12) {
				month = 1;
			}
			
			int day = cal.get(Calendar.DATE);
			
			if(day == 0 || day > 31) {
				day = 1;
			}
			
			LocalDate d1 = LocalDate.of(year, month, day);
			LocalDate now = LocalDate.now();
			
			Period diff = Period.between(d1, now);
			item.setPatientAge((long)diff.getYears());
			
			item.setPatientId(specimen.getPatient().getIdentityNo());
			item.setPatientName(specimen.getPatient().getFirstName());
			item.setPatientSex(specimen.getPatient().getSex());
			item.setPatientSurname(specimen.getPatient().getSurname());
			item.setSpecimenBarcode(rd.getValue());
    		
    		items.set(idx, item);
    	}
    	
    	return batch;
    }
    
    @PostMapping("/search/batch")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public List<BatchVO> searchBatches(@RequestBody BatchSearchCriteria searchCriteria) {
    	
    	List<BatchVO> batches = new ArrayList<BatchVO>();
    	
    	RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
    	criteria.setProjectId(labResultingPID);
    	
    	if(!StringUtils.isBlank(searchCriteria.getBatchId())) {
    		criteria.setFieldName("test_det_batch_id");
    		criteria.setValue(searchCriteria.getBatchId());
    		
    		Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);
    		
    		// Find all the other fields    		
    		if(tmp != null && tmp.size() > 0) {
    			
    			RedcapDataVO rd = tmp.iterator().next();
    			criteria = new RedcapDataSearchCriteria();
    			criteria.setEventId(rd.getEventId());
    			criteria.setProjectId(rd.getProjectId());
    			criteria.setRecord(rd.getRecord());
    			
    			tmp = redcapDataService.searchByCriteria(criteria);
    			Map<String, List<RedcapDataVO>> map = getRedcapDataBatchMap(tmp);
        		batches.add(getBatchFromRedcapData(map.get(searchCriteria.getBatchId())));
    		}
    		
    	} else if(!StringUtils.isBlank(searchCriteria.getSpecimenBarcode())) {
    		criteria.setFieldName("test_det_barcode_%");
    		criteria.setValue(searchCriteria.getSpecimenBarcode());
    		
    		Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);
    		
    		if(tmp != null && tmp.size() > 0) {
    			
    			RedcapDataVO rd = tmp.iterator().next();
    			criteria = new RedcapDataSearchCriteria();
    			criteria.setEventId(rd.getEventId());
    			criteria.setProjectId(rd.getProjectId());
    			criteria.setRecord(rd.getRecord());
    			
    			tmp = redcapDataService.searchByCriteria(criteria);    			
    			Map<String, List<RedcapDataVO>> map = getRedcapDataBatchMap(tmp);
    			
    			for(Map.Entry<String, List<RedcapDataVO>> e : map.entrySet()) {
    				batches.add(getBatchFromRedcapData(e.getValue()));
    			}
    		}
    		
    	} else if(!StringUtils.isBlank(searchCriteria.getLab())) {
    		criteria.setFieldName("detection_lab");
    		criteria.setValue(searchCriteria.getLab());
    		Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);
    		
    		for(RedcapDataVO rd : tmp) {
    			criteria = new RedcapDataSearchCriteria();
    			criteria.setEventId(rd.getEventId());
    			criteria.setProjectId(rd.getProjectId());
    			criteria.setRecord(rd.getRecord());
    			
    			tmp = redcapDataService.searchByCriteria(criteria);    			
    			Map<String, List<RedcapDataVO>> map = getRedcapDataBatchMap(tmp);
    			
    			for(Map.Entry<String, List<RedcapDataVO>> e : map.entrySet()) {
    				batches.add(getBatchFromRedcapData(e.getValue()));
    			}
    		}
    	}
    	
    	return batches;    	
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.findMaxEvent
     * @param projectId TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.findMaxEvent(projectId)
     * @return Long
     */
    public Long findMaxEvent(Long projectId) {
    	
    	return redcapDataService.findMaxEvent(projectId);
    }
}
