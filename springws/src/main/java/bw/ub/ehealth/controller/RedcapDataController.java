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
	
    @Autowired
    private DhisLink dhisLink;
	
    @PostMapping("/saveone")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public RedcapDataVO saveRedcapData(@RequestBody RedcapDataVO redcapDataVO) {
    	
    	return redcapDataService.saveRedcapData(redcapDataVO);
    }
    
    private RedcapDataVO getRedcapDataObjet(String record, Long projectId, String fieldName, String value) {
    	
    	RedcapDataVO data = new RedcapDataVO();
    	data.setFieldName(fieldName);
    	data.setRecord(record);
    	data.setValue(value);
    	data.setProjectId(projectId); 
    	
    	return data;
    }
    
    @PostMapping("/authoriseresults")
    @ResponseStatus(code = HttpStatus.OK)
	public void publishResults(@RequestBody BatchVO batch) {
		
	}
    
    @PostMapping("/savebatch")
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    public List<RedcapDataVO> saveBatch(@RequestBody BatchVO batch) {
    	
    	List<RedcapDataVO> redcapData = new ArrayList<RedcapDataVO>();
    	
    	if(batch.getPage().equals("testing_detection")) {
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_id", batch.getBatchId()));  	
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_batch_id", batch.getDetectionBatchId()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_batch_id", batch.getAssayBatchId()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batch_id", batch.getVerifyBatchId()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_personnel", batch.getDetectionPersonnel()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_datetime", batch.getDetectionDateTime()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "detection_lab", batch.getLab().getCode()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_instrument", batch.getInstrument().getCode()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_batchsize", batch.getDetectionSize().toString()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "testing_detection_complete", batch.getDetectionStatus()));
	    	
    	} else if(batch.getPage().equals("resulting")) {
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_batch_id", batch.getAssayBatchId()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_personnel", batch.getResultingPersonnel()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_datetime", batch.getResultingDateTime()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_batchsize", batch.getDetectionSize().toString()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "resulting_complete", batch.getDetectionStatus()));
	    	
    	} else if(batch.getPage().equals("verification")) {
    		 	
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batch_id", batch.getVerifyBatchId()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_personnel", batch.getVerificationPersonnel()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_datetime", batch.getVerificationDateTime()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batchsize", batch.getDetectionSize().toString()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "verification_complete", batch.getVerificationStatus()));
	    	
    	}
    	
    	for(SpecimenVO specimen : batch.getBatchItems()) {
    		
    		int pos = decodePosition(specimen.getPosition());
    		if(batch.getPage().equals("testing_detection")) {
    			
	    		RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_barcode_" + pos, specimen.getSpecimenBarcode());
	    		redcapData.add(data);
	    		
    		} else if(batch.getPage().equals("resulting")) {
    			
	    		RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_result_" + pos, specimen.getResults());
	    		redcapData.add(data);
	    		
	    		specimen.setResultsEnteredBy(batch.getResultingPersonnel());
	    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	    		try {
					specimen.setResultsEnteredDate(format.parse(batch.getResultingDateTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
	    		
    		} else if(batch.getPage().equals("verification")) {
    			
	    		RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "covid_rna_results" + pos, specimen.getCovidRnaResults());
	    		redcapData.add(data);
	    		
	    		data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_result_" + pos, specimen.getTestVerifyResults());
	    		redcapData.add(data);
	    		
	    		specimen.setResultsVerifiedBy(batch.getVerificationPersonnel());
	    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	    		try {
					specimen.setResultsVerifiedDate(format.parse(batch.getVerificationDateTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
    		}    		
    	}

    	// Save the data for this particular project    	
		redcapLink.postRedcapData(redcapData, batch.getProjectId());

		// Update the staging area. This also updated the lab report
		redcapLink.updateStaging(batch.getBatchItems());
    	
		return redcapData;
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
    	
    	redcapLink.postRedcapData((List<RedcapDataVO>) data, projectId);
     	
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
    		if(rd.getFieldName().equals("test_det_id")) {
    			batch.setBatchId(rd.getValue());
    		} else if(rd.getFieldName().equals("test_det_batch_id")) {
    			batch.setDetectionBatchId(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_assay_batch_id")) {
    			batch.setAssayBatchId(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_verify_batch_id")) {
    			batch.setVerifyBatchId(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("test_det_batchsize")) {
    			batch.setDetectionSize(Long.parseLong(rd.getValue()));
    			
    		} else if(rd.getFieldName().equals("test_det_datetime")) {
    			
    			batch.setDetectionDateTime(rd.getValue());
				    			
    		} else if(rd.getFieldName().equals("test_assay_datetime")) {
    			
    			batch.setResultingDateTime(rd.getValue());
				    			
    		} else if(rd.getFieldName().equals("test_verify_datetime")) {
    			
    			batch.setVerificationDateTime(rd.getValue());
    			
    		} else if(rd.getFieldName().equals("detection_lab")) {
    			
    			batch.setLab(locationService.searchByCode(rd.getValue()));
    			
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
    		specimen.setPosition(encodePosition(Integer.parseInt(pos)));
    		specimen.setCovidRnaResults(specimen.getTestAssayResults());
    		
    		items.set(idx, specimen);
    	}
    	    	
    	return batch;
    }
    
    /**
     * Decode the instrument position into the relative position of scanning 
     * 
     * @param encoded
     * @return
     */
    public int decodePosition(String encoded) {
    	
    	int pos = 1;
    	int q, r;
    	r = Integer.parseInt(encoded.substring(1));
    	
    	char ch = encoded.charAt(0);
    	
    	if(ch == 'A') {
    		
    		q = 0;
    		
    	} else if (ch == 'B') {
    		
    		q = 1;
    		
    	} else if (ch == 'C') {
    		
    		q = 2;
    	} else if (ch == 'D') {
    		
    		q = 3;
    	} else if (ch == 'E') {
    		
    		q = 4;
    	} else if (ch == 'F') {
    		
    		q = 5;
    	} else if (ch == 'G') {
    		
    		q = 6;
    	} else {
    		q = 7;
    	}
    	
    	return q * 12 + r;
    }
    
    /**
     * 
     * @param i
     * @return
     */
    private String encodePosition(int i) {
    	
        int q = i / 12;
        int r = i % 12;
        
        if(r == 0) {
        	r = 12;
        	q--;
        }
        
        String position = "";

        if(q == 0) {
          position = "A" + r;
        } else if(q == 1) {
          position = "B" + r;
        } else if(q == 2) {
          position = "C" + r;
        } else if(q == 3) {
          position = "D" + r;
        } else if(q == 4) {
          position = "E" + r;
        } else if(q == 5) {
          position = "F" + r;
        } else if(q == 6) {
          position = "G" + r;
        } else if(q == 7) {
          position = "H" + r;
        }

        return position;
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
