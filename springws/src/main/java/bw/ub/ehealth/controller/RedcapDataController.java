package bw.ub.ehealth.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import bw.ub.ehealth.dhislink.vo.DDPObjectField;
import bw.ub.ehealth.dhislink.vo.Event;
import io.jsonwebtoken.lang.Collections;

@RestController
@RequestMapping("/ddpcontroller/data")
@CrossOrigin()
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
    
	@Value("${dhis2.api.url}")
	private String dhis2Url;
	
    @Value("${app.live}")
    private Boolean isLive;
    
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
    
    @PostMapping("/publishresults")
    @ResponseStatus(code = HttpStatus.OK)
	public void publishResults(@RequestBody BatchVO batch) {
		
	}
    
    private Map<String, SpecimenVO> getSpecimenMap(List<SpecimenVO> specimen) {
    	HashMap<String, SpecimenVO> map = new HashMap<>();
    	
    	for(SpecimenVO sp : specimen) {
    		map.put(sp.getSpecimenBarcode(), sp);
    	}
    	
    	return map;
    }
    
    private List<SpecimenVO> queryDhisSpecimenBarcodes(List<String> barcodes) {
    	
    	if(Collections.isEmpty(barcodes)) {
    		return new ArrayList<>();
    	}
    	
    	String queryBase = dhis2Url + "/events?programStage=nIaEdUY97YD&program=HR4C8VTwGuo&filter=kkD26RljqPY:IN:";
    	StringBuilder builder = new StringBuilder();
    	
    	for(String barcode : barcodes) {
    		if(builder.length() > 0) {
    			builder.append(";");
    		}
    		builder.append(barcode);
    	}
    	builder.insert(0, queryBase);
    	    	
    	List<Event> events = dhisLink.eventQueryExecute(builder.toString());
    	List<SpecimenVO> found = dhisLink.getSpecimen(events, false);
    	    	    	    	
    	return found;
    }
    
    @PostMapping("/pullspecimen")
    @ResponseBody
    public List<SpecimenVO> pullSpecimenInfo(@RequestBody List<SpecimenVO> specimenToPull) {

    	if(Collections.isEmpty(specimenToPull)) {
    		return new ArrayList<>();
    	}
    	
    	List<SpecimenVO> noInfo = new ArrayList<>();
    	List<String> barcodes = new ArrayList<>();
    	Map<String, SpecimenVO> sps = new HashMap<>();
    	
    	for(SpecimenVO sp : specimenToPull) {
    		if(StringUtils.isBlank(sp.getEvent())) {
    			noInfo.add(sp);
    			sps.put(sp.getSpecimenBarcode(), sp);
    			barcodes.add(sp.getSpecimenBarcode());
    		}
    	}
    	
    	List<SpecimenVO> pulled = queryDhisSpecimenBarcodes(barcodes);
    	redcapLink.updateStaging(pulled);
    	
    	for(SpecimenVO sp : pulled) {
    		SpecimenVO s = sps.get(sp.getSpecimenBarcode());
    		logger.debug(s.toString());
    		for(int i = 0; i < specimenToPull.size(); i++) {
    			SpecimenVO s2 = specimenToPull.get(i);
    			if(s.getSpecimenBarcode().equals(s2.getSpecimenBarcode())) {
    				s.setPosition(s2.getPosition());
    				specimenToPull.set(i, s);
    			}
    		}
    	}
    	
    	return specimenToPull;
    }
    
    @PostMapping("/savebatch")
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    public List<SpecimenVO> saveBatch(@RequestBody BatchVO batch) {
    	//logger.debug(batch.toString());
    	List<RedcapDataVO> redcapData = new ArrayList<RedcapDataVO>();
    	List<SpecimenVO> verifiedSpecimen = new ArrayList<SpecimenVO>();
    	
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
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_id", batch.getBatchId()));  	
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_batch_id", batch.getDetectionBatchId()));    	
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_batch_id", batch.getAssayBatchId()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_personnel", batch.getResultingPersonnel()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_datetime", batch.getResultingDateTime()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_batchsize", batch.getDetectionSize().toString()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "resulting_complete", batch.getDetectionStatus()));
	    	
    	} else if(batch.getPage().equals("verification")) {

	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_id", batch.getBatchId()));  	
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_batch_id", batch.getDetectionBatchId()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batch_id", batch.getVerifyBatchId()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_personnel", batch.getVerificationPersonnel()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_datetime", batch.getVerificationDateTime()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batchsize", batch.getDetectionSize().toString()));
	    	redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "verification_complete", batch.getVerificationStatus()));
	    	
    	}
    	
    	/// Detect barcodes that do not have data in the stagins
		List<String> missing = new ArrayList<>();

		for (SpecimenVO specimen : batch.getBatchItems()) {
			if (specimen.getId() == null) {
				// Record this somewhere
				missing.add(specimen.getSpecimenBarcode());
				
				// Save the specimen in the staging area
				
				if(specimen.getPatient() != null && specimen.getPatient().getIdentityNo() == null) {
		    		specimen.setPatient(null);
		    	}
				specimenService.saveSpecimen(specimen);
				if(specimen.getPatient() == null) {
					specimen.setPatient(new PatientVO());
				}
			}
		}

		// Find the specimen from DHIS2
		List<SpecimenVO> found = new ArrayList<>();
		if(batch.getPublishResults()) {
			found = queryDhisSpecimenBarcodes(missing);
		}
		Map<String, SpecimenVO> foundMap = getSpecimenMap(found);
    	
    	for(SpecimenVO specimen : batch.getBatchItems()) {
    		
    		if(foundMap.containsKey(specimen.getSpecimenBarcode())) {
    			String position = specimen.getPosition();
    			specimen = foundMap.get(specimen.getSpecimenBarcode());
    			specimen.setPosition(position);
    		}
    		
    		int pos = decodePosition(specimen.getPosition());
    		if(batch.getPage().equals("testing_detection")) {
    			
	    		RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_barcode_" + pos, specimen.getSpecimenBarcode());
	    		redcapData.add(data);
	    		
    		} else if(batch.getPage().equals("resulting")) {
    			
	    		RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_result_" + pos, specimen.getTestAssayResults());
	    		redcapData.add(data);
	    		
	    		specimen.setResultsEnteredBy(batch.getResultingPersonnel());
	    		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	    		try {
					specimen.setResultsEnteredDate(format.parse(batch.getResultingDateTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
	    		
    		} else if(batch.getPage().equals("verification")) {
	    		
	    		if(!StringUtils.isBlank(specimen.getTestVerifyResults()) && specimen.getTestVerifyResults().equals("5")) {
	    			specimen.setCovidRnaResults(specimen.getTestAssayResults());
		    		RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "covid_rna_results" + pos, specimen.getCovidRnaResults());
		    		redcapData.add(data);
		    		
		    		data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_result_" + pos, specimen.getTestVerifyResults());
		    		redcapData.add(data);

	    			specimen.setResults(specimen.getCovidRnaResults());
	    			if(specimen.getId() != null) {
	    				verifiedSpecimen.add(specimen);
	    			}
	    		}
	    		
	    		if(!StringUtils.isBlank(specimen.getResultsVerifiedBy())) {
		    		specimen.setResultsVerifiedBy(batch.getVerificationPersonnel());
		    		
		    		try {
		    			SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		    			specimen.setResultsVerifiedDate(format.parse(batch.getVerificationDateTime()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
    			}
	    		
	    		if(!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
		    		specimen.setResultsAuthorisedBy(batch.getAuthorisingPersonnel());
		    		
		    		try {
		    			SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		    			specimen.setResultsAuthorisedDate(format.parse(batch.getAuthorisingDateTime()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
	    		}
    		}
    		
    	}

    	// Save the data for this particular project       	
		redcapLink.postSpecimen((List)batch.getBatchItems(), batch.getProjectId());

		// Update the staging area. This also updated the lab report
		redcapLink.updateStaging(batch.getBatchItems());
		
		if(batch.getPublishResults() && isLive) {
			//logger.info(String.format("%d specimen data sent to DHIS2 and they are %s", verifiedSpecimen.size(), verifiedSpecimen.toString()));
			dhisLink.getDhisPayload(verifiedSpecimen);
		}
		
		List<SpecimenVO> tmp = new ArrayList<>();
		
		for (SpecimenVO specimen : batch.getBatchItems()) {
			if(foundMap.containsKey(specimen.getSpecimenBarcode())) {
    			String position = specimen.getPosition();
    			specimen = foundMap.get(specimen.getSpecimenBarcode());
    			specimen.setPosition(position);
    			tmp.add(specimen);
    		} else {
    			tmp.add(specimen);
    		}
		}
		
		return tmp;
    }
        
    @GetMapping("/extraction/specimen/{batchId}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<SpecimenVO> fetchExtractionBatchSpecimen(@PathVariable @NotNull String batchId) {
    	
    	RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
    	criteria.setProjectId(labExtractionPID);    	
    	criteria.setFieldName("test_ext_barcode_%");
    	criteria.setRecord(batchId);
    	    	
    	return doFetchBatchSpecimen(criteria);
    }
    
    @PostMapping("/batch/specimen")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<SpecimenVO> fetchBatchSpecimen(@RequestBody RedcapDataSearchCriteria criteria) {
    	
    	return doFetchBatchSpecimen(criteria);
    }
    
    private Collection<SpecimenVO> doFetchBatchSpecimen(RedcapDataSearchCriteria criteria) {
    	Collection<SpecimenVO> specimens = new ArrayList<SpecimenVO>();
    	Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);
    	
    	for(RedcapDataVO rd : tmp) {
    		SpecimenVO specimen = specimenService.findSpecimenByBarcode(rd.getValue());
    		
    		if(specimen == null) {
    			specimen = new SpecimenVO();
    			specimen.setSpecimenBarcode(rd.getValue());
    			specimen.setPatient(new PatientVO());
    		}
    		
    		if(specimen.getPatient() == null || specimen.getPatient().getId() == null) {
    			specimen.setPatient(new PatientVO());
    		}
    		
    		specimens.add(specimen);
    	}
    	
    	return specimens;
    }
    
    @PostMapping("/publish/specimen")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public SpecimenVO synchSpecimen(@RequestBody SpecimenVO specimen) {
    	
    	return null;
    }
    
    @PostMapping("/saveall")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<RedcapDataVO> saveRedcapData(@RequestBody Collection<RedcapDataVO> data, @RequestBody Long projectId) {
    	
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
    
    private String getInstrumentName(String code) {
    	
    	String name = "";
    	if(code.equals("122")) {
    		name = "NHL ROCHE Z480 (122)";
    	} else if(code.equals("221")) {

    		name = "BHHRL ABI 7500 S/N 750S8180106 (221)";
    	} else if(code.equals("222")) {

    		name = "BHHRL m2000rt S/N 275020775 (222)";
    	} else if(code.equals("321")) {
    		name = "UB ABI 7500 (321)";
    		
    	} else if(code.equals("421")) {

    		name = "BNVL ABI 7500 FAST (421)";
    	} else if(code.equals("521")) {

    		name = "BVI ABI 7500 (521)";
    	} else if(code.equals("999")) {

    		name = "Other detection machine(specify) (999))";
    	}
    	
    	return name;
    }
    
    private BatchVO getBatchFromRedcapData(List<RedcapDataVO> data, boolean includeSpecimen) {
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
    			inst.setName(getInstrumentName(rd.getValue()));
    			batch.setInstrument(inst);
    			
    		} else if(rd.getFieldName().contains("test_det_barcode_")) {
    			
    			t2.add(rd);
    		}
    	}
    	
    	if(includeSpecimen) {
	    	batch.setInstrumentBatchSize((long)t2.size());
	    	ArrayList<SpecimenVO> items = new ArrayList<>();
	    	    	
	    	for(int i = 0; i < t2.size(); i++) {
	    		items.add(new SpecimenVO());
	    	}
	
	    	// Get the specimen information
	    	for(RedcapDataVO rd : t2) {
	    		String pos = rd.getFieldName().substring(17);
	    		
	    		int idx = Integer.parseInt(pos) - 1;
	    		
	    		SpecimenVO specimen = specimenService.findSpecimenByBarcode(rd.getValue());
	    		
	    		if(specimen == null 
	    				|| StringUtils.isBlank(specimen.getTestAssayResults())
	    				|| StringUtils.isBlank(specimen.getResultsEnteredBy())
	    				|| StringUtils.isBlank(specimen.getResultsVerifiedBy())) {
	    			
	    			if(specimen == null) {
		    			specimen = new SpecimenVO();
		    			specimen.setSpecimenBarcode(rd.getValue());
		    			specimen.setPatient(new PatientVO());
	    			}
	    			
	    			if(specimen.getPatient() == null) {
	    				specimen.setPatient(new PatientVO());
	    			}
	    			
	    			List<DDPObjectField> fields = dhisLink.getResultingFormFields(rd.getValue());
	    			for(DDPObjectField field : fields) {
	    				if(field.getField().equals("test_assay_personnel")) {
	    					specimen.setResultsEnteredBy(field.getValue());
	    				} else if(field.getField().equals("test_assay_datetime")) {
	    					    					
	    					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    					try {
	    						specimen.setResultsEnteredDate(format.parse(field.getValue()));
	    					} catch (ParseException e) {
	    						e.printStackTrace();
	    					}
	    					
	    				} else if(field.getField().equals("test_assay_result")) {
	    					specimen.setTestAssayResults(field.getValue());
	    					
	    				}
	    				
	    			}
	    			
	    			fields = dhisLink.getVerificationFormFields(rd.getValue());
	    			for(DDPObjectField field : fields) {
	    				if(field.getField().equals("test_verify_personnel")) {
	    					specimen.setResultsVerifiedBy(field.getValue());
	    				} else if(field.getField().equals("test_verify_datetime")) {
	    					    					
	    					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    					try {
	    						specimen.setResultsVerifiedDate(format.parse(field.getValue()));
	    					} catch (ParseException e) {
	    						e.printStackTrace();
	    					}
	    					
	    				} else if(field.getField().equals("covid_rna_results")) {
	    					
	    					specimen.setCovidRnaResults(field.getValue());
	    					
	    				} else if(field.getField().equals("test_verify_result")) {
	    					
	    					specimen.setTestVerifyResults(field.getValue());
	    					
	    				} 
	    			}
	    			specimen.setDhis2Synched(false);
	    		}
	    		
	    		if(StringUtils.isBlank(batch.getAuthorisingPersonnel()) && 
	    				!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
	    			batch.setAuthorisingPersonnel(specimen.getResultsAuthorisedBy());
	    			
	    			Instant authDate = specimen.getResultsAuthorisedDate().toInstant();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());
	    			batch.setAuthorisingDateTime(formatter.format(authDate));
	    		}
	    		
	    		specimen.setCovidRnaResults(specimen.getTestAssayResults());
	    		
	    		if(specimen.getId() == null) {
	    			if(specimen.getPatient() != null && specimen.getPatient().getId() == null) {
	    				specimen.setPatient(null);	    			
	    			}
	    			specimen = specimenService.saveSpecimen(specimen);
	    		}
    			if(specimen.getPatient() == null) {
    				//logger.debug(specimen.toString());
					specimen.setPatient(new PatientVO());
				}

	    		specimen.setPosition(encodePosition(Integer.parseInt(pos)));
	    		items.set(idx, specimen);
	    	}
	    	batch.setBatchItems(items);
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
    	
    	if(searchCriteria.getIncludeSpecimen() == null) {
    		logger.debug(searchCriteria.toString());
    		searchCriteria.setIncludeSpecimen(true);
    	}
    	
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
        		batches.add(getBatchFromRedcapData(map.get(searchCriteria.getBatchId()), searchCriteria.getIncludeSpecimen()));
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
    				batches.add(getBatchFromRedcapData(e.getValue(), searchCriteria.getIncludeSpecimen()));
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
    				batches.add(getBatchFromRedcapData(e.getValue(), searchCriteria.getIncludeSpecimen()));
    			}
    		}
    	}
    	
    	return batches;    	
    }

    
}
