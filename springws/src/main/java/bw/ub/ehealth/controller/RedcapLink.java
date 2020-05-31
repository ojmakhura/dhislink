package bw.ub.ehealth.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

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
	
	public void postRedcapData(SpecimenVO specimen) {
		List<RedcapDataVO> list  = getSpecimenRedcapData(specimen);
		String username = securityService.findLoggedInUsername();
		redcapAuthService.getUserProjectToken(username, labReportPID);
		doPostRedcapData(list, env.getProperty("redcap.lab.report.token"));
	}
	
	public void postRedcapData(List<RedcapDataVO> list, String projectToken ) {
		doPostRedcapData(list, projectToken);
	}

	public void doPostRedcapData(List<RedcapDataVO> list, String projectToken) {

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
		params.add(new BasicNameValuePair("token", projectToken)); //env.getProperty(project)));
		
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
	
	
	/**
	 * Convert a specimen into a list of redcap data objects
	 * 
	 * @param specimen
	 * @return
	 */
	public List<RedcapDataVO> getSpecimenRedcapData(SpecimenVO specimen) {
		
		List<RedcapDataVO> data = new ArrayList<RedcapDataVO>();
		
		RedcapDataVO tmp = new RedcapDataVO();
		
		if(specimen.getPatient() != null) {
						
			if (!StringUtils.isBlank(specimen.getPatient().getFirstName())) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("patient_first_name");
				tmp.setValue(specimen.getPatient().getFirstName());
				data.add(tmp);
			}
	
			if (!StringUtils.isBlank(specimen.getPatient().getSurname())) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("patient_surname");
				tmp.setValue(specimen.getPatient().getSurname());
				data.add(tmp);
			}
	
			if (!StringUtils.isBlank(specimen.getPatient().getIdentityNo())) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("national_id");
				tmp.setValue(specimen.getPatient().getIdentityNo());
				data.add(tmp);
			}
	
			if (!StringUtils.isBlank(specimen.getPatient().getSex())) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("sex");
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
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("date_birth");
				Calendar cal = Calendar.getInstance();
				cal.setTime(specimen.getPatient().getDateOfBirth());
				
				tmp.setValue(cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DATE));
				data.add(tmp);
				
				// Calculate the age
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("age");
				
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
				
				tmp.setValue("" + diff.getYears());
				data.add(tmp);
			}
		}
		
		if(specimen.getDispatchDate() != null ) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("date_dispatched");
			tmp.setValue(specimen.getDispatchDate().toString());
			data.add(tmp);
		}
		
		if(specimen.getDispatchDate() != null) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("date_specimen_collected");
			tmp.setValue(specimen.getCollectionDateTime().toString());
			data.add(tmp);
		}
		
		if (specimen.getLatitude() != null) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("gis_lat");
			tmp.setValue(specimen.getLatitude());
			data.add(tmp);
		}

		if (specimen.getLongitude() != null) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("gis_long");
			tmp.setValue(specimen.getLongitude());
			data.add(tmp);
		}

		if (!StringUtils.isBlank(specimen.getSpecimenBarcode())) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("specimen_barcode");
			tmp.setValue(specimen.getSpecimenBarcode());
			data.add(tmp);
			
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("covid19_lab_report_complete");
			tmp.setValue("0");
			data.add(tmp);
		}
		
		if (!StringUtils.isBlank(specimen.getDispatchLocation())) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("dispatch_facility");
			tmp.setValue(specimen.getDispatchLocation());
			data.add(tmp);
			
		}
		
		if (!StringUtils.isBlank(specimen.getPatientFacility())) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("patient_facility");
			tmp.setValue(specimen.getPatientFacility());
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
			RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
			// Reception project
			criteria.setProjectId(labReceptionPID);
			criteria.setFieldName("lab_rec_barcode_%");
			criteria.setValue(specimen.getSpecimenBarcode());
			
			List<RedcapDataVO> reportData = new ArrayList<RedcapDataVO>();
			reportData.add(new RedcapDataVO(null, null, null, "specimen_barcode", specimen.getSpecimenBarcode()));
			
			RedcapDataVO tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setRecord(specimen.getSpecimenBarcode());
			
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
					
					tmp.setFieldName("received_datetime");
					tmp.setValue(rd.getValue());
					
					redcapDataVOs.add(tmp);
					
				}
				
				criteria.setFieldName("receiving_personnel");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setReceivingPersonnel(rd.getValue());
					
					tmp.setFieldName("receiving_personnel");
					tmp.setValue(rd.getValue());
					
					redcapDataVOs.add(tmp);
				}
				
				criteria.setFieldName("receiving_lab");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setReceivingLab(rd.getValue());
					
					tmp.setFieldName("receiving_lab");
					tmp.setValue(rd.getValue());
					
					redcapDataVOs.add(tmp);
				}
				
				criteria.setFieldName("lab_rec_id");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setReceivingLab(rd.getValue());
					
					tmp.setFieldName("lab_rec_id");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
				}
				
				criteria.setFieldName("specimen_cond_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setReceivingConditionCode(rd.getValue());
					
					tmp.setFieldName("receiving_condition_code");
					tmp.setValue(rd.getValue());
					
					redcapDataVOs.add(tmp);
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
				int index = rd.getFieldName().lastIndexOf("_");
				String pos = rd.getFieldName().substring(index + 1);
				
				tmp.setFieldName("test_ext_barcode");
				tmp.setValue(rd.getValue());
				redcapDataVOs.add(tmp);
				
				tmp.setFieldName("ext_batch_pos");
				tmp.setValue(pos);
				redcapDataVOs.add(tmp);
				
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
					
					tmp.setFieldName("test_ext_datetime");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
				}
				
				criteria.setFieldName("test_ext_personnel");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResultsEnteredBy(rd.getValue());
					
					tmp.setFieldName("test_ext_personnel");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
				}

				
				criteria.setFieldName("test_ext_instrument" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResults(rd.getValue());
					
					tmp.setFieldName("test_ext_instrument");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
					
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

				tmp.setFieldName("test_det_barcode");
				tmp.setValue(specimen.getSpecimenBarcode());
				redcapDataVOs.add(tmp);
				
				tmp.setFieldName("det_batch_pos");
				tmp.setValue(pos);
				redcapDataVOs.add(tmp);
				
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
					
					tmp.setFieldName("test_assay_datetime");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
				}
				
				criteria.setFieldName("test_assay_personnel");
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResultsEnteredBy(rd.getValue());
					
					tmp.setFieldName("test_assay_personnel");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
				}
				
				criteria.setFieldName("test_assay_result_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResults(rd.getValue());
					
					tmp.setFieldName("test_assay_result");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
					
				}
				
				criteria.setFieldName("test_assay_result_why_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					specimen.setResults(rd.getValue());
					
					tmp.setFieldName("test_assay_result_why");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
					
				}
				
				// Find if the the results have been verified
				criteria.setFieldName("test_verify_result_" + pos);
				redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
				if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
					rd = redcapDataVOs.get(0);
					
					tmp.setFieldName("test_verify_result");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
					
					if(rd.getValue().contentEquals("5")) {
						// Replace with the verified results
						criteria.setFieldName("covid_rna_results" + pos);
						redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
						if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
							rd = redcapDataVOs.get(0);
							specimen.setResults(rd.getValue());
							
							tmp.setFieldName("covid_rna_results");
							tmp.setValue(rd.getValue());							
							redcapDataVOs.add(tmp);
						}
						
						criteria.setFieldName("test_verify_personnel");
						redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
						if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
							rd = redcapDataVOs.get(0);
							tmp.setValue(rd.getValue());	
							tmp.setFieldName("test_verify_personnel");						
							redcapDataVOs.add(tmp);
							specimen.setResultsVerifiedBy(rd.getValue());
						}
						
						criteria.setFieldName("test_verify_datetime");
						redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
						if(redcapDataVOs != null && redcapDataVOs.size() > 0) {
							rd = redcapDataVOs.get(0);
							tmp.setValue(rd.getValue());	
							tmp.setFieldName("test_verify_datetime");						
							redcapDataVOs.add(tmp);
							
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
					
					tmp.setFieldName("test_assay_result_why");
					tmp.setValue(rd.getValue());
					redcapDataVOs.add(tmp);
					
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

			if(StringUtils.isBlank(specimen.getEvent())) {
				specimenService.saveSpecimen(specimen);
			}
			doPostRedcapData(redcapDataVOs, "redcap.lab.report.token");
		}
	}
}
