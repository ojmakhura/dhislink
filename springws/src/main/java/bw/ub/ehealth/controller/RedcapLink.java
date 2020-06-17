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
import java.util.List;
import java.util.Locale;

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
	
	@Value("${dhis2.api.program}")
    private String program;

    @Value("${dhis2.api.program.stage}")
    private String programStage;
    
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
	
	public void postRedcapData(SpecimenVO specimen, Long projectId) {
		List<RedcapDataVO> list  = getSpecimenRedcapData(specimen, projectId);
		
		doPostRedcapData(list, projectId);
	}
	
	public void postRedcapData(List<RedcapDataVO> list, Long projectId) {
		doPostRedcapData(list, projectId);
	}

	private void doPostRedcapData(List<RedcapDataVO> list, Long projectId) {
		
		String username = securityService.findLoggedInUsername();
		
		if(StringUtils.isBlank(username)) {
			username = "dhislink";
		}
		
		String projectToken = redcapAuthService.getUserProjectToken(username, projectId);
		
		if(StringUtils.isBlank(projectToken)) {
			projectToken = redcapAuthService.getUserProjectToken("dhislink", projectId);
		}

		JSONArray arr = new JSONArray();
		JSONObject records = new JSONObject();
		
		for(RedcapDataVO dt : list) {
			try {
				records.put(dt.getFieldName(), dt.getValue());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		arr.put(records);
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
			respCode = resp.getStatusLine().getStatusCode();

			try
			{
				reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
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
		}

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

		return data;
		
	}

	/**
	 * Update the staging area with the data from the forms
	 * @param specimens
	 */ 
	public void updateStaging(Collection<SpecimenVO> specimens) {
		
		for(SpecimenVO specimen : specimens) {
			if(StringUtils.isBlank(specimen.getEvent())) {
				continue;
			}
			RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
			// Reception project
			criteria.setProjectId(labReceptionPID);
			criteria.setFieldName("lab_rec_barcode_%");
			criteria.setValue(specimen.getSpecimenBarcode());
			
			List<RedcapDataVO> reportData = new ArrayList<RedcapDataVO>();
			reportData.add(new RedcapDataVO(null, null, null, "specimen_barcode", specimen.getSpecimenBarcode()));
			
			List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			// Update with information from receiving forms
			if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
				RedcapDataVO rd = redcapDataVOs.get(0);
				
				int index = rd.getFieldName().lastIndexOf("_");
				String pos = rd.getFieldName().substring(index + 1);
				
				criteria = new RedcapDataSearchCriteria();
				criteria.setEventId(rd.getEventId());
				criteria.setProjectId(rd.getProjectId());
				criteria.setRecord(rd.getRecord());
				criteria.setFieldName("received_datetime");
				
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setReceivingDateTime(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "received_datetime", rd.getValue());
					reportData.add(tmp);					
				}
				
				criteria.setFieldName("receiving_personnel");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setReceivingPersonnel(rd.getValue());
										
					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "receiving_personnel", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("receiving_lab");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setReceivingLab(rd.getValue());

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "receiving_lab", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("lab_rec_id");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setReceivingLab(rd.getValue());
					
					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "lab_rec_id", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("specimen_cond_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setReceivingConditionCode(rd.getValue());

					RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "receiving_condition_code", rd.getValue());
					reportData.add(tmp);
				}
			}
			
			criteria = new RedcapDataSearchCriteria();
			// Extraction project
			criteria.setProjectId(labResultingPID);
			criteria.setFieldName("test_ext_barcode_%");
			criteria.setValue(specimen.getSpecimenBarcode());
			
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			// Update with information from receiving forms
			if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
				RedcapDataVO rd = redcapDataVOs.get(0);
				
				RedcapDataVO tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_ext_barcode", rd.getValue());
				reportData.add(tmp);

				int index = rd.getFieldName().lastIndexOf("_");
				String pos = rd.getFieldName().substring(index + 1);
				tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "ext_batch_pos", pos);
				reportData.add(tmp);
				
				criteria = new RedcapDataSearchCriteria();
				criteria.setEventId(rd.getEventId());
				criteria.setProjectId(rd.getProjectId());
				criteria.setRecord(rd.getRecord());
				criteria.setFieldName("test_ext_datetime");
				
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setResultsEnteredDate(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_ext_datetime", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("test_ext_personnel");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResultsEnteredBy(rd.getValue());
					
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_ext_personnel", rd.getValue());
					reportData.add(tmp);
				}

				
				criteria.setFieldName("test_ext_instrument" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_ext_instrument", rd.getValue());
					reportData.add(tmp);
					
				}
			}
			
			criteria = new RedcapDataSearchCriteria();
			// Resulting project
			criteria.setProjectId(labResultingPID);
			criteria.setFieldName("test_det_barcode_%");
			criteria.setValue(specimen.getSpecimenBarcode());
			
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
				RedcapDataVO rd = redcapDataVOs.get(0);
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
				criteria.setFieldName("test_assay_datetime");
				
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						specimen.setResultsEnteredDate(format.parse(rd.getValue()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_datetime", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("test_assay_personnel");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResultsEnteredBy(rd.getValue());
					
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_personnel", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("test_assay_result_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_result", rd.getValue());
					reportData.add(tmp);
					
				}
				
				criteria.setFieldName("test_assay_result_why_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_result_why", rd.getValue());
					reportData.add(tmp);
					
				}
				
				/**
				 * Detection form fields
				 */
				criteria.setFieldName("test_det_personnel");				
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_personnel", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("test_det_datetime");		
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_datetime", rd.getValue());
					reportData.add(tmp);
				}	
				
				criteria.setFieldName("test_det_batchsize");	
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_batchsize", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("detection_lab");		
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "detection_lab", rd.getValue());
					reportData.add(tmp);
				}
				
				criteria.setFieldName("test_det_instrument");		
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_det_instrument", rd.getValue());
					reportData.add(tmp);
				}
				
				/**
				 * Find if the the results have been verified
				 */
				criteria.setFieldName("test_verify_result_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_verify_result", rd.getValue());
					reportData.add(tmp);
					
					if(rd.getValue().contentEquals("5")) {
						// Replace with the verified results
						criteria.setFieldName("covid_rna_results" + pos);
						redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
						if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
							rd = redcapDataVOs.get(0);
							specimen.setResults(rd.getValue());
														
							tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "covid_rna_results", rd.getValue());
							reportData.add(tmp);
						}
						
						criteria.setFieldName("test_verify_personnel");
						redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
						if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
							rd = redcapDataVOs.get(0);
														
							tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_verify_personnel", rd.getValue());
							reportData.add(tmp);
							specimen.setResultsVerifiedBy(rd.getValue());
						}
						
						criteria.setFieldName("test_verify_datetime");
						redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
						if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
							rd = redcapDataVOs.get(0);
														
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
				
				criteria.setFieldName("test_assay_result_why_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResults(rd.getValue());
										
					tmp = getRedcapDataObjet(specimen.getSpecimenBarcode(), labReportPID, "test_assay_result_why", rd.getValue());
					reportData.add(tmp);
					
				}
			}
			
			criteria = new RedcapDataSearchCriteria(); 
			// Reporting project
			criteria.setProjectId(labReportPID);
			criteria.setRecord(specimen.getSpecimenBarcode());
			criteria.setFieldName("result_authorised");
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			if (redcapDataVOs != null && redcapDataVOs.size() > 0) {
				RedcapDataVO rd = redcapDataVOs.get(0);

				if (rd.getValue().equals("1")) { // Results have been authorised
					criteria.setFieldName("covid_rna_results");
					redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
					if (redcapDataVOs != null && redcapDataVOs.size() > 0) {
						rd = redcapDataVOs.get(0);
						specimen.setResults(rd.getValue());
					}
					
					// Who authorised
					criteria.setFieldName("authorizer_personnel");
					redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
					if (redcapDataVOs != null && redcapDataVOs.size() > 0) {
						rd = redcapDataVOs.get(0);
						specimen.setResultsAuthorisedBy(rd.getValue());
					}
					
					// When was the results authorised
					criteria.setFieldName("authorizer_datetime");
					redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
					if (redcapDataVOs != null && redcapDataVOs.size() > 0) {
						rd = redcapDataVOs.get(0);
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						try {
							specimen.setResultsAuthorisedDate(format.parse(rd.getValue()));
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
					}
				}
			}

			specimenService.saveSpecimen(specimen);
			
			doPostRedcapData(reportData, labReportPID);
		}
	}
}
