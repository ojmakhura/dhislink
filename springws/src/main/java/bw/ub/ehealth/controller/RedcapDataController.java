package bw.ub.ehealth.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.patient.vo.PatientVO;
import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.redacap.data.vo.BatchVO;
import bw.ub.ehealth.dhislink.redacap.data.vo.InstrumentVO;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;
import bw.ub.ehealth.dhislink.redacap.location.service.LocationService;
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
	private LocationService locationService;
	
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
    
    @PostMapping("/savebatch")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public void saveBatch(@RequestBody BatchVO batch, @RequestBody Long projectId) {
    	
    	logger.info("Saving " + batch.toString() + " for project " + projectId);
    }
    
    @GetMapping("/extraction/specimen/{batchId}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<SpecimenVO> fetchExtractionBatchSpecimen(@PathVariable @NotNull String batchId) {
    	
    	Collection<SpecimenVO> specimens = new ArrayList<SpecimenVO>();
    	RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
    	criteria.setProjectId(labExtractionPID);    	
    	criteria.setFieldName("test_ext_barcode_%");
    	criteria.setRecord(batchId);
    	
    	Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);
    	
    	for(RedcapDataVO rd : tmp) {
    		SpecimenVO specimen = specimenService.findSpecimenByBarcode(rd.getValue());
    		
    		if(specimen == null) {
    			specimen = new SpecimenVO();
    			specimen.setSpecimenBarcode(rd.getValue());
    			specimen.setPatient(new PatientVO());
    		}
    		
    		specimens.add(specimen);
    	}
    	
    	return specimens;
    }
    
    @PostMapping("/saveall")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<RedcapDataVO> saveRedcapData(@RequestBody Collection<RedcapDataVO> data, @RequestBody Long projectId) {
    	
    	logger.info("Saving " + data.toString());
    	
    	if(projectId == labReportPID) {
    		redcapLink.doPostRedcapData((List<RedcapDataVO>) data, "redcap.lab.report.token");
    	} else if(projectId == labExtractionPID) {
    		redcapLink.doPostRedcapData((List<RedcapDataVO>) data, "redcap.lab.extraction.token");
    	} else if(projectId == labResultingPID) {
    		redcapLink.doPostRedcapData((List<RedcapDataVO>) data, "redcap.lab.resulting.token");
    	} else if(projectId == labReceptionPID) {
    		redcapLink.doPostRedcapData((List<RedcapDataVO>) data, "redcap.lab.reception.token");
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
    private Map<String, List<RedcapDataVO>> getRedcapDataBatchMap(Collection<RedcapDataVO> data) {
    	
    	Map<String, List<RedcapDataVO>> map = new HashMap<String, List<RedcapDataVO>>();
    	List<RedcapDataVO> batchIds = new ArrayList<RedcapDataVO>();
    	
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
    
    private BatchVO getBatchFromRedcapData(List<RedcapDataVO> data) {
    	BatchVO batch = new BatchVO();
    	List<RedcapDataVO> t2 = new ArrayList<RedcapDataVO>();
    	
    	batch.setBatchId(data.get(0).getRecord());
    	
    	for(RedcapDataVO rd : data) {
    		if(rd.getFieldName().equals("test_det_batch_id")) {
    			batch.setBatchId(rd.getValue());
    		} else if(rd.getFieldName().equals("test_det_batchsize")) {
    			batch.setDetectionSize(Long.parseLong(rd.getValue()));
    			
    		} else if(rd.getFieldName().equals("test_det_datetime")) {
    			
    			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");    			
    			try {
					batch.setDetectionDateTime(format.parse(rd.getValue()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		} else if(rd.getFieldName().equals("test_assay_datetime")) {
    			
    			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");    			
    			try {
					batch.setResultingDateTime(format.parse(rd.getValue()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		} else if(rd.getFieldName().equals("test_verify_datetime")) {
    			
    			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");    			
    			try {
					batch.setVerificationDateTime(format.parse(rd.getValue()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		} else if(rd.getFieldName().equals("detection_lab")) {
    			
    			batch.setLab(locationService.searchByCode(rd.getValue()));
    			
    		} else if(rd.getFieldName().equals("testing_detection_complete")) {
    			
    			String val = "Complete";
    			if(rd.getValue().equals("0")) {
    				val = "Incomplete";
    			} else if(rd.getValue().equals("1")) {
    				val = "Unverified";
    			}
    			
    			batch.setDetectionStatus(val);
    			
    		} else if(rd.getFieldName().equals("resulting_complete")) {
    			
    			String val = "Complete";
    			if(rd.getValue().equals("0")) {
    				val = "Incomplete";
    			} else if(rd.getValue().equals("1")) {
    				val = "Unverified";
    			}
    			
    			batch.setResultingStatus(val);
    			
    		} else if(rd.getFieldName().equals("verification_complete")) {
    			
    			String val = "Complete";
    			if(rd.getValue().equals("0")) {
    				val = "Incomplete";
    			} else if(rd.getValue().equals("1")) {
    				val = "Unverified";
    			}
    			
    			batch.setVerificationStatus(val);
    			
    		} else if(rd.getFieldName().equals("test_det_personnel")) {
    			
    			batch.setDetectionPersonnel(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_assay_personnel")) {
    			
    			batch.setResultingPersonnel(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_verify_personnel")) {
    			
    			batch.setVerificationPersonnel(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_verify_personnel")) {
    			
    			batch.setVerificationPersonnel(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_det_instrument")) {
    			
    			InstrumentVO inst = new InstrumentVO();
    			inst.setCode(rd.getValue());
    			batch.setInstrument(inst);
    			
    		} else if(rd.getFieldName().contains("test_det_barcode_")) {
    			
    			t2.add(rd);
    		}
    	}
    	
    	batch.setInstrumentBatchSize((long)t2.size());
    	ArrayList<SpecimenVO> items = new ArrayList<>();
    	batch.setBatchItems(items);
    	
    	for(int i = 0; i < t2.size(); i++) {
    		items.add(new SpecimenVO());
    	}
    	    	
    	// Get the specimen information
    	for(RedcapDataVO rd : t2) {
    		String pos = rd.getFieldName().substring(17);
    		
    		int idx = Integer.parseInt(pos) - 1;
    		SpecimenVO specimen = specimenService.findSpecimenByBarcode(rd.getValue());
    		
    		if(specimen == null) {
    			specimen = new SpecimenVO();
    			specimen.setSpecimenBarcode(rd.getValue());
    			specimen.setPatient(new PatientVO());
    		}
    		items.set(idx, specimen);
    	}
    	
    	return batch;
    }
    
    /**
     * Search the batched
     * 
     * @param searchCriteria
     * @return
     */
    @PostMapping("/search/batch")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public List<BatchVO> searchBatches(@RequestBody BatchSearchCriteria searchCriteria) {
    	
    	List<BatchVO> batches = new ArrayList<BatchVO>();
    	
    	RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
    	criteria.setProjectId(labResultingPID);
    	
    	if(!StringUtils.isBlank(searchCriteria.getBatchId())) {
    		criteria.setRecord(searchCriteria.getBatchId());
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

    
}
