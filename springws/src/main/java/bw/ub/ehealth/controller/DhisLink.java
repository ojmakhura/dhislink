package bw.ub.ehealth.controller;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import bw.ub.ehealth.dhislink.patient.service.PatientService;
import bw.ub.ehealth.dhislink.patient.vo.PatientVO;
import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;
import bw.ub.ehealth.dhislink.specimen.service.SpecimenService;
import bw.ub.ehealth.dhislink.specimen.vo.SpecimenVO;
import bw.ub.ehealth.dhislink.vo.CurrentUser;
import bw.ub.ehealth.dhislink.vo.DDPObjectField;
import bw.ub.ehealth.dhislink.vo.DataElement;
import bw.ub.ehealth.dhislink.vo.DataValue;
import bw.ub.ehealth.dhislink.vo.Event;
import bw.ub.ehealth.dhislink.vo.EventList;
import bw.ub.ehealth.dhislink.vo.OrganisationUnit;
import bw.ub.ehealth.dhislink.vo.OrganisationUnitList;
import bw.ub.ehealth.dhislink.vo.Program;
import bw.ub.ehealth.dhislink.vo.Sex;
import bw.ub.ehealth.dhislink.vo.SpecimenType;
import bw.ub.ehealth.dhislink.vo.Symptoms;
import bw.ub.ehealth.dhislink.vo.TestType;
import bw.ub.ehealth.dhislink.vo.TrackedEntityInstance;
import bw.ub.ehealth.dhislink.vo.TrackedEntityInstanceAttribute;
import bw.ub.ehealth.dhislink.vo.TrackedEntityInstanceList;

@Component
public class DhisLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8927307237563216372L;

	private Logger logger = LoggerFactory.getLogger(DhisLink.class);

	private CurrentUser currentUser = null;

	private static Map<String, String> dataElementType = null;

	@Autowired
	private Environment env;

	@Autowired
	private RestTemplateBuilder builder;

	@Value("${dhis2.api.url}")
	private String dhis2Url;
	
	@Value("${dhis2.api.program}")
    private String program;
	
	@Value("${lab.specimen.barcode}")
    private String barcodeField;
	
	@Value("${dhis2.mohw.org.unit}")
    private String mohwOrgUnit;

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
    
    @Value("${app.live}")
    private Boolean isLive;
    
	@Autowired
	private PatientService patientService;

	@Autowired
	private SpecimenService specimenService;

	@Autowired
	private RedcapDataService redcapDataService;
		
	@Autowired
	private RedcapLink redcapLink;
	
	private int numPulled = 0;
	
	public DhisLink() {

	}
	
	public int getNumPulled() {
		return this.numPulled;
	}

	@Bean
	public RestTemplate restTemplate() {
		
		return builder.basicAuthentication("redcapLink", ")>Ys<+6V|DBCo81").build();
	}

	public CurrentUser getCurrentUser() {

		currentUser = restTemplate().getForObject(dhis2Url + "/me", CurrentUser.class);
		return currentUser;
	}

	/**
	 * Build the GET URL for events and get a list from DHIS
	 * 
	 * @param parameters
	 * @return
	 */
	public List<Event> getEvents(Map<String, String> parameters) {
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (builder.length() > 0) {
				builder.append("&");
			}
			builder.append(entry.getKey() + "=" + entry.getValue());
		}
		String finalUrl = dhis2Url + "/events?" + builder.toString();
		logger.debug(finalUrl);
		logger.info(finalUrl);
		
		return eventQueryExecute(finalUrl);
	}
	
	public List<Event> eventQueryExecute(String queryUrl) {
		EventList eventList = restTemplate().getForObject(queryUrl, EventList.class);
		return (List<Event>) eventList.getEvents();
	}

	/**
	 * Load a program from DHIS
	 * @param program
	 * @return
	 */
	public Program getProgram(String program) {

		return restTemplate().getForObject(dhis2Url + "/programs/" + program, Program.class);
	}

	/**
	 * Load an tracked entity instance from DHIS
	 * 
	 * @param orgUnit
	 * @param instanceId
	 * @return
	 */
	public TrackedEntityInstance getTrackedEntityInstance(@NotBlank String orgUnit, @NotBlank String instanceId) {

		String append = "/trackedEntityInstances?ou=" + orgUnit + "&trackedEntityInstance=" + instanceId;
		TrackedEntityInstanceList list = restTemplate().getForObject(dhis2Url + append,
				TrackedEntityInstanceList.class);
		TrackedEntityInstance instance = null;
		if (list != null && list.getTrackedEntityInstances() != null && list.getTrackedEntityInstances().size() > 0) {
			instance = ((List<TrackedEntityInstance>) list.getTrackedEntityInstances()).get(0);
		}

		return instance;
	}
	
	private List<TrackedEntityInstance> getTrackedEntityInstanceList(@NotBlank String instanceIds) {
		String append = "/trackedEntityInstances?ou=" + mohwOrgUnit + "&trackedEntityInstance=" + instanceIds;
		TrackedEntityInstanceList list = restTemplate().getForObject(dhis2Url + append, TrackedEntityInstanceList.class);
		
		return (List<TrackedEntityInstance>) list.getTrackedEntityInstances();
	}

	private PatientVO trackedEntityInstanceToPatientVO(TrackedEntityInstance trackedEntityInstance) {
		PatientVO patientVO = null;

		Map<String, TrackedEntityInstanceAttribute> attrMap = getTrackedEntityInstanceAttributeMap(
				(List<TrackedEntityInstanceAttribute>) trackedEntityInstance.getAttributes());

		/// Get identity number
		patientVO = new PatientVO();
		
		patientVO.setCreated(trackedEntityInstance.getCreated());
		patientVO.setLastUpdated(trackedEntityInstance.getLastUpdated());
		patientVO.setTrackedEntityInstance(trackedEntityInstance.getTrackedEntityInstance());
		
		if (attrMap.get(env.getProperty("patient.omang")) != null) {
			patientVO.setIdentityNo(attrMap.get(env.getProperty("patient.omang")).getValue());
		}

		if (attrMap.get(env.getProperty("patient.passport")) != null && patientVO.getIdentityNo() == null) {
			patientVO.setIdentityNo(attrMap.get(env.getProperty("patient.passport")).getValue());
		}

		/// Get the surname
		if (attrMap.get(env.getProperty("patient.surname")) != null) {
			patientVO.setSurname(attrMap.get(env.getProperty("patient.surname")).getValue());
		}

		/// Get the first name
		if (attrMap.get(env.getProperty("patient.name")) != null) {
			patientVO.setFirstName(attrMap.get(env.getProperty("patient.name")).getValue());
		}

		/// Get the birth day
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

			if (attrMap.get(env.getProperty("patient.birth.date")) != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(format.parse(attrMap.get(env.getProperty("patient.birth.date")).getValue()));
				patientVO.setDateOfBirth(cal.getTime());
			}

			/// If the bith date does not exist and age does, estimate birth day from it
			if (attrMap.get(env.getProperty("patient.age")) != null && patientVO.getDateOfBirth() == null) {
				Integer age = Integer.parseInt(attrMap.get(env.getProperty("patient.age")).getValue());
				int a = 0 - age;

				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, a);

				patientVO.setDateOfBirth(cal.getTime());
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		/// Get sex
		if (attrMap.get(env.getProperty("patient.sex")) != null) {
			
			String sex = attrMap.get(env.getProperty("patient.sex")).getValue();
			
			if(sex.equalsIgnoreCase("male")) {
				patientVO.setSex(Sex.MALE.getValue());
			} else if(sex.equalsIgnoreCase("female")) {
				patientVO.setSex(Sex.FEMALE.getValue());
			} else {
			
				patientVO.setSex(Sex.UNKNOWN.getValue());
			}
		}

		// Get patient nationality
		if (attrMap.get(env.getProperty("patient.nationality")) != null) {
			patientVO.setNationality(attrMap.get(env.getProperty("patient.nationality")).getValue());
		}

		// Get patient phone
		if (attrMap.get(env.getProperty("patient.phone")) != null) {
			patientVO.setContactNumber(attrMap.get(env.getProperty("patient.phone")).getValue());
		}

		// Get the plot number
		if (attrMap.get(env.getProperty("patient.plot.no")) != null) {
			patientVO.setPlotNo(attrMap.get(env.getProperty("patient.plot.no")).getValue());
		}

		/// Get transport registration
		if (attrMap.get(env.getProperty("patient.transportation")) != null) {
			patientVO.setTransportRegistration(attrMap.get(env.getProperty("patient.transportation")).getValue());
		}

		/// Get destination
		if (attrMap.get(env.getProperty("patient.travelled.where")) != null) {
			patientVO.setTravelDestination(attrMap.get(env.getProperty("patient.travelled.where")).getValue());
		}

		/// Get kin
		if (attrMap.get(env.getProperty("patient.kin")) != null) {
			patientVO.setNextOfKin(attrMap.get(env.getProperty("patient.kin")).getValue());
		}

		/// Get kin contact
		if (attrMap.get(env.getProperty("patient.kin.phone")) != null) {
			patientVO.setKinContact(attrMap.get(env.getProperty("patient.kin.phone")).getValue());
		}
		
		if(StringUtils.isBlank(patientVO.getFirstName()) && !StringUtils.isBlank(patientVO.getSurname())) {
			patientVO.setFirstName(patientVO.getSurname());
		}
		
		if(!StringUtils.isBlank(patientVO.getFirstName()) && StringUtils.isBlank(patientVO.getSurname())) {
			patientVO.setSurname(patientVO.getFirstName());
		}

		return patientVO;
	}

	private Map<String, TrackedEntityInstanceAttribute> getTrackedEntityInstanceAttributeMap(
			List<TrackedEntityInstanceAttribute> attributes) {

		Map<String, TrackedEntityInstanceAttribute> attr = new HashMap<>();

		for (TrackedEntityInstanceAttribute a : attributes) {
			attr.put(a.getAttribute(), a);
		}

		return attr;
	}

	private Map<String, DataValue> getDataValueMap(List<DataValue> values) {

		Map<String, DataValue> valueMap = new HashMap<>();

		for (DataValue value : values) {
			valueMap.put(value.getDataElement(), value);
		}

		return valueMap;
	}

	private String getSpecimenType(Map<String, DataValue> values) {
		
		String specimenType = "";
		
		if(values.get(env.getProperty("lab.specimen.type.nasal")) != null) {
			specimenType = SpecimenType.NASAL.getValue();
			
		} else if(values.get(env.getProperty("lab.specimen.type.npa")) != null) {
			specimenType = SpecimenType.NPA.getValue();
			
		} else if(values.get(env.getProperty("lab.specimen.type.op")) != null) {
			specimenType = SpecimenType.OP.getValue();
			
		} else if(values.get(env.getProperty("lab.specimen.type.bal")) != null) {
			specimenType = SpecimenType.BAL.getValue();
			
		} else if(values.get(env.getProperty("lab.specimen.type.combined.npop")) != null) {
			specimenType = SpecimenType.NP_OP.getValue();
			
		} else if(values.get(env.getProperty("lab.specimen.type.np")) != null) {
			specimenType = SpecimenType.NP.getValue();
			
		} else if(values.get(env.getProperty("lab.specimen.type.sputum")) != null) {
			specimenType = SpecimenType.SPUTUM.getValue();
			
		} else if(values.get(env.getProperty("lab.specimen.type.other")) != null) {
			specimenType = values.get(env.getProperty("lab.specimen.type.other")).getValue();
			
		} 

		return specimenType;
	}

	private String getSymptoms(Map<String, DataValue> values) {
		
		StringBuilder builder = new StringBuilder();

		if(values.get(env.getProperty("lab.symptoms.apnoea")) != null) {
			builder.append(Symptoms.APNOEA);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.cough")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.COUGH);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.diarrhoea")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.DIARRHOEA);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.fever")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.FEVER);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.none")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.NONE);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.paroxysmal")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.PAROXYSMAL_WHOOP);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.breath.shortness")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.SHORT_BREATH);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.sore.throat")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.SORE_THROAT);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.stiff.neck")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.STIFF_NECK);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.unknown")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.UNKNOWN);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.vomiting")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(Symptoms.VOMITTING);
			
		}
		
		if(values.get(env.getProperty("lab.symptoms.other")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.symptoms.other")).getValue());
			
		}
				
		return builder.toString();
	}

	/**
	 * Get the underlying risk factors
	 * 
	 * @param values
	 * @return
	 */
	private String getRiskFactors(Map<String, DataValue> values) {

		StringBuilder builder = new StringBuilder();
		
		if(values.get(env.getProperty("lab.underlying.asthma")) != null) {
			builder.append(values.get(env.getProperty("lab.underlying.asthma")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.chronic.lung")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.chronic.lung")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.diabetes")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.diabetes")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.hiv")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.hiv")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.none")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.none")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.stiff.neck")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.stiff.neck")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.tb")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.tb")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.unknown")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.unknown")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.pregnancy")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.pregnancy")).getValue());
			
		}
		
		if(values.get(env.getProperty("lab.underlying.other")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.underlying.other")).getValue());
			
		}
		
		return builder.toString();
	}

	/**
	 * Find the type of test to be conducted
	 * 
	 * @param values
	 * @return
	 */
	private String getTestType(Map<String, DataValue> values) {

		StringBuilder builder = new StringBuilder();
		
		if(values.get(env.getProperty("lab.test.avian")) != null) {
			builder.append(TestType.AVIAN);
			
		}
		
		if(values.get(env.getProperty("lab.test.influenza.RSV")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(TestType.FLU_RSV);
			
		}
		
		if(values.get(env.getProperty("lab.test.mers")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(TestType.MERS_COV);
			
		}
		
		if(values.get(env.getProperty("lab.test.neonatal")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(TestType.NEONATAL);
			
		}
		
		if(values.get(env.getProperty("lab.test.cov2")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(TestType.SARS_COV2);
			
		}
		
		if(values.get(env.getProperty("lab.test.other")) != null) {
			
			if(builder.length() > 0) {
				builder.append(";");
			}
			
			builder.append(values.get(env.getProperty("lab.test.other")).getValue());
			
		}
		
		return builder.toString();
	}

	/**
	 * Get submitter
	 * 
	 * @param values
	 * @return
	 */
	private String getSubmitter(Map<String, DataValue> values) {

		String submitter = "";

		if (values.get(env.getProperty("lab.submitter.surname")) != null) {
			submitter = values.get(env.getProperty("lab.submitter.surname")).getValue();
		}

		if (values.get(env.getProperty("lab.submitter.firstname")) != null) {

			if (!StringUtils.isBlank(submitter)) {
				submitter = submitter + " " + values.get(env.getProperty("lab.submitter.firstname")).getValue();
			} else {

				submitter = values.get(env.getProperty("lab.submitter.firstname")).getValue();
			}
		}

		return submitter;
	}

	/**
	 * We need to convert an event into a SpecimenVO. To do this, we must first get
	 * a TrackedEntityInstance to find the PatientVO. This can be done by getting
	 * the PatientVO from the Redcap side if it already existed or by creating a new
	 * one based on the information contained in the TrackedEntityInstance
	 *
	 * @param event
	 * @return
	 */
	private SpecimenVO eventToSpecimen(Event event, boolean skipResulted) {

		Map<String, DataValue> values = getDataValueMap((List<DataValue>) event.getDataValues());
		/**
		 * If the results have already been produced, no need to pull the data or the barcode does not exist,
		 * or if the specimen has 
		 */
		DataValue labResults = values.get(env.getProperty("lab.results").trim());		
		boolean resultsCheckOk = false;
		
		if(!skipResulted) { 
			//logger.debug("Resulted specimen not skipped");
			resultsCheckOk = true;
		} else {
			if(labResults == null || StringUtils.isBlank(labResults.getValue()) || labResults.getValue().equals("PENDING")) {
				resultsCheckOk = true;
			} else {
				resultsCheckOk = false;
			}
		}
		
		if (!resultsCheckOk || values.get(env.getProperty("lab.specimen.barcode").trim()) == null) {
			//logger.debug("Results check failed");
			//logger.debug(event.toString());
			return null;
		}

		SpecimenVO specimen = new SpecimenVO();
		specimen.setEvent(event.getEvent());		
		specimen.setCreated(event.getCreated());
		specimen.setLastUpdated(event.getLastUpdated());

		if (values.get(env.getProperty("lab.submitter.facility").trim()) != null) {
			specimen.setDispatchLocation(values.get(env.getProperty("lab.submitter.facility").trim()).getValue());
		}
		
		if (values.get(env.getProperty("lab.specimen.facility").trim()) != null) {
			specimen.setPatientFacility(values.get(env.getProperty("lab.specimen.facility").trim()).getValue());
		}
		
		if (values.get(env.getProperty("lab.submitter.city").trim()) != null) {
			
			specimen.setDispatcherCity(values.get(env.getProperty("lab.submitter.city").trim()).getValue());
		}
		
		if (values.get(env.getProperty("lab.submitter.email").trim()) != null) {
			
			specimen.setDispatcherEmail(values.get(env.getProperty("lab.submitter.email").trim()).getValue());
		}
		
		if (values.get(env.getProperty("lab.submitter.contact").trim()) != null) {
			
			specimen.setDispatcherContact(values.get(env.getProperty("lab.submitter.contact").trim()).getValue());
		}
		
		// Get the barcode
		if (values.get(env.getProperty("lab.specimen.barcode").trim()) != null) {
			String barcode = values.get(env.getProperty("lab.specimen.barcode").trim()).getValue();
			specimen.setSpecimenBarcode(barcode.toUpperCase()); // Ensure standard barcodes
		}

		// Get the covid number
		if (values.get(env.getProperty("lab.covid.number").trim()) != null) {
			String covidNUmber = values.get(env.getProperty("lab.covid.number").trim()).getValue();
			
			specimen.setCovidNumber(covidNUmber);
			
			if(StringUtils.isBlank(specimen.getSpecimenBarcode())) {
				specimen.setSpecimenBarcode(covidNUmber.replaceAll("[^a-zA-Z0-9]", ""));
			}						
		}

		specimen.setDispatcher(getSubmitter(values));

		try {
			if (values.get(env.getProperty("lab.specimen.date.collection").trim()) != null) {
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
				String date = values.get(env.getProperty("lab.specimen.date.collection").trim()) == null ? ""
						: values.get(env.getProperty("lab.specimen.date.collection").trim()).getValue();

				String time = values.get(env.getProperty("lab.specimen.collection.time").trim()) == null ? " 00:00"
						: " " + values.get(env.getProperty("lab.specimen.collection.time").trim()).getValue();
				if (date.trim().length() > 0) {
					date = date + time;
					Date dt = format.parse(date.trim());
					Calendar cal = Calendar.getInstance();
					cal.setTime(dt);
					specimen.setCollectionDateTime(cal.getTime());
				}
			}

			if (values.get(env.getProperty("lab.specimen.date.received")) != null) {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

				Date dt = values.get(env.getProperty("lab.specimen.date.received").trim()) == null ? null
						: format.parse(values.get(env.getProperty("lab.specimen.date.received").trim()).getValue());

				specimen.setReceivingDateTime(dt);
			}
			

		} catch (ParseException e) {
			e.printStackTrace();
		}

		specimen.setSpecimenType(getSpecimenType(values));
		specimen.setSymptom(this.getSymptoms(values));
		
		specimen.setTestType(this.getTestType(values));
		//specimen.setRiskFacors(this.getRiskFactors(values));

		return specimen;
	}
	
	/**
	 * Load data elements from DHIS2
	 * @param elementId
	 * @return
	 */
	public DataElement getDataElement(String elementId) {

		return restTemplate().getForObject(dhis2Url + "/dataElements/" + elementId, DataElement.class);
	}
		
	/**
	 * Get bulk patients information. We call the trackedEntityInstances resource
	 * with a list of teis separated by ;
	 * 
	 * @param tei
	 * @return
	 */
	private Map<String, PatientVO> getPatientMap(Map<String, String> teis) {
		Map<String, PatientVO> map = new HashMap<String, PatientVO>();
		
		
		List<String> tmp = new ArrayList<String>();
		tmp.addAll(teis.values());
		
		// TO limit the GET URL size, we only get a maximum of 100 at a time
		for(int i = 0; i < tmp.size(); i = i + 100) {
			StringBuilder builder = new StringBuilder();
			for(int j = 0; (j < 100 && (i + j) < tmp.size()); j++) {
				if(builder.length() > 0) {
					builder.append(";");
				}
				
				builder.append(tmp.get(i + j));	
			}
			List<TrackedEntityInstance> teiList = getTrackedEntityInstanceList(builder.toString());
			
			for(TrackedEntityInstance tei : teiList) {
				map.put(tei.getTrackedEntityInstance(), trackedEntityInstanceToPatientVO(tei));
			}
		}
		
		return map;
	}

	/**
	 * Load specimen from DHIS2
	 * 
	 * Be careful not to query the dhis too many times. This happened when we were getting the 
	 * patient information for each specimen. To avoid it, we make a call to get multiple
	 * tracked entity instances at the same time.
	 * 
	 * @param parameters
	 * @return
	 */
	public List<SpecimenVO> getSpecimen(Map<String, String> parameters) {
		List<Event> events = this.getEvents(parameters);
		return getSpecimen(events, true);
	}
	
	public List<SpecimenVO> getSpecimen(List<Event> events, boolean skipResulted) {
				
		logger.info("Found " + events.size() + " events.");
		this.numPulled = events.size();
		
		// Final specimen list
		List<SpecimenVO> specimen = new ArrayList<>();
		
		// Temporary specimen list before getting the patient information
		List<SpecimenVO> tmp = new ArrayList<SpecimenVO>();
		
		// A map of specimen barcode to tracked entity instance id where 
		// the key is the barcode and the value is the instance id
		Map<String, String> teis = new HashMap<String, String>();

		for (Event event : events) {
		
			SpecimenVO s = eventToSpecimen(event, skipResulted);

			if (s == null) {
				continue;
			}
			
			tmp.add(s);
			teis.put(s.getSpecimenBarcode(), event.getTrackedEntityInstance());
			
		}
		
		// Get the patient map
		// We have to fetch a batch of organisation units
		Map<String, PatientVO> pmap = getPatientMap(teis);
		Map<String, OrganisationUnit> orgUnits = new HashMap<String, OrganisationUnit>();
		Set<String> orgIds = new HashSet<>();
		
		for(SpecimenVO sp : tmp) {
			if (specimenService.findSpecimenByBarcode(sp.getSpecimenBarcode()) == null) {
				if(!StringUtils.isBlank(sp.getDispatchLocation())) {
					orgIds.add(sp.getDispatchLocation());
				}
				
				if(!StringUtils.isBlank(sp.getPatientFacility())) {
					orgIds.add(sp.getPatientFacility());					
				}
			}
		}
		
		for(OrganisationUnit unit : getOrganisationUnits(orgIds)) {
			orgUnits.put(unit.getId(), unit);
		}
				
		for(SpecimenVO sp : tmp) {
			String barcode = sp.getSpecimenBarcode();
			String tei = teis.get(barcode);
			PatientVO patientVO = pmap.get(tei);
			
			if (patientVO != null && patientVO.getId() == null) {
				if(StringUtils.isBlank(patientVO.getIdentityNo())) {
					patientVO.setIdentityNo(barcode);
				}
				
				/// We cannot do anything if both the patient and specimen have no
				/// identifying numbers
				if(StringUtils.isBlank(patientVO.getIdentityNo())) {
					continue;
				}
								
				PatientVO p = patientService.findByIdentityNo(patientVO.getIdentityNo());
				if (p != null) {
					patientVO = p;
				}
			}
			
			try {
				if (patientVO.getId() == null) {

					patientVO = patientService.savePatient(patientVO);										
				} 
				
				// Should not try to save the same specimen twice
				SpecimenVO st = specimenService.findSpecimenByBarcode(sp.getSpecimenBarcode());
				if ( st == null || StringUtils.isBlank(st.getEvent())) {
					
					if(st != null && st.getId() != null) {
						sp.setId(st.getId());
						
					}
					
					// Only get the facilities if this is a new specimen
					OrganisationUnit unit = null;
					if(!StringUtils.isBlank(sp.getDispatchLocation())) {
						unit = orgUnits.get(sp.getDispatchLocation());
						sp.setDispatchLocation(unit != null ? unit.getName() : null);
					}
					
					if(!StringUtils.isBlank(sp.getPatientFacility())) {

						unit = orgUnits.get(sp.getPatientFacility());
						sp.setPatientFacility(unit != null ? unit.getName() : null);
					}
					
					sp.setPatient(patientVO);
					sp = specimenService.saveSpecimen(sp);
						
					// We should set the information for the lab report project
					RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
					criteria.setFieldName("lab_rec_barcode_%");
					criteria.setValue(sp.getSpecimenBarcode());
					criteria.setProjectId(labReceptionPID);
					
					Collection<RedcapDataVO> data = redcapDataService.searchByCriteria(criteria);
					
					if(data == null) {
						sp.setDhis2Synched(true);
					} else {
						sp.setDhis2Synched(false);
					}
					sp = specimenService.saveSpecimen(sp);
					
				} else {
					continue;
				}
				
				specimen.add(sp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		redcapLink.postSpecimen(specimen, labReportPID);
		return specimen;
	}
		
	/**
	 * Load organisation unit from DHSI2 using the id
	 * @param id
	 * @return
	 */
	public List<OrganisationUnit> getOrganisationUnits(Set<String> ids) {
		if(ids.size() == 0) {
			return new ArrayList<>();
		}
		StringBuilder builder = new StringBuilder();
		for(String id : ids) {
			if(builder.length() > 0) {
				builder.append(",");
			}
			
			builder.append(id);
		}
		OrganisationUnitList orgList = restTemplate().getForObject(dhis2Url + "/organisationUnits?filter=id:in:[" + builder.toString() + "]", OrganisationUnitList.class);
		return (List<OrganisationUnit>) orgList.getOrganisationUnits();
	}

	/**
	 * Get one specimen from the staging area
	 * @param barcode
	 * @return
	 */
	public SpecimenVO getOneSpecimen(String barcode) {

		return specimenService.findSpecimenByBarcode(barcode);
	}
	
	public String getSpecimenFieldsString(SpecimenVO specimen) {

		StringBuilder builder = new StringBuilder();
		for(DDPObjectField field : getSpecimenFieldList(specimen)) {
			if(builder.length() > 0) {
				builder.append(",");
			}
			builder.append(field.toString());
		}
		String str = "[" + builder.toString() + "]";
		
		return str;
	}

	/**
	 * Convert the specimen into a JSON string
	 * @param specimen
	 * @return
	 */
	public List<DDPObjectField> getSpecimenFieldList(SpecimenVO specimen) {

		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();

		if (specimen == null) {
			return fields;
		}
				
		/// Patient demographics
		if(specimen.getPatient() != null ) {
			if (specimen.getPatient().getDateOfBirth() != null) {
				
				Instant dob = specimen.getPatient().getDateOfBirth().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault());
				fields.add(new DDPObjectField("date_birth", formatter.format(dob), null));
			}
	
			if(!StringUtils.isBlank(specimen.getPatient().getFirstName())) { 
				fields.add(new DDPObjectField("patient_first_name", specimen.getPatient().getFirstName(), null));
			}
			
			if(specimen.getPatient().getSex() != null) {
				if(specimen.getPatient().getSex().equals("MALE")) {
					fields.add(new DDPObjectField("sex", "1", null));
				} else if(specimen.getPatient().getSex().equals("FEMALE")) {
					fields.add(new DDPObjectField("sex", "2", null));
				} else {
					fields.add(new DDPObjectField("sex", "3", null));
				}
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getSurname())) {
				fields.add(new DDPObjectField("patient_surname", specimen.getPatient().getSurname(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getContactNumber())) {
				fields.add(new DDPObjectField("patient_contact", specimen.getPatient().getContactNumber(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getNationality())) {
				fields.add(new DDPObjectField("patient_nationality", specimen.getPatient().getNationality(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getIdentityNo())) {
				fields.add(new DDPObjectField("national_id", specimen.getPatient().getIdentityNo(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getCity())) {
				fields.add(new DDPObjectField("patient_city", specimen.getPatient().getCity(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getCountryDeparture())) {
				fields.add(new DDPObjectField("patient_departure_country", specimen.getPatient().getCountryDeparture(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getNextOfKin())) {
				fields.add(new DDPObjectField("patient_kin", specimen.getPatient().getNextOfKin(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getKinContact())) {
				fields.add(new DDPObjectField("patient_kin_contact", specimen.getPatient().getKinContact(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getTransportRegistration())) {
				fields.add(new DDPObjectField("patient_transport_registration", specimen.getPatient().getTransportRegistration(), null));
			}

			if (specimen.getPatient().getDepartureDate() != null) {
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(specimen.getPatient().getDepartureDate());

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH).withZone(ZoneId.systemDefault());
				Instant depDate = specimen.getPatient().getDepartureDate().toInstant();
				String date = formatter.format(depDate);
				
				fields.add(new DDPObjectField("patient_departure_date", date, null));
			}
			
		}
		/// Specimen details
		if(!StringUtils.isBlank(specimen.getSpecimenBarcode())) {
			fields.add(new DDPObjectField("specimen_barcode", specimen.getSpecimenBarcode(), null));
		}
		
		if(!StringUtils.isBlank(specimen.getCovidNumber())) {
			fields.add(new DDPObjectField("ipms_lab_covid_number", specimen.getCovidNumber(), null));
		}
		
		if(!StringUtils.isBlank(specimen.getBatchNumber())) {
			fields.add(new DDPObjectField("batch_number", specimen.getBatchNumber(), null));
		}

		if (specimen.getDispatchDate() != null) {
			fields.add(new DDPObjectField("date_dispatched", specimen.getDispatchDate().toString(), null));
		}

		if (specimen.getDispatchTime() != null) {
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(specimen.getDispatchTime());

			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int mins = cal.get(Calendar.MINUTE);
			
			//String time = (hour < 10 ? "0" + hour : hour) + ":" + (mins < 10 ? "0" + mins : mins) ;
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());			
			String time = formatter.format(specimen.getDispatchTime().toInstant());
			
			fields.add(new DDPObjectField("time_dispatched", time, null));
		}
		
		if (!StringUtils.isBlank(specimen.getDispatcher())) {
			fields.add(new DDPObjectField("specimen_dispatcher", specimen.getDispatcher(), null));
		}

		if (!StringUtils.isBlank(specimen.getSampleStatusDispatch())) {
			fields.add(new DDPObjectField("specimen_status_dispatch", specimen.getSampleStatusDispatch(), null));
		}

		if (!StringUtils.isBlank(specimen.getDispatchLocation())) {
			fields.add(new DDPObjectField("dispatch_facility", specimen.getDispatchLocation(), null));
		}

		if (!StringUtils.isBlank(specimen.getPatientFacility())) {
			fields.add(new DDPObjectField("patient_facility", specimen.getPatientFacility(), null));
		}
		
		
		if (!StringUtils.isBlank(specimen.getDispatcherCity())) {
			fields.add(new DDPObjectField("specimen_dispatcher_city", specimen.getDispatcherCity(), null));
		}

		if (!StringUtils.isBlank(specimen.getDispatcherContact())) {
			fields.add(new DDPObjectField("specimen_dispatcher_contact", specimen.getDispatcherContact(), null));
		}

		if (!StringUtils.isBlank(specimen.getDispatcherEmail())) {
			fields.add(new DDPObjectField("specimen_dispatcher_email", specimen.getDispatcherEmail(), null));
		}
		
		if (!StringUtils.isBlank(specimen.getReceivingPersonnel())) {
			fields.add(new DDPObjectField("receiving_personnel", specimen.getReceivingPersonnel(), null));
		}

		if (specimen.getReceivingDateTime() != null) {
			
			Instant receivingDate = specimen.getReceivingDateTime().toInstant();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());
			String datetime = formatter.format(receivingDate);
			fields.add(new DDPObjectField("receiving_datetime", datetime, null));
		}

		if (specimen.getCollectionDateTime() != null) {
			
			Instant collectionDate = specimen.getCollectionDateTime().toInstant();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());
			String datetime = formatter.format(collectionDate);
			fields.add(
					new DDPObjectField("date_specimen_collected", datetime, null));
		}
		
		if (!StringUtils.isBlank(specimen.getReceivingConditionCode())) {
			fields.add(new DDPObjectField("receiving_condition_code", specimen.getReceivingConditionCode(), null));
		}
				
		if (!StringUtils.isBlank(specimen.getResults())) {
			
			String results = "";
			
			if(specimen.getResults().equals("1")) {
				results = "POSITIVE";
			} else if(specimen.getResults().equals("2")) {
				results = "NEGATIVE";
			} else if(specimen.getResults().equals("3")) {
				results = "INCONCLUSIVE";
			}else if(specimen.getResults().equals("4")) {
				results = "PENDING";
			}
			
			fields.add(new DDPObjectField("specimen_results", results, null));
		}

		if (!StringUtils.isBlank(specimen.getSpecimenType())) {
			fields.add(new DDPObjectField("specimen_type", specimen.getSpecimenType(), null));
		}

		if (!StringUtils.isBlank(specimen.getTestType())) {
			fields.add(new DDPObjectField("specimen_test_type", specimen.getTestType(), null));
		}

		if (!StringUtils.isBlank(specimen.getRiskFactors())) {
			fields.add(new DDPObjectField("patient_risk_factors", specimen.getRiskFactors(), null));
		}

		if (!StringUtils.isBlank(specimen.getResultsEnteredBy())) {
			fields.add(new DDPObjectField("results_entered_by", specimen.getResultsEnteredBy(), null));
		}

		if (specimen.getResultsEnteredDate() != null) {
			
			Instant verifiedInstant = specimen.getResultsEnteredDate().toInstant();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());
			String datetime = formatter.format(verifiedInstant);
			fields.add(new DDPObjectField("results_entered_date", datetime, null));
		}

		if (!StringUtils.isBlank(specimen.getResultsVerifiedBy())) {
			fields.add(new DDPObjectField("results_verifies_by", specimen.getResultsVerifiedBy(), null));
		}

		if (specimen.getResultsVerifiedDate() != null) {
						
			Instant collectionDate = specimen.getResultsVerifiedDate().toInstant();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());
			String datetime = formatter.format(collectionDate);
			
			fields.add(new DDPObjectField("results_verified_date", datetime, null));
		}

		if (!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
			fields.add(new DDPObjectField("results_authorised_by", specimen.getResultsAuthorisedBy(), null));
		}

		if (specimen.getResultsAuthorisedDate() != null) {

			Instant authDate = specimen.getResultsAuthorisedDate().toInstant();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH).withZone(ZoneId.systemDefault());
			String datetime = formatter.format(authDate);
			fields.add(new DDPObjectField("results_authorised_date", datetime, null));
		}

		if (!StringUtils.isBlank(specimen.getNotes())) {
			fields.add(new DDPObjectField("specimen_notes", specimen.getNotes(), null));
		}
		
		// Reception project
		List<DDPObjectField> newf = getReceptionFormFields(specimen.getSpecimenBarcode());
		fields.addAll(newf);

		// Reception TPOR form
		newf = getTporFormFields(specimen.getSpecimenBarcode());
		fields.addAll(getTporFormFields(specimen.getSpecimenBarcode()));
		
		// Extraction form
		newf = getExtractionFormFields(specimen.getSpecimenBarcode());
		fields.addAll(newf);
		
		// Detection form
		newf = getDetectionFormFields(specimen.getSpecimenBarcode());
		fields.addAll(newf);
		
		// Resulting form
		newf = getResultingFormFields(specimen.getSpecimenBarcode());
		fields.addAll(newf);

		// Verification form
		newf = getVerificationFormFields(specimen.getSpecimenBarcode());
		fields.addAll(newf);

		return fields;
	}
	
	private DDPObjectField getCriteriaField(RedcapDataSearchCriteria criteria) {
		List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
		if(redcapDataVOs != null && redcapDataVOs.size() > 0 ) {
			RedcapDataVO rd = redcapDataVOs.get(0);
			return new DDPObjectField(criteria.getFieldName(), rd.getValue(), null);
		}
		
		return null;
	}
	
	public List<DDPObjectField> getVerificationFormFields(String barcode) {
		
		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();
		DDPObjectField field;
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		// Reception project
		criteria.setProjectId(labResultingPID);
		criteria.setFieldName("test_det_barcode_%");
		criteria.setValue(barcode);
		
		List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
		field = getCriteriaField(criteria);
		if(redcapDataVOs != null && redcapDataVOs.size() > 0 ) {
			RedcapDataVO rd = redcapDataVOs.get(0);
			int pindex = rd.getFieldName().lastIndexOf("_");
			String pos = rd.getFieldName().substring(pindex + 1);
			
			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labResultingPID);
			criteria.setRecord(rd.getRecord());
			criteria.setEventId(rd.getEventId());
			criteria.setFieldName("test_verify_personnel");
			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_verify_datetime");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_verify_batchsize");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("covid_rna_results" + pos);			
			field = getCriteriaField(criteria);
			if(field != null) {
				field.setField("covid_rna_results");
				fields.add(field);
			}
			
			criteria.setFieldName("test_verify_result_" + pos);			
			field = getCriteriaField(criteria);
			if(field != null) {
				field.setField("test_verify_result");
				fields.add(field);
			}
			
			criteria.setFieldName("verification_complete");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
		}
		return fields;
		
	}
	
	public List<DDPObjectField> getResultingFormFields(String barcode) {
		
		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();
		DDPObjectField field;
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		// resulting project
		criteria.setProjectId(labResultingPID);
		criteria.setFieldName("test_det_barcode_%");
		criteria.setValue(barcode);
		
		List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
		field = getCriteriaField(criteria);
		if(redcapDataVOs != null && redcapDataVOs.size() > 0 ) {
			RedcapDataVO rd = redcapDataVOs.get(0);
			int pindex = rd.getFieldName().lastIndexOf("_");
			String pos = rd.getFieldName().substring(pindex + 1);
			
			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labResultingPID);
			criteria.setRecord(rd.getRecord());
			criteria.setEventId(rd.getEventId());
			criteria.setFieldName("test_assay_personnel");
			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_assay_datetime");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_assay_result_" + pos);			
			field = getCriteriaField(criteria);
			if(field != null) {
				field.setField("test_assay_result");
				fields.add(field);
			}
			
			criteria.setFieldName("test_assay_batch_id");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_assay_result_why");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("resulting_complete");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
		}
		return fields;
		
	}

	public List<DDPObjectField> getDetectionFormFields(String barcode) {
		
		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();
		DDPObjectField field;
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		// Reception project
		criteria.setProjectId(labResultingPID);
		criteria.setFieldName("test_det_barcode_%");
		criteria.setValue(barcode);
		
		List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
		field = getCriteriaField(criteria);
		if(redcapDataVOs != null && redcapDataVOs.size() > 0 ) {
			RedcapDataVO rd = redcapDataVOs.get(0);
			int pindex = rd.getFieldName().lastIndexOf("_");
			String pos = rd.getFieldName().substring(pindex + 1);
			
			fields.add(new DDPObjectField("test_det_batch_id", rd.getRecord(), null));
			fields.add(new DDPObjectField("test_det_barcode", rd.getValue(), null));
			fields.add(new DDPObjectField("det_batch_pos", pos, null));
									
			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labResultingPID);
			criteria.setRecord(rd.getRecord());
			criteria.setEventId(rd.getEventId());
			criteria.setFieldName("test_det_personnel");
			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_det_datetime");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_det_batchsize");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("detection_lab");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_det_instrument");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
		}
		return fields;
		
	}
	
	public List<DDPObjectField> getExtractionFormFields(String barcode) {
		
		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();
		DDPObjectField field;
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		// Reception project
		criteria.setProjectId(labExtractionPID);
		criteria.setFieldName("test_ext_barcode_%");
		criteria.setValue(barcode);
		
		List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
		field = getCriteriaField(criteria);
		if(redcapDataVOs != null && redcapDataVOs.size() > 0 ) {
			RedcapDataVO rd = redcapDataVOs.get(0);
			int pindex = rd.getFieldName().lastIndexOf("_");
			String pos = rd.getFieldName().substring(pindex + 1);
			
			fields.add(new DDPObjectField("ext_batch_pos", pos, null));
			fields.add(new DDPObjectField("test_ext_barcode", rd.getValue(), null));
			fields.add(new DDPObjectField("test_ext_batch_id", rd.getRecord(), null));
						
			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labExtractionPID);
			criteria.setRecord(rd.getRecord());
			criteria.setEventId(rd.getEventId());
			criteria.setFieldName("test_ext_datetime");
			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_ext_personnel");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_ext_batchsize");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("extraction_lab");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			int instPos = ((Integer.parseInt(pos) -1)/24) + 1;
			
			criteria.setFieldName("test_ext_instrument" + instPos);			
			field = getCriteriaField(criteria);
			if(field != null) {
				field.setField("test_ext_instrument");
				fields.add(field);
			}
			
		}
		return fields;
		
	}
	
	public List<DDPObjectField> getTporFormFields(String barcode) {
		
		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();
		DDPObjectField field;
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		// Reception project
		criteria.setProjectId(labReceptionPID);
		criteria.setFieldName("test_tpor_barcode_%");
		criteria.setValue(barcode);
		
		List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
		field = getCriteriaField(criteria);
		if(redcapDataVOs != null && redcapDataVOs.size() > 0 ) {
			RedcapDataVO rd = redcapDataVOs.get(0);
			int pindex = rd.getFieldName().lastIndexOf("_");
			String pos = rd.getFieldName().substring(pindex + 1);
			fields.add(new DDPObjectField("tpor_batch_pos", pos, null));
			fields.add(new DDPObjectField("test_tpor_batch_id", rd.getRecord(), null));
			
			fields.add(new DDPObjectField("test_tpor_barcode", rd.getValue(), null));
						
			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labReceptionPID);
			criteria.setRecord(rd.getRecord());
			criteria.setEventId(rd.getEventId());
			criteria.setFieldName("test_tpor_datetime");
						
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_tpor_personnel");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("test_tpor_batchsize");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("tpor_lab");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
		}
		return fields;
		
	}
	
	/**
	 * Get the fields from the reception form on the Reception project
	 * 
	 * @param barcode
	 * @return
	 */
	public List<DDPObjectField> getReceptionFormFields(String barcode) {
		
		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();
		DDPObjectField field;
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		// Reception project
		criteria.setProjectId(labReceptionPID);
		criteria.setFieldName("lab_rec_barcode_%");
		criteria.setValue(barcode);
		
		List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
		field = getCriteriaField(criteria);
		if(redcapDataVOs != null && redcapDataVOs.size() > 0 ) {
			RedcapDataVO rd = redcapDataVOs.get(0);
			int pindex = rd.getFieldName().lastIndexOf("_");
			String pos = rd.getFieldName().substring(pindex + 1);
			fields.add(new DDPObjectField("lab_rec_id", rd.getRecord(), null));
			
			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labReceptionPID);
			criteria.setRecord(rd.getRecord());
			criteria.setEventId(rd.getEventId());
			criteria.setFieldName("received_datetime");
			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("receiving_personnel");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("receiving_lab");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("lab_rec_batchsize");			
			field = getCriteriaField(criteria);
			if(field != null) {
				fields.add(field);
			}
			
			criteria.setFieldName("specimen_cond_" + pos);			
			field = getCriteriaField(criteria);
			if(field != null) {
				field.setField("receiving_condition_code");
				fields.add(field);
			}
			
		}
		return fields;
		
	}
	
	private Map<String, RedcapDataVO> getRedcapDataMap(List<RedcapDataVO> data) {
		
		HashMap<String, RedcapDataVO> map = new HashMap<String, RedcapDataVO>();
		
		for(RedcapDataVO d : data) {
			map.put(d.getProjectId() + d.getEventId() + d.getRecord() + d.getFieldName(), d);
		}
		
		return map;
		
	}
	
	/**
	 * Generate a payload JSON string for updating an event with the results
	 * of the specimen
	 * 
	 * @param event
	 * @param specimen
	 * @return
	 */
	private String getEventPayloadString(Event event) {
		
		StringBuilder builder = new StringBuilder();

		builder.append("{\n");
		builder.append("\"program\": \"" + event.getProgram() + "\",\n");
		builder.append("\"orgUnit\": \""+ event.getOrgUnit() + "\",\n");
		builder.append("\"programStage\": \"" + event.getProgramStage()+ "\",\n");
		
		if(event.getCompletedDate() == null) {
			builder.append("\"completedDate\": \"null\",\n");
		} else {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");						
			String datetime = format.format(event.getCompletedDate());
			
			builder.append("\"completedDate\": \"" + datetime + "\",\n");
		}
		
		builder.append("\"completedBy\": \"" + event.getCompleteBy() + "\",\n");
		if(event.getEventDate() == null) {
			builder.append("\"eventDate\": \"null\",\n");
		} else {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");						
			String datetime = format.format(event.getEventDate());
			
			builder.append("\"eventDate\": \"" + datetime + "\",\n");
		}
		
		builder.append("\"status\": \"" + event.getStatus() + "\",\n");
		builder.append("\"trackedEntityInstance\": \"" + event.getTrackedEntityInstance() + "\",\n");
		builder.append("\"dataValues\": [\n");
		
		// Values from the the event
		StringBuilder evbuilder = new StringBuilder();
		int i = 0;
		for(DataValue value : event.getDataValues()){
			
			if(evbuilder.length() > 0) {
				evbuilder.append(", ");
			}
			
			evbuilder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(value.getDataElement()) + 
					", " + JSONObject.quote("value") + ": " + JSONObject.quote(value.getValue()) + "}\n");
		}
		
		builder.append(evbuilder.toString());
				
		builder.append("]\n");
		builder.append("}\n");
		return builder.toString();
	}
	
	/**
	 * Creation of JSON payload for the ready results from redcap to DHIS2
	 * 
	 * @param specimen
	 * @return
	 */
	public String getDhisPayload(Collection<SpecimenVO> specimen) {
		
		StringBuilder builder = new StringBuilder();
		Map<String, SpecimenVO> spMap = new HashMap<String, SpecimenVO>();
		
		// To pull all events at once we have to put the events together
		for(SpecimenVO sp : specimen) {
			if(builder.length() > 0) {
				builder.append(";");
			}
			builder.append(sp.getSpecimenBarcode());
			spMap.put(sp.getSpecimenBarcode(), sp);
		}
		
		String barcodes = builder.toString();

		builder = new StringBuilder();
		builder.append(dhis2Url);
		builder.append("/events?programStage=" + programStage);
		builder.append("&program=" + program);
		builder.append("&filter=" + barcodeField + ":IN:" + barcodes);
		//builder.append("&event=" + evIds);
		logger.info("Final url is " + builder.toString());
		
		EventList eventList = restTemplate().getForObject(builder.toString(), EventList.class);
		
		builder = new StringBuilder();
		builder.append("{\n");
		builder.append("\"events\" : [\n");
		
		for(Event event : eventList.getEvents()) {
			SpecimenVO s = eventToSpecimen(event, false);
			if(s == null) {
				logger.debug(String.format("The event could not be converted to specimen : %s\n", event.toString()));
				continue;
			}
			SpecimenVO sp = spMap.get(s.getSpecimenBarcode());
			sp.setEvent(s.getEvent());
			Map<String, DataValue> values = getDataValueMap((List<DataValue>) event.getDataValues());
			 
			// Add the result values to the event
			DataValue val = new DataValue();
			if (!StringUtils.isBlank(sp.getResults())) {
				val.setDataElement(env.getProperty("lab.results"));
				if (sp.getResults().equals("1")) {
					val.setValue("POSITIVE");
				} else if (sp.getResults().equals("2")) {
					val.setValue("NEGATIVE");
				} else if (sp.getResults().equals("3")) {
					val.setValue("INCONCLUSIVE");
				} else {
					val.setValue("PENDING");
				}
				event.getDataValues().add(val);
			}

			if (!StringUtils.isBlank(sp.getResultsEnteredBy())) {
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.entered.by"));
				val.setValue(sp.getResultsEnteredBy());
				event.getDataValues().add(val);
			}

			if (sp.getResultsEnteredDate() != null) {

				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.date.entered"));
				
				Instant dob = sp.getResultsEnteredDate().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);
				
				val.setValue(date);
				event.getDataValues().add(val);
			}

			if (!StringUtils.isBlank(sp.getResultsVerifiedBy())) {
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.verified.by"));
				val.setValue(sp.getResultsVerifiedBy());
				event.getDataValues().add(val);
			}

			if (sp.getResultsVerifiedDate() != null) {
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.result.date.verified"));
				
				Instant dob = sp.getResultsVerifiedDate().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);
				
				val.setValue(date);
				event.getDataValues().add(val);
			}

			if (!StringUtils.isBlank(sp.getResultsAuthorisedBy())) {
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.authorised.by"));
				val.setValue(sp.getResultsAuthorisedBy());
				event.getDataValues().add(val);
			}

			if (sp.getResultsAuthorisedDate() != null) {
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.date.authorised"));
				
				Instant dob = sp.getResultsAuthorisedDate().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);
				
				val.setValue(date);
				event.getDataValues().add(val);
			}

			if (sp.getReceivingDateTime() != null) {
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.specimen.date.received"));
				
				Instant dob = sp.getReceivingDateTime().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);
				
				val.setValue(date);
				event.getDataValues().add(val);
			}

			/*
			 * if(!StringUtils.isBlank(sp.getReceivingLab())) { val = new DataValue();
			 * val.setDataElement(env.getProperty("lab.results.date.authorised"));
			 * val.setValue(sp.getResultsAuthorisedDate().toString());
			 * event.getDataValues().add(val); }
			 */
			
			val = values.get(env.getProperty("lab.results"));
			DataValue authorisedBy = values.get(env.getProperty("lab.results.authorised.by"));
			DataValue authorisedDate = values.get(env.getProperty("lab.results.date.authorised"));
			
			// Only push if the results from dhis is not the same as redcap
			// AND the dhis results are not authorised and the authorised date is blank
			boolean sameResults = val != null && val.getValue().equalsIgnoreCase(sp.getResults());
			boolean synchResults = !sameResults & authorisedBy != null 
					&& !StringUtils.isBlank(authorisedBy.getValue()) 
					&& authorisedDate != null && !StringUtils.isBlank(authorisedDate.getValue());
			
			boolean synchReceiving = sp.getReceivingDateTime() != null;
			
			// Only do this if we are live. This comes from the property 'app.live'
			if ((synchResults || synchReceiving) && isLive) {

				String payload = getEventPayloadString(event);
				
				if (builder.length() > 0) {
					builder.append(", ");
				}
	
				builder.append(payload);
		
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				HttpEntity<String> entity = new HttpEntity<String>(payload, headers);
	
				String url = this.dhis2Url + "/events/" + event.getEvent();
	
				try {
					this.restTemplate().put(url, entity);
	
				} catch (HttpClientErrorException e) {
					logger.info(e.getResponseBodyAsString());
					e.printStackTrace();
				}
			}

			sp.setDhis2Synched(true);
			specimenService.saveSpecimen(sp);
		}
				
		builder.append("]\n}\n");
		
		return builder.toString();
	
	}
	
}
