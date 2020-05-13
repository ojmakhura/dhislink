package bw.ub.ehealth.controller;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
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
    
	@Autowired
	private PatientService patientService;

	@Autowired
	private SpecimenService specimenService;

	@Autowired
	private RedcapDataService redcapDataService;
	
	private int numPulled = 0;
	
	public DhisLink() {

	}
	
	public int getNumPulled() {
		return this.numPulled;
	}

	@Bean
	public RestTemplate restTemplate() {
		return builder.basicAuthentication("demo", "Demo@2020").build();
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
		logger.info(finalUrl);
		EventList eventList = restTemplate().getForObject(finalUrl, EventList.class);

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
	private SpecimenVO eventToSpecimen(Event event) {

		Map<String, DataValue> values = getDataValueMap((List<DataValue>) event.getDataValues());
		/**
		 * If the results have already been produced, no need to pull the data or the barcode does not exist
		 */
		if (values.get(env.getProperty("lab.results").trim()) != null || values.get(env.getProperty("lab.specimen.barcode").trim()) == null) {
			return null;
		}

		SpecimenVO specimen = new SpecimenVO();
		
		specimen.setEvent(event.getEvent());
		
		specimen.setCreated(event.getCreated());
		specimen.setLastUpdated(event.getLastUpdated());

		if (values.get(env.getProperty("lab.submitter.facility").trim()) != null) {
			OrganisationUnit unit = getOrganisationUnit(
					values.get(env.getProperty("lab.submitter.facility").trim()).getValue());
			specimen.setDispatchLocation(unit != null ? unit.getName() : null);
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
		

		if (values.get(env.getProperty("lab.specimen.barcode").trim()) != null) {
			
			specimen.setSpecimenBarcode(values.get(env.getProperty("lab.specimen.barcode").trim()).getValue());
		}

		// Get the covid number
		if (values.get(env.getProperty("lab.covid.number").trim()) != null
				&& StringUtils.isBlank(specimen.getSpecimenBarcode())) {
			String barcode = values.get(env.getProperty("lab.covid.number").trim()).getValue();
			specimen.setSpecimenBarcode(barcode.replaceAll("[^a-zA-Z0-9]", ""));
		}

		specimen.setDispatcher(getSubmitter(values));

		try {
			if (values.get(env.getProperty("lab.specimen.date.collection").trim()) != null) {
				// yyyy-MM-dd'T'HH:mm:ss.SSS
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
	 * Load specimen from DHIS2
	 * 
	 * @param parameters
	 * @return
	 */
	public List<SpecimenVO> getSpecimen(Map<String, String> parameters) {

		List<Event> events = this.getEvents(parameters);
		this.numPulled = events.size();
		List<SpecimenVO> specimen = new ArrayList<>();

		for (Event event : events) {
			
			SpecimenVO s = eventToSpecimen(event);

			if (s == null || s.getSpecimenBarcode() == null || s.getSpecimenBarcode().trim().length() == 0) {
				continue;
			}

			TrackedEntityInstance instance = getTrackedEntityInstance(event.getOrgUnit(),
					event.getTrackedEntityInstance());
			PatientVO patientVO = trackedEntityInstanceToPatientVO(instance);

			if (patientVO != null && patientVO.getId() == null) {
				if(StringUtils.isBlank(patientVO.getIdentityNo())) {
					patientVO.setIdentityNo(s.getSpecimenBarcode());
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
				if (specimenService.findSpecimenByBarcode(s.getSpecimenBarcode()) == null) {
					//patientVO = patientService.savePatient(patientVO);
					s.setPatient(patientService.findById(patientVO.getId()));
					s = specimenService.saveSpecimen(s);
						
					// We should set the information for the lab report project
					for(RedcapDataVO r : getSpecimenRedcapData(s)) {
						r = redcapDataService.saveRedcapData(r);
					}
					
				} else {
					continue;
				}
				
				specimen.add(s);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		return specimen;
	}
	
	/**
	 * Convert a specimen into a list of redcap data objects
	 * 
	 * @param specimen
	 * @return
	 */
	private List<RedcapDataVO> getSpecimenRedcapData(SpecimenVO specimen) {
		
		List<RedcapDataVO> data = new ArrayList<RedcapDataVO>();
		
		Long eventId = redcapDataService.findMaxEvent(labReportPID);
		
		RedcapDataVO tmp = new RedcapDataVO();
		
		if(specimen.getPatient() != null) {
						
			if (!StringUtils.isBlank(specimen.getPatient().getFirstName())) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setEventId(eventId);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("patient_first_name");
				tmp.setValue(specimen.getPatient().getFirstName());
				data.add(tmp);
			}
	
			if (!StringUtils.isBlank(specimen.getPatient().getSurname())) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setEventId(eventId);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("patient_surname");
				tmp.setValue(specimen.getPatient().getSurname());
				data.add(tmp);
			}
	
			if (!StringUtils.isBlank(specimen.getPatient().getSex())) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setEventId(eventId);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("sex");
				tmp.setValue(specimen.getPatient().getSex());
				data.add(tmp);
			}
			
			if(specimen.getPatient().getDateOfBirth() != null) {
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setEventId(eventId);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("date_birth");
				tmp.setValue(specimen.getPatient().getDateOfBirth().toString());
				data.add(tmp);
				
				// Calculate the age
				tmp = new RedcapDataVO();
				tmp.setProjectId(labReportPID);
				tmp.setEventId(eventId);
				tmp.setRecord(specimen.getSpecimenBarcode());
				tmp.setFieldName("age");
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(specimen.getPatient().getDateOfBirth());
				
				LocalDate d1 = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
				LocalDate now = LocalDate.now();
				
				Period diff = Period.between(d1, now);
				
				tmp.setValue("" + diff.getYears());
				data.add(tmp);
			}
		}
		
		if(specimen.getDispatchDate() != null ) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setEventId(eventId);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("date_dispatched");
			tmp.setValue(specimen.getDispatchDate().toString());
			data.add(tmp);
		}
		
		if(specimen.getDispatchDate() != null) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setEventId(eventId);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("date_specimen_collected");
			tmp.setValue(specimen.getCollectionDateTime().toString());
			data.add(tmp);
		}
		
		if (specimen.getLatitude() != null) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setEventId(eventId);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("gis_lat");
			tmp.setValue(specimen.getLatitude());
			data.add(tmp);
		}

		if (specimen.getLongitude() != null) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setEventId(eventId);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("gis_long");
			tmp.setValue(specimen.getLongitude());
			data.add(tmp);
		}

		if (!StringUtils.isBlank(specimen.getSpecimenBarcode())) {
			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setEventId(eventId);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("specimen_barcode");
			tmp.setValue(specimen.getSpecimenBarcode());
			data.add(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getReceivingLab())) {

			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setEventId(eventId);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("receiving_lab");
			tmp.setValue(specimen.getReceivingLab());
			data.add(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getCovidRnaResults())) {

			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setEventId(eventId);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("receiving_lab");
			tmp.setValue(specimen.getCovidRnaResults());
			data.add(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getCovidLabReportComplete())) {

			tmp = new RedcapDataVO();
			tmp.setProjectId(labReportPID);
			tmp.setEventId(eventId);
			tmp.setRecord(specimen.getSpecimenBarcode());
			tmp.setFieldName("receiving_lab");
			tmp.setValue(specimen.getCovidLabReportComplete());
			data.add(tmp);
		}
				
		return data;
		
	}
	
	public void updateLabReport(SpecimenVO specimen) {
		Long eventId = redcapDataService.findMaxEvent(labReportPID);
		RedcapDataVO tmp = new RedcapDataVO();
		tmp.setProjectId(labReportPID);
		tmp.setEventId(eventId);
		tmp.setRecord(specimen.getSpecimenBarcode());
				
		if(!StringUtils.isBlank(specimen.getBatchNumber())) {

			tmp.setFieldName("batch_number");
			tmp.setValue(specimen.getBatchNumber());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getCovidRnaResults())) {

			tmp.setFieldName("covid_rna_results");
			tmp.setValue(specimen.getCovidRnaResults());			
			redcapDataService.saveRedcapData(tmp);

			tmp.setFieldName("covid19_lab_report_complete");
			tmp.setValue("2");			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getDetectionBatchPosition())) {

			tmp.setFieldName("det_batch_position");
			tmp.setValue(specimen.getDetectionBatchPosition());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getDetectionLab())) {

			tmp.setFieldName("detection_lab");
			tmp.setValue(specimen.getDetectionLab());
			
			redcapDataService.saveRedcapData(tmp);
		}		
		
		if(!StringUtils.isBlank(specimen.getExtractionBatchPosition())) {

			tmp.setFieldName("ext_batch_position");
			tmp.setValue(specimen.getExtractionBatchPosition());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getExtractionLab())) {

			tmp.setFieldName("extraction_lab");
			tmp.setValue(specimen.getExtractionLab());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getReceivingConditionCode())) {

			tmp.setFieldName("receiving_condition_code");
			tmp.setValue(specimen.getReceivingConditionCode());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getReceivingLab())) {

			tmp.setFieldName("receiving_lab");
			tmp.setValue(specimen.getReceivingLab());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(specimen.getReceivingDateTime() != null) {

			tmp.setFieldName("received_datetime");
			tmp.setValue(specimen.getReceivingDateTime().toString());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getReceivingLab())) {

			tmp.setFieldName("receiving_lab");
			tmp.setValue(specimen.getReceivingLab());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getReceivingConditionCode())) {

			tmp.setFieldName("receiving_condition_code");
			tmp.setValue(specimen.getReceivingConditionCode());
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {

			//tmp.setFieldName("receiving_lab");
			//tmp.setValue(specimen.getResultsAuthorisedBy());
			
			tmp.setFieldName("result_authorised");
			tmp.setValue("1");
			
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestAssayBatchId())) {

			tmp.setFieldName("test_assay_batch_id");
			tmp.setValue(specimen.getTestAssayBatchId());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestAssayBatchsize())) {

			tmp.setFieldName("test_assay_batchsize");
			tmp.setValue(specimen.getTestAssayBatchsize());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(specimen.getTestAssayDatetime() != null) {

			tmp.setFieldName("test_assay_datetime");
			tmp.setValue(specimen.getTestAssayDatetime().toString());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestAssayPersonnel())) {

			tmp.setFieldName("test_assay_personnel");
			tmp.setValue(specimen.getTestAssayPersonnel());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestAssayResult())) {

			tmp.setFieldName("test_assay_result");
			tmp.setValue(specimen.getTestAssayResult());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestAssayPosition())) {

			tmp.setFieldName("test_assay_result");
			tmp.setValue(specimen.getTestAssayPosition());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getSpecimenBarcode())) {

			tmp.setFieldName("test_det_barcode");
			tmp.setValue(specimen.getSpecimenBarcode());
						
			redcapDataService.saveRedcapData(tmp);
			
			tmp.setFieldName("test_ext_barcode");						
			redcapDataService.saveRedcapData(tmp);
			
			tmp.setFieldName("test_tpor_barcode");						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(specimen.getTestDetectionDatetime() != null) {

			tmp.setFieldName("test_det_datetime");
			tmp.setValue(specimen.getTestDetectionDatetime().toString());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestDetectionBatchsize())) {

			tmp.setFieldName("test_det_batchsize");
			tmp.setValue(specimen.getTestDetectionBatchsize());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestDetectionBatchId())) {

			tmp.setFieldName("test_det_batch_id");
			tmp.setValue(specimen.getTestDetectionBatchId());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getDetectionBatchPosition())) {

			tmp.setFieldName("test_det_id");
			tmp.setValue(specimen.getDetectionBatchPosition());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestDetectionInstrument())) {

			tmp.setFieldName("test_det_instrument");
			tmp.setValue(specimen.getTestDetectionInstrument());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestDetectionPersonnel())) {

			tmp.setFieldName("test_det_personnel");
			tmp.setValue(specimen.getTestDetectionPersonnel());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getExtractionBatchPosition())) {

			tmp.setFieldName("test_ext_id");
			tmp.setValue(specimen.getExtractionBatchPosition());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestExtractionBatchsize())) {

			tmp.setFieldName("test_ext_batchsize");
			tmp.setValue(specimen.getTestExtractionBatchsize());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(specimen.getTestExtractionDatetime() != null) {

			tmp.setFieldName("test_ext_datetime");
			tmp.setValue(specimen.getTestExtractionDatetime().toString());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		 if(!StringUtils.isBlank(specimen.getTestExtractionBatchId())) {

			tmp.setFieldName("test_ext_batch_id");
			tmp.setValue(specimen.getTestExtractionBatchId());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestExtractionInstrument())) {

			tmp.setFieldName("test_ext_instrument");
			tmp.setValue(specimen.getTestExtractionInstrument());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestExtractionPersonnel())) {

			tmp.setFieldName("test_ext_personnel");
			tmp.setValue(specimen.getTestExtractionPersonnel());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestTporId())) {

			tmp.setFieldName("test_tpor_batch_id");
			tmp.setValue(specimen.getTestTporId());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestTporBatchsize())) {

			tmp.setFieldName("test_tpor_batchsize");
			tmp.setValue(specimen.getTestTporBatchsize());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(specimen.getTestTporDatetime() != null) {

			tmp.setFieldName("test_tpor_datetime");
			tmp.setValue(specimen.getTestTporDatetime().toString());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestTporId())) {

			tmp.setFieldName("test_tpor_id");
			tmp.setValue(specimen.getTestTporId());
						
			redcapDataService.saveRedcapData(tmp);
			
			tmp.setFieldName("tpor_batch_pos");
			tmp.setValue(specimen.getTestTporId());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestTporPersonnel())) {

			tmp.setFieldName("test_tpor_personnel");
			tmp.setValue(specimen.getTestTporPersonnel());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getResultsVerifiedBy())) {

			tmp.setFieldName("test_verify_personnel");
			tmp.setValue(specimen.getResultsVerifiedBy());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(specimen.getResultsVerifiedDate() != null) {

			tmp.setFieldName("test_verify_datetime");
			tmp.setValue(specimen.getResultsVerifiedDate().toString());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getResultsVerifiedBy())) {

			tmp.setFieldName("test_verify_personnel");
			tmp.setValue(specimen.getResultsVerifiedBy());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
		if(!StringUtils.isBlank(specimen.getTestVerifyBatchId())) {

			tmp.setFieldName("test_verify_batch_id");
			tmp.setValue(specimen.getTestVerifyBatchId());
						
			redcapDataService.saveRedcapData(tmp);
		}
		
	}

	/**
	 * Load organisation unit from DHSI2 using the id
	 * @param id
	 * @return
	 */
	public OrganisationUnit getOrganisationUnit(String id) {

		return restTemplate().getForObject(dhis2Url + "/organisationUnits/" + id, OrganisationUnit.class);
	}

	/**
	 * Get one specimen from the staging area
	 * @param barcode
	 * @return
	 */
	public SpecimenVO getOneSpecimen(String barcode) {

		return specimenService.findSpecimenByBarcode(barcode);
	}

	/**
	 * Convert the specimen into a JSON string
	 * @param specimen
	 * @return
	 */
	public String getSpecimenFieldList(SpecimenVO specimen) {

		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();

		if (specimen == null) {
			return "[]";
		}
				
		/// Patient demographics
		if(specimen.getPatient() != null ) {
			if (specimen.getPatient().getDateOfBirth() != null) {
				
				Calendar cal = Calendar.getInstance();
				cal.setTime(specimen.getPatient().getDateOfBirth());

				int day = cal.get(Calendar.DAY_OF_MONTH);
				int month = cal.get(Calendar.MONTH);
				int year = cal.get(Calendar.YEAR);
				
				String date = (day < 10 ? "0" + day : day) + "-" + (month < 10 ? "0" + month : month) + "-" + year;
				
				fields.add(new DDPObjectField("date_birth", date, null));
			}
	
			if(!StringUtils.isBlank(specimen.getPatient().getFirstName())) { 
				fields.add(new DDPObjectField("patient_first_name", specimen.getPatient().getFirstName(), null));
			}
			
			if(!StringUtils.isBlank(specimen.getPatient().getSex())) {
								
				fields.add(new DDPObjectField("sex", specimen.getPatient().getSex(), null));
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
				fields.add(new DDPObjectField("identity_no", specimen.getPatient().getIdentityNo(), null));
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

				int day = cal.get(Calendar.DAY_OF_MONTH);
				int month = cal.get(Calendar.MONTH);
				int year = cal.get(Calendar.YEAR);
				
				String date = (day < 10 ? "0" + day : day) + "-" + (month < 10 ? "0" + month : month) + "-" + year;
				
				fields.add(new DDPObjectField("patient_departure_date", date, null));
			}
			
		}
		/// Specimen details
		if(!StringUtils.isBlank(specimen.getSpecimenBarcode())) {
			fields.add(new DDPObjectField("specimen_barcode", specimen.getSpecimenBarcode(), null));
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
			
			String time = (hour < 10 ? "0" + hour : hour) + ":" + (mins < 10 ? "0" + mins : mins) ;
			
			fields.add(new DDPObjectField("time_dispatched", time, null));
		}
		
		if (!StringUtils.isBlank(specimen.getDispatcher())) {
			fields.add(new DDPObjectField("specimen_dispatcher", specimen.getDispatcher(), null));
		}

		if (!StringUtils.isBlank(specimen.getSampleStatusDispatch())) {
			fields.add(new DDPObjectField("specimen_status_dispatch", specimen.getSampleStatusDispatch(), null));
		}

		if (!StringUtils.isBlank(specimen.getDispatchLocation())) {
			fields.add(new DDPObjectField("location_of_dispatch", specimen.getDispatchLocation(), null));
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
			Calendar cal = Calendar.getInstance();
			cal.setTime(specimen.getReceivingDateTime());
			
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);

			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int mins = cal.get(Calendar.MINUTE);
			
			String datetime = (day < 10 ? "0" + day : day) + "-" + (month < 10 ? "0" + month : month) + "-" + year +  " " + (hour < 10 ? "0" + hour : hour) + ":" + (mins < 10 ? "0" + mins : mins) ;
			fields.add(new DDPObjectField("receiving_datetime", datetime, null));
		}

		if (specimen.getCollectionDateTime() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(specimen.getCollectionDateTime());
			
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);

			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int mins = cal.get(Calendar.MINUTE);

			String datetime = (day < 10 ? "0" + day : day) + "-" + (month < 10 ? "0" + month : month) + "-" + year +  " " + (hour < 10 ? "0" + hour : hour) + ":" + (mins < 10 ? "0" + mins : mins) ;
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
			fields.add(new DDPObjectField("results_entered_by", specimen.getResultsEnteredBy(), specimen.getCollectionDateTime()));
		}

		if (specimen.getResultsEnteredDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(specimen.getResultsEnteredDate());
			
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);

			String datetime = (day < 10 ? "0" + day : day) + "-" + (month < 10 ? "0" + month : month) + "-" + year;
			fields.add(new DDPObjectField("results_entered_date", datetime, specimen.getCollectionDateTime()));
		}

		if (!StringUtils.isBlank(specimen.getResultsVerifiedBy())) {
			fields.add(new DDPObjectField("results_verifies_by", specimen.getResultsVerifiedBy(), specimen.getCollectionDateTime()));
		}

		if (specimen.getResultsVerifiedDate() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(specimen.getResultsVerifiedDate());
			
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);

			String datetime = (day < 10 ? "0" + day : day) + "-" + (month < 10 ? "0" + month : month) + "-" + year;
			
			fields.add(new DDPObjectField("results_verified_date", datetime, specimen.getCollectionDateTime()));
		}

		if (!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
			fields.add(new DDPObjectField("results_authorised_by", specimen.getResultsAuthorisedBy(), specimen.getCollectionDateTime()));
		}

		if (specimen.getResultsAuthorisedDate() != null) {

			Calendar cal = Calendar.getInstance();
			cal.setTime(specimen.getResultsAuthorisedDate());
			
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH);
			int year = cal.get(Calendar.YEAR);

			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int mins = cal.get(Calendar.MINUTE);

			String datetime = (day < 10 ? "0" + day : day) + "-" + (month < 10 ? "0" + month : month) + "-" + year;
			
			fields.add(new DDPObjectField("results_authorised_date", datetime, specimen.getCollectionDateTime()));
		}

		if (!StringUtils.isBlank(specimen.getNotes())) {
			fields.add(new DDPObjectField("specimen_notes", specimen.getNotes(), specimen.getCollectionDateTime()));
		}

		logger.info(fields.toString());
		
		StringBuilder builder = new StringBuilder();
		for(DDPObjectField field : fields) {
			if(builder.length() > 0) {
				builder.append(",");
			}
			builder.append(field.toString());
		}
		
		return "[" + builder.toString() + "]";
	}
	
	/**
	 * Generate a payload JSON string for updating an event with the results
	 * of the specimen
	 * 
	 * @param event
	 * @param specimen
	 * @return
	 */
	private String getEventPayloadString(Event event, SpecimenVO specimen) {
		
		StringBuilder builder = new StringBuilder();

		builder.append("{\n");
		builder.append("\"program\": \"" + event.getProgram() + "\",\n");
		builder.append("\"orgUnit\": \""+ event.getOrgUnit() + "\",\n");
		builder.append("\"programStage\": \"" + event.getProgramStage()+ "\",\n");
		
		if(event.getCompletedDate() != null) {
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
		for(DataValue value : event.getDataValues()){
			builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(value.getDataElement()) + 
					", " + JSONObject.quote("value") + ": " + JSONObject.quote(value.getValue()) + "},\n");
		}
		
		// Fill in the results data values
		builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(env.getProperty("lab.results")) + 
				", " + JSONObject.quote("value") + ": " + JSONObject.quote(specimen.getResults()) + "},\n");
		
		builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(env.getProperty("lab.results.entered.by")) + 
				", " + JSONObject.quote("value") + ": " + JSONObject.quote(specimen.getResultsEnteredBy()) + "},\n");
		
		builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(env.getProperty("lab.results.date.entered")) + 
				", " + JSONObject.quote("value") + ": " + JSONObject.quote(specimen.getResultsEnteredDate().toString()) + "},\n");
		
		builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(env.getProperty("lab.results.verified.by")) + 
				", " + JSONObject.quote("value") + ": " + JSONObject.quote(specimen.getResultsVerifiedBy()) + "},\n");
		
		builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(env.getProperty("lab.result.date.verified")) + 
				", " + JSONObject.quote("value") + ": " + JSONObject.quote(specimen.getResultsVerifiedDate().toString()) + "},\n");
		
		builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(env.getProperty("lab.results.authorised.by")) + 
				", " + JSONObject.quote("value") + ": " + JSONObject.quote(specimen.getResultsAuthorisedBy()) + "},\n");
		
		builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(env.getProperty("lab.results.date.authorised")) + 
				", " + JSONObject.quote("value") + ": " + JSONObject.quote(specimen.getResultsAuthorisedDate().toString()) + "}\n");

		if(specimen.getReceivingDateTime() != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");						
			String datetime = format.format(specimen.getReceivingDateTime());
			builder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(env.getProperty("lab.specimen.date.received")) + 
					", " + JSONObject.quote("value") + ": " + JSONObject.quote(datetime) + "}\n");
		}
		
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
		builder.append("{\n");
		builder.append("\"events\" : [\n");
		
		for(SpecimenVO sp : specimen) {
			
			updateLabReport(sp);
			
			StringBuilder bd = new StringBuilder();
			bd.append(dhis2Url);
			bd.append("/events?programStage=" + programStage);
			bd.append("&program=" + program);
			bd.append("&event=" + sp.getEvent());
			logger.info("Final url is " + bd.toString());
			
			EventList eventList = restTemplate().getForObject(bd.toString(), EventList.class);
			int i = 0;
			
			for(Event event : eventList.getEvents()) {
			
				if(i == 1) {
					builder.append(", ");
				}
				
				String payload = getEventPayloadString(event, sp);
				
				builder.append(payload);
				
				if(i == 0) {
					i = 1;
				}
				
				// Add the result values to the event
				DataValue val = new DataValue();
				val.setDataElement(env.getProperty("lab.results"));
				val.setValue(sp.getResults());				
				event.getDataValues().add(val);
				
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.entered.by"));
				val.setValue(sp.getResultsEnteredBy());				
				event.getDataValues().add(val);
				
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.date.entered"));
				val.setValue(sp.getResultsEnteredDate().toString());				
				event.getDataValues().add(val);
				
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.verified.by"));
				val.setValue(sp.getResultsVerifiedBy());				
				event.getDataValues().add(val);
				
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.result.date.verified"));
				val.setValue(sp.getResultsVerifiedDate().toString());				
				event.getDataValues().add(val);
				
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.authorised.by"));
				val.setValue(sp.getResultsAuthorisedBy());				
				event.getDataValues().add(val);
				
				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.date.authorised"));
				val.setValue(sp.getResultsAuthorisedDate().toString());				
				event.getDataValues().add(val);
								
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
