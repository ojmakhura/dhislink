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
import java.util.TimeZone;

import org.apache.commons.collections4.CollectionUtils;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bw.ub.ehealth.dhislink.redacap.auth.service.RedcapAuthService;
import bw.ub.ehealth.dhislink.redacap.auth.service.SecurityService;
import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.redacap.data.vo.RecordExport;
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
	
	public void postSpecimen(Collection<SpecimenVO> specimens, Collection<RedcapDataVO> batchData, Long projectId) {
		
		if(specimens == null || specimens.size() == 0) {
			return;
		}
		
		HashMap<String, Collection<RedcapDataVO>> data = new HashMap<String, Collection<RedcapDataVO>>();
		
		for(SpecimenVO specimen : specimens) {

			List<RedcapDataVO> list  = getSpecimenRedcapData(specimen, projectId);
			for(RedcapDataVO dt : batchData) {
				list.add(new RedcapDataVO(null, projectId, specimen.getSpecimenBarcode(), dt.getFieldName(), dt.getValue()));
			}
			data.put(specimen.getSpecimenBarcode(), list);
		}
		
		logger.info(data.toString());
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
		
		logger.debug(arr.toString());
		ArrayList<NameValuePair> params = getProjectParams(projectToken);
		params.add(new BasicNameValuePair("data", arr.toString()));
		
		executePost(params);
	}
	
	private ArrayList<NameValuePair> getProjectParams(String projectToken) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("content", "record"));
		params.add(new BasicNameValuePair("format", "json"));
		//params.add(new BasicNameValuePair("type", "eav"));
		params.add(new BasicNameValuePair("token", projectToken));
		
		return params;
	}
	
	public RecordExport searchData(List<String> records, List<String> fields, String filter, Long projectId) {

		String username = securityService.findLoggedInUsername();
		
		if(StringUtils.isBlank(username)) {
			username = "dhislink";
		}
		
		String projectToken = redcapAuthService.getUserProjectToken(username, projectId);
		
		if(StringUtils.isBlank(projectToken)) {
			projectToken = redcapAuthService.getUserProjectToken("dhislink", projectId);
		}
		
		ArrayList<NameValuePair> params = getProjectParams(projectToken);
		params.add(new BasicNameValuePair("type", "eav"));
		if(!CollectionUtils.isEmpty(records)) {
			StringBuilder builder = new StringBuilder();
			for(String record : records) {
				if(builder.length() > 0) {
					builder.append(",");
				}
				builder.append(record);
			}
			params.add(new BasicNameValuePair("records", builder.toString()));
		}

		if(!CollectionUtils.isEmpty(fields)) {
			StringBuilder builder = new StringBuilder();
			for(String field : fields) {
				if(builder.length() > 0) {
					builder.append(",");
				}
				builder.append(field);
			}
			params.add(new BasicNameValuePair("fields", builder.toString()));
		}
		
		if(!StringUtils.isBlank(filter)) {
			params.add(new BasicNameValuePair("filterLogic", filter));
		}
		
		String response = executePost(params);
		//logger.info(response);
		ObjectMapper mapper = new ObjectMapper();
		RecordExport data = null;
		
		try {
			data = mapper.readValue(response, RecordExport.class);
			
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	public String executePost(ArrayList<NameValuePair> params) {

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
				
		return doPost(post);
	}
		
	public String doPost(HttpPost post)
	{
		StringBuffer result = new StringBuffer();
		HttpResponse resp = null;
		int respCode = -1;
		BufferedReader reader = null;
		String line;
		HttpClient client = HttpClientBuilder.create().build();
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
		
		return String.format("{\"responseCode\": %d, \"datas\": %s}", respCode, result.toString());
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
		data.add(new RedcapDataVO(null, projectId, null, "specimen_barcode", specimen.getSpecimenBarcode()));
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

			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "test_ext_barcode", specimen.getSpecimenBarcode());
			data.add(tmp);

			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "test_det_barcode", specimen.getSpecimenBarcode());
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
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "ipms_lab_covid_number", specimen.getCovidNumber());
			data.add(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestAssayResults())) {
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "test_assay_result", specimen.getTestAssayResults());
			data.add(tmp);

			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "test_assay_personnel", specimen.getResultsEnteredBy());
			data.add(tmp);
						
		}
		
		if(specimen.getResultsEnteredDate() != null) {
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String dt = format.format(specimen.getResultsEnteredDate());
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "test_assay_datetime", dt);
			data.add(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestVerifyResults())) {
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "test_verify_result", specimen.getTestVerifyResults());
			data.add(tmp);

			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "test_verify_personnel", specimen.getResultsVerifiedBy());
			data.add(tmp);
						
		}
		
		if(specimen.getResultsVerifiedDate() != null) {

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			format.setTimeZone(TimeZone.getTimeZone("Africa/Gaborone"));
			String dt = format.format(specimen.getResultsVerifiedDate());
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "test_verify_datetime", dt);
			data.add(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
			
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "authorizer_personnel", specimen.getResultsAuthorisedBy());
			data.add(tmp);

			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "covid_rna_results", specimen.getCovidRnaResults());
			data.add(tmp);
		}
		
		if(specimen.getResultsAuthorisedDate() != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			format.setTimeZone(TimeZone.getTimeZone("Africa/Gaborone"));
			String dt = format.format(specimen.getResultsAuthorisedDate());
			tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), projectId, "authorizer_datetime", dt);
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

		for(SpecimenVO specimen : specimens) {

			RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
			// Reception project
			criteria.setProjectId(labReceptionPID);
			criteria.setValue(specimen.getSpecimenBarcode());
						
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
												
				List<String> records = new ArrayList<String>();
				records.add(rd.getRecord());
				
				RecordExport data = searchData(records, new ArrayList<String>(), null, rd.getProjectId());
				searchMap = getRedcapDataMap((List<RedcapDataVO>) data.getDatas());
				String key = "received_datetime";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setReceivingDateTime(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}			
				}
				
				key = "receiving_personnel";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setReceivingPersonnel(rd.getValue());
				}
				
				key = "receiving_lab";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setReceivingLab(rd.getValue());
				}
				
				key = "lab_rec_id";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setReceivingLab(rd.getValue());
				}
				
				key = "receiving_condition_code";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setReceivingConditionCode(rd.getValue());
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
								
				List<String> records = new ArrayList<String>();
				records.add(rd.getRecord());
				
				RecordExport data = searchData(records, new ArrayList<String>(), null, rd.getProjectId());
				
				searchMap = getRedcapDataMap((List<RedcapDataVO>) data.getDatas());
				String key = "test_ext_datetime";
				
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setResultsEnteredDate(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
				key = "test_ext_personnel";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResultsEnteredBy(rd.getValue());
				}
				
				key = "test_ext_instrument";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResults(rd.getValue());
					
				}
				
				key = "extraction_lab";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResults(rd.getValue());
					
				}
				
				key = "test_ext_batchsize";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResults(rd.getValue());
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
								
				List<String> records = new ArrayList<String>();
				records.add(rd.getRecord());
				
				RecordExport data = searchData(records, new ArrayList<String>(), null, rd.getProjectId());
				searchMap = getRedcapDataMap((List<RedcapDataVO>) data.getDatas());
				
				String key = "test_assay_datetime";
				
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setResultsEnteredDate(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
				key = "test_assay_personnel";
				if(searchMap.containsKey(key)) {
					rd = searchMap.get(key);
					specimen.setResultsEnteredBy(rd.getValue());
				}
				
				key = "test_assay_result";
				if(searchMap.containsKey(key + "_" + pos)) {
					rd = searchMap.get(key + "_" + pos);
					specimen.setResults(rd.getValue());
				}
				
				key = "test_assay_result_why";
				if(searchMap.containsKey(key + "_" + pos)) {
					rd = searchMap.get(key + "_" + pos);
					specimen.setResults(rd.getValue());
				}
								
				key = "test_assay_result_why";
				if(searchMap.containsKey(key + "_" + pos)) {
					rd = searchMap.get(key + "_" + pos);
					specimen.setResults(rd.getValue());
					
				}
			}
			
			criteria = new RedcapDataSearchCriteria(); 
			// Reporting project
			criteria.setProjectId(labReportPID);
			criteria.setRecord(specimen.getSpecimenBarcode());
			
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
					}
				}
			}
			
			if(specimen.getPatient() != null && StringUtils.isBlank(specimen.getPatient().getIdentityNo())) {
				specimen.setPatient(null);
			}
			
			specimenService.saveSpecimen(specimen);
			
		}
	}
}
