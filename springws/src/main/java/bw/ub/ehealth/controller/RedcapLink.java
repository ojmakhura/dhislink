package bw.ub.ehealth.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import bw.ub.ehealth.dhislink.redacap.auth.service.RedcapAuthService;
import bw.ub.ehealth.dhislink.redacap.auth.service.SecurityService;
import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;
import bw.ub.ehealth.dhislink.specimen.service.SpecimenService;
import bw.ub.ehealth.dhislink.specimen.vo.SpecimenVO;

@Service(value = "redcapLink")
public class RedcapLink {

    private Logger logger = LoggerFactory.getLogger(RedcapLink.class);

	@Autowired
	private Environment env;
	
	@Value("${dhis2.api.url}")
	private String dhis2Url;
	
	//@Value("${dhis2.api.program}")
    //private String program;

    //@Value("${dhis2.api.program.stage}")
    //private String programStage;
    
    @Value("${lab.report.pid}")
    private Long labReportPID;

    @Value("${lab.reception.pid}")
    private Long labReceptionPID;

    @Value("${lab.extraction.pid}")
    private Long labExtractionPID;

    @Value("${lab.resulting.pid}")
    private Long labResultingPID;
    
    @Value("${redcap.api.url}")
    private String redcapApiUrl;
    
	@Autowired
	private RedcapDataService redcapDataService;
	
	@Autowired
	private SpecimenService specimenService;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private RedcapAuthService redcapAuthService;
	
	public void postSpecimen(SpecimenVO specimen, Long projectId) {
		
		if(specimen == null) {
			return;
		}
		
		List<RedcapDataVO> list  = getSpecimenRedcapData(specimen, projectId);
		HashMap<String, Collection<RedcapDataVO>> data = new HashMap<String, Collection<RedcapDataVO>>();
		data.put(specimen.getSpecimenBarcode(), list);
		doPostRedcapData(data, projectId);
	}
	
	public void postSpecimen(Collection<SpecimenVO> specimens, Long projectId) {
		
		if(specimens == null || specimens.size() == 0) {
			return;
		}
		
		HashMap<String, Collection<RedcapDataVO>> data = new HashMap<String, Collection<RedcapDataVO>>();
		
		for(SpecimenVO specimen : specimens) {

			List<RedcapDataVO> list  = getSpecimenRedcapData(specimen, projectId);
			data.put(specimen.getSpecimenBarcode(), list);
		}
		doPostRedcapData(data, projectId);
		
	}
		
	public void postRedcapData(Map<String, Collection<RedcapDataVO>> data, Long projectId) {
		doPostRedcapData(data, projectId);
	}
	
	public void postRedcapData(Collection<RedcapDataVO> list, Long projectId) {
		
		if(list == null || list.size() == 0) {
			return;
		}
		
		HashMap<String, Collection<RedcapDataVO>> data = new HashMap<String, Collection<RedcapDataVO>>();
		
		for(RedcapDataVO dt : list) {
			String key = dt.getRecord();
			if(StringUtils.isBlank(key)) {
				key = dt.getValue();
			}
			
			List<RedcapDataVO> tmp = (List<RedcapDataVO>) data.get(key);
			
			if(tmp == null) {
				tmp = new ArrayList<>();
				data.put(key, tmp);
			}
			
			tmp.add(dt);
		}
		
		doPostRedcapData(data, projectId);
	}

	private void doPostRedcapData(Map<String, Collection<RedcapDataVO>> data, Long projectId) {
		
		String username = securityService.findLoggedInUsername();
		
		if(StringUtils.isBlank(username)) {
			username = "dhislink";
		}
		
		String projectToken = redcapAuthService.getUserProjectToken(username, projectId);
		
		if(StringUtils.isBlank(projectToken)) {
			projectToken = redcapAuthService.getUserProjectToken("dhislink", projectId);
		}
		
		JSONArray arr = new JSONArray();
		
		
		for(Map.Entry<String, Collection<RedcapDataVO>> entry : data.entrySet()) {
			JSONObject record = new JSONObject();
			for(RedcapDataVO dt : entry.getValue()) {
				try {
					record.put(dt.getFieldName(), dt.getValue());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			arr.put(record);
		}
		
		//logger.debug(arr.toString());
		ArrayList<NameValuePair> params = getProjectParams(projectToken);
		params.add(new BasicNameValuePair("data", arr.toString()));

		HttpPost post = new HttpPost(redcapApiUrl);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");

		try
		{
			post.setEntity(new UrlEncodedFormEntity(params));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		HttpClient client = HttpClientBuilder.create().build();
		
		doPost(client, post);
	}
	
	private ArrayList<NameValuePair> getProjectParams(String projectToken) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("content", "record"));
		params.add(new BasicNameValuePair("format", "json"));
		params.add(new BasicNameValuePair("type", "flat"));
		params.add(new BasicNameValuePair("token", projectToken));
		
		return params;
	}
		
	public void doPost(HttpClient client, HttpPost post)
	{
		StringBuffer result = new StringBuffer();
		HttpResponse resp = null;
		int respCode = -1;
		BufferedReader reader = null;
		String line;

		try
		{
			resp = client.execute(post);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		if(resp != null)
		{
			logger.debug("doPost: response = " + resp.toString());
			respCode = resp.getStatusLine().getStatusCode();

			try
			{
				reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
				logger.debug("doPost: resp.getEntity().getContent() = " + resp.getEntity().getContent());
			}
			catch (final Exception e)
			{
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		} else {
			logger.debug("Could not execute post. Response object is null.");
		}
		
		if(reader != null)
		{
			
			try
			{
				while((line = reader.readLine()) != null)
				{
					result.append(line);
				}
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		} else {
			logger.debug("No data read.");
		}
		
		logger.debug("doPost: result is " + result.toString());
	}
	
	private RedcapDataVO getRedcapDataObjet(String record, Long projectId, String fieldName, String value) {
    	
    	RedcapDataVO data = new RedcapDataVO();
    	data.setFieldName(fieldName);
    	data.setRecord(record);
    	data.setValue(value);
    	data.setProjectId(projectId); 
    	
    	return data;
    }
	
	/**
	 * Convert a specimen into a list of redcap data objects
	 * 
	 * @param specimen
	 * @return
	 */
	public List<RedcapDataVO> getSpecimenRedcapData(SpecimenVO specimen, Long projectId) {
		
		List<RedcapDataVO> data = new ArrayList<RedcapDataVO>();		
		RedcapDataVO tmp = new RedcapDataVO();
		
		if(specimen.getPatient() != null) {
						
			if (!StringUtils.isBlank(specimen.getPatient().getFirstName())) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(projectId);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("patient_first_name");
				tmp.setValue(specimen.getPatient().getFirstName());
				data.add(tmp);
			}
	
			if (!StringUtils.isBlank(specimen.getPatient().getSurname())) {
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "patient_surname", specimen.getPatient().getSurname());
				data.add(tmp);
			}
	
			if (!StringUtils.isBlank(specimen.getPatient().getIdentityNo())) {
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "national_id", specimen.getPatient().getIdentityNo());
				data.add(tmp);
			}
	
			if (!StringUtils.isBlank(specimen.getPatient().getSex())) {
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "sex", "");
				
				if(specimen.getPatient().getSex().equals("MALE")) {
					tmp.setValue("1");
				} else if(specimen.getPatient().getSex().equals("FEMALE")) {
					tmp.setValue("2");
				} else {
					tmp.setValue("3");
				}
				data.add(tmp);
			}
						
			if(specimen.getPatient().getDateOfBirth() != null) {
							
				Instant dob = specimen.getPatient().getDateOfBirth().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "date_birth", date);
				data.add(tmp);
				
				// Calculate the age
				Calendar cal = Calendar.getInstance();
				cal.setTime(specimen.getPatient().getDateOfBirth());
				int year = cal.get(Calendar.YEAR);
				int month =  cal.get(Calendar.MONTH) + 1;
				int day = cal.get(Calendar.DATE);
				
				if(month == 0 || month > 12) {
					month = 1;
				}
								
				if(day == 0 || day > 31) {
					day = 1;
				}
				
				LocalDate d1 = LocalDate.of(year, month, day);
				LocalDate now = LocalDate.now();
				
				Period diff = Period.between(d1, now);
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "age", ""+ diff.getYears());
				data.add(tmp);
			}
		}
		
		if(specimen.getDispatchDate() != null ) {
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "date_dispatched", specimen.getDispatchDate().toString());
			data.add(tmp);
		}
		
		if(specimen.getDispatchDate() != null) {
			
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "date_specimen_collected", specimen.getCollectionDateTime().toString());
			data.add(tmp);
		}
		
		if (specimen.getLatitude() != null) {
			
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "gis_lat", specimen.getLatitude());
			data.add(tmp);
		}

		if (specimen.getLongitude() != null) {

			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "gis_long", specimen.getLongitude());
			data.add(tmp);
		}

		if (!StringUtils.isBlank(specimen.getSpecimenBarcode())) {
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "specimen_barcode", specimen.getSpecimenBarcode());
			data.add(tmp);
						
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "covid19_lab_report_complete", "0");
			data.add(tmp);
		}
		
		if (!StringUtils.isBlank(specimen.getDispatchLocation())) {
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "dispatch_facility", specimen.getDispatchLocation());
			data.add(tmp);
			
		}
		
		if (!StringUtils.isBlank(specimen.getPatientFacility())) {

			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "patient_facility", specimen.getPatientFacility());
			data.add(tmp);
			
		}
		
		if(!StringUtils.isBlank(specimen.getCovidNumber())) {
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "patient_facility", specimen.getCovidNumber());
			data.add(tmp);
		}
		
		return data;
		
	}
	
	private HashMap<String, RedcapDataVO> getRedcapDataMap(List<RedcapDataVO> data) {
		HashMap<String, RedcapDataVO> map = new HashMap<>();
		
		for(RedcapDataVO vo : data) {
			map.put(vo.getFieldName(), vo);
		}
		
		return map;
	}

	/**
	 * Update the staging area with the data from the forms
	 * @param specimens
	 */ 
	public void updateStaging(Collection<SpecimenVO> specimens) {
		Map<String, Collection<RedcapDataVO>> toPost = new HashMap<>();
		for(SpecimenVO specimen : specimens) {

			RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
			// Reception project
			criteria.setProjectId(labReceptionPID);
			criteria.setValue(specimen.getSpecimenBarcode());
			
			List<RedcapDataVO> reportData = new ArrayList<RedcapDataVO>();
			reportData.add(new RedcapDataVO(null, labReportPID, null, "specimen_barcode", specimen.getSpecimenBarcode()));
			
			List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			HashMap<String, RedcapDataVO> searchMap = null;
			
			RedcapDataVO rec = null, tpor = null;
			
			for(RedcapDataVO r : redcapDataVOs) {
				if(r.getFieldName().contains("lab_rec_barcode_")) {
					rec = r;
				}
				
				if(r.getFieldName().contains("test_tpor_barcode_")) {
					tpor = r;
				}
			}
			
			// Update with information from receiving forms
			if(rec != null) {
				RedcapDataVO rd = rec;
				
				int index = rd.getFieldName().lastIndexOf("_");
				String pos = rd.getFieldName().substring(index + 1);
				
				criteria = new RedcapDataSearchCriteria();
				criteria.setEventId(rd.getEventId());
				criteria.setProjectId(rd.getProjectId());
				criteria.setRecord(rd.getRecord());
				
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				searchMap = getRedcapDataMap(redcapDataVOs);
				String key = "received_datetime";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setReceivingDateTime(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);					
				}
				
				key = "receiving_personnel";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setReceivingPersonnel(rd.getValue());
										
					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "receiving_lab";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setReceivingLab(rd.getValue());

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "lab_rec_id";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setReceivingLab(rd.getValue());
					
					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "receiving_condition_code";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setReceivingConditionCode(rd.getValue());

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				// Testing point of reception
				if(tpor != null) {
					rd = tpor;

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_tpor_barcode", rd.getValue());
					reportData.add(tmp);
					index = rd.getFieldName().lastIndexOf("_");
					pos = rd.getFieldName().substring(index + 1);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "tpor_batch_pos", pos);
					reportData.add(tmp);
					
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_tpor_batch_id", rd.getRecord());
					reportData.add(tmp);
				}
				
				key = "tpor_lab";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_tpor_batchsize";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_tpor_datetime";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_tpor_personnel";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
			}
			
			criteria = new RedcapDataSearchCriteria();
			// Extraction project
			criteria.setProjectId(labExtractionPID);
			criteria.setValue(specimen.getSpecimenBarcode());
			
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			RedcapDataVO ext = null;
			if(redcapDataVOs.size() == 1) {
				ext = redcapDataVOs.get(0);
			} else {
				// TODO: find the latest extraction
			}
			
			// Update with information from receiving forms
			if(ext != null) {
				RedcapDataVO rd = ext;
				
				RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_ext_barcode", rd.getValue());
				reportData.add(tmp);
				
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_ext_batch_id", rd.getRecord());
				reportData.add(tmp);

				int index = rd.getFieldName().lastIndexOf("_");
				String pos = rd.getFieldName().substring(index + 1);
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "ext_batch_pos", pos);
				reportData.add(tmp);
				
				criteria = new RedcapDataSearchCriteria();
				criteria.setEventId(rd.getEventId());
				criteria.setProjectId(rd.getProjectId());
				criteria.setRecord(rd.getRecord());
				
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				searchMap = getRedcapDataMap(redcapDataVOs);
				String key = "test_ext_datetime";
				
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setResultsEnteredDate(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_ext_personnel";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResultsEnteredBy(rd.getValue());
					
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_ext_instrument";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
					
				}
				
				key = "extraction_lab";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
					
				}
				
				key = "test_ext_batchsize";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
			}
			
			criteria = new RedcapDataSearchCriteria();
			// Resulting project
			criteria.setProjectId(labResultingPID);
			criteria.setValue(specimen.getSpecimenBarcode());	
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			RedcapDataVO det = null;
			
			if(redcapDataVOs.size() == 1) {
				det = redcapDataVOs.get(0);
			} else {
				// TODO: what to do if the specimen was processed multiple times
			}
			
			if(det != null) {
				RedcapDataVO rd = det;
				int index = rd.getFieldName().lastIndexOf("_");
				String pos = rd.getFieldName().substring(index + 1);
				
				RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_barcode", specimen.getSpecimenBarcode());
				reportData.add(tmp);

				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_batch_id", rd.getRecord());
				reportData.add(tmp);
				
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "det_batch_pos", pos);
				reportData.add(tmp);
				
				criteria = new RedcapDataSearchCriteria();
				criteria.setEventId(rd.getEventId());
				criteria.setProjectId(rd.getProjectId());
				criteria.setRecord(rd.getRecord());
				criteria.setFieldName(null);
				
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				searchMap = getRedcapDataMap(redcapDataVOs);
				
				String key = "test_assay_datetime";
				
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setResultsEnteredDate(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, key, rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_assay_personnel";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResultsEnteredBy(rd.getValue());
					
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_personnel", rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_assay_result";
				if(searchMap.containsKey(key + "_" + pos)) {
					rd = searchMap.get(key + "_" + pos);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_result", rd.getValue());
					reportData.add(tmp);
					
					index = rd.getFieldName().lastIndexOf("_");
					String post = rd.getFieldName().substring(index + 1);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_batch_id", post);
					reportData.add(tmp);
				}
				
				key = "test_assay_result_why";
				if(searchMap.containsKey(key + "_" + pos)) {
					rd = searchMap.get(key + "_" + pos);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_result_why", rd.getValue());
					reportData.add(tmp);
					
				}
				
				/**
				 * Detection form fields
				 */
				key = "test_det_personnel";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_personnel", rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_det_datetime";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_datetime", rd.getValue());
					reportData.add(tmp);
				}	
				
				key = "test_det_batchsize";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_batchsize", rd.getValue());
					reportData.add(tmp);
				}
				
				key = "detection_lab";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "detection_lab", rd.getValue());
					reportData.add(tmp);
				}
				
				key = "test_det_instrument";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_instrument", rd.getValue());
					reportData.add(tmp);
				}
				
				/**
				 * Find if the the results have been verified
				 */
				key = "test_verify_result";
				if(searchMap.containsKey(key + "_" + pos)) {
					rd = searchMap.get(key + "_" + pos);

					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_verify_result", rd.getValue());
					reportData.add(tmp);
					
					if(rd.getValue().equals("5")) {
						// Replace with the verified results
						key = "covid_rna_results";
						if(searchMap.containsKey(key + pos)) {
							rd = searchMap.get(key + pos);
							specimen.setResults(rd.getValue());
														
							tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "covid_rna_results", rd.getValue());
							reportData.add(tmp);
						}
						
						key = "test_verify_personnel";
						if(searchMap.containsKey(key)) {
							rd = searchMap.get(key);
														
							tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_verify_personnel", rd.getValue());
							reportData.add(tmp);
							specimen.setResultsVerifiedBy(rd.getValue());
						}
						
						key = "test_verify_datetime";
						if(searchMap.containsKey(key)) {
							rd = searchMap.get(key);
														
							tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_verify_datetime", rd.getValue());
							reportData.add(tmp);
							
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							try {
								specimen.setResultsVerifiedDate(format.parse(rd.getValue()));
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				}
				
				//criteria.setFieldName("test_assay_result_why_" + pos);
				//redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				key = "test_assay_result_why";
				if(searchMap.containsKey(key + "_" + pos)) {
					rd = searchMap.get(key + "_" + pos);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_result_why", rd.getValue());
					reportData.add(tmp);
					
				}
			}
			
			criteria = new RedcapDataSearchCriteria(); 
			// Reporting project
			criteria.setProjectId(labReportPID);
			criteria.setRecord(specimen.getSpecimenBarcode());
			//criteria.setFieldName("result_authorised");
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			searchMap = getRedcapDataMap(redcapDataVOs);
			if (searchMap.containsKey("result_authorised")) {
				RedcapDataVO rd = searchMap.get("result_authorised");

				if (rd.getValue().equals("1")) { // Results have been authorised
					if (searchMap.containsKey("covid_rna_results")) {
						rd = searchMap.get("covid_rna_results");
						specimen.setResults(rd.getValue());
					}
					
					// Who authorised
					if (searchMap.containsKey("authorizer_personnel")) {
						rd = searchMap.get("authorizer_personnel");
						specimen.setResultsAuthorisedBy(rd.getValue());
					} else {
						if(!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
							reportData.add(getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "authorizer_personnel", specimen.getResultsAuthorisedBy()));
						}
					}
					
					// When was the results authorised
					if (searchMap.containsKey("authorizer_datetime")) {
						rd = searchMap.get("authorizer_datetime");
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						try {
							specimen.setResultsAuthorisedDate(format.parse(rd.getValue()));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
					} else {
						if(specimen.getResultsAuthorisedDate() != null) {
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
							reportData.add(getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "authorizer_datetime", format.format(specimen.getResultsAuthorisedDate())));
						}
					}
				}
			} else {
				if(!StringUtils.isBlank(specimen.getResultsAuthorisedBy()) && specimen.getResultsAuthorisedDate() != null) {
					reportData.add(getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "result_authorised", "1"));
					reportData.add(getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "authorizer_personnel", specimen.getResultsAuthorisedBy()));
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					reportData.add(getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "authorizer_datetime", format.format(specimen.getResultsAuthorisedDate())));
				} else {
					reportData.add(getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "result_authorised", "0"));
				}
			}
			
			if(specimen.getPatient() != null && StringUtils.isBlank(specimen.getPatient().getIdentityNo())) {
				specimen.setPatient(null);
			}
			
			specimenService.saveSpecimen(specimen);
			//logger.info(specimen.toString());
			
			String status = "0";
			
			/// If the specimen results have been authorised, then it is complete
			if(!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
				status = "2";
			}
			
			// If the information from dhis is not available the status must be set to incomplete
			if(specimen.getPatient() == null || specimen.getPatient().getId() == null || specimen.getId() == 0) {
				status = "1";
			}
			
			reportData.add(getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "covid19_lab_report_complete", status));
			//logger.debug("Positing to lab report : " + reportData.toString());
			
			toPost.put(specimen.getSpecimenBarcode(), reportData);
		}
		
		doPostRedcapData(toPost, labReportPID);		
	}
}
