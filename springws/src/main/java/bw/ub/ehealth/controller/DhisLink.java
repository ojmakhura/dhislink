package bw.ub.ehealth.controller;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotBlank;

import org.apache.commons.collections4.CollectionUtils;
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
import bw.ub.ehealth.dhislink.vo.ProgramStage;
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

	@Value("${dhis2.mohw.org.unit}")
	private String mohwOrgUnit;

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

    @Value("${sentinel.id}")
    private String sentinelId;

    @Value("${sentinel.program.stage.examination}")
    private String sentinelExaminationId;
    
    //@Value("${yVAVueRALgp.rQeSbwNwWRJ.lab.specimen.barcode}")
    //private String sentinelField;
    
    @Value("${dhis2.api.program}")
    private String covidProgram;

    @Value("${dhis2.api.program.stage}")
    private String covidProgramStage;

    @Value("lab.specimen.barcode}")
    private String barcodeField;
    
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
		String finalUrl = dhis2Url + "/events.json?" + builder.toString();
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
	 * 
	 * @param program
	 * @return
	 */
	public Program getProgram(String program) {

		return restTemplate().getForObject(dhis2Url + "/programs/" + program, Program.class);
	}

	/**
	 * Load a program from DHIS
	 * 
	 * @param program
	 * @return
	 */
	public ProgramStage getProgramStage(String programStage) {

		return restTemplate().getForObject(dhis2Url + "/programStages/" + programStage, ProgramStage.class);
	}

	/**
	 * Load an tracked entity instance from DHIS
	 * 
	 * @param orgUnit
	 * @param instanceId
	 * @return
	 */
	public TrackedEntityInstance getTrackedEntityInstance(@NotBlank String orgUnit, @NotBlank String instanceId) {

		String append = "/trackedEntityInstances.json?ou=" + orgUnit + "&trackedEntityInstance=" + instanceId;
		TrackedEntityInstanceList list = restTemplate().getForObject(dhis2Url + append,
				TrackedEntityInstanceList.class);
		TrackedEntityInstance instance = null;
		if (list != null && list.getTrackedEntityInstances() != null && list.getTrackedEntityInstances().size() > 0) {
			instance = ((List<TrackedEntityInstance>) list.getTrackedEntityInstances()).get(0);
		}

		return instance;
	}

	private List<TrackedEntityInstance> getTrackedEntityInstanceList(@NotBlank String instanceIds) {
		String append = "/trackedEntityInstances.json?ou=" + mohwOrgUnit + "&trackedEntityInstance=" + instanceIds;
		TrackedEntityInstanceList list = restTemplate().getForObject(dhis2Url + append,
				TrackedEntityInstanceList.class);

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
		
		if (attrMap.get(env.getProperty("patient.identification")) != null) {
			patientVO.setIdentityNo(attrMap.get(env.getProperty("patient.identification")).getValue());
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

			if (sex.equalsIgnoreCase("male")) {
				patientVO.setSex(Sex.MALE.getValue());
			} else if (sex.equalsIgnoreCase("female")) {
				patientVO.setSex(Sex.FEMALE.getValue());
			} else {

				patientVO.setSex(Sex.UNKNOWN.getValue());
			}
		}

		// Get patient nationality
		if (attrMap.get(env.getProperty("patient.nationality")) != null) {
			patientVO.setNationality(attrMap.get(env.getProperty("patient.nationality")).getValue());
		}
		
		if (attrMap.get(env.getProperty("patient.identification")) != null) {
			patientVO.setNationality(attrMap.get(env.getProperty("patient.identification")).getValue());
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

		if (StringUtils.isBlank(patientVO.getFirstName()) && !StringUtils.isBlank(patientVO.getSurname())) {
			patientVO.setFirstName(patientVO.getSurname());
		}

		if (!StringUtils.isBlank(patientVO.getFirstName()) && StringUtils.isBlank(patientVO.getSurname())) {
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

	private String getSpecimenType(Map<String, DataValue> values, String pre) {

		String specimenType = "";

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.type.nasal"))
				&& values.get(env.getProperty(pre + "lab.specimen.type.nasal")) != null) {
			specimenType = SpecimenType.NASAL.getValue();

		} else if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.type.npa"))
				&& values.get(env.getProperty(pre + "lab.specimen.type.npa")) != null) {
			specimenType = SpecimenType.NPA.getValue();

		} else if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.type.op"))
				&& values.get(env.getProperty(pre + "lab.specimen.type.op")) != null) {
			specimenType = SpecimenType.OP.getValue();

		} else if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.type.bal"))
				&& values.get(env.getProperty(pre + "lab.specimen.type.bal")) != null) {
			specimenType = SpecimenType.BAL.getValue();

		} else if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.type.combined.npop"))
				&& values.get(env.getProperty(pre + "lab.specimen.type.combined.npop")) != null) {
			specimenType = SpecimenType.NP_OP.getValue();

		} else if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.type.np"))
				&& values.get(env.getProperty(pre + "lab.specimen.type.np")) != null) {
			specimenType = SpecimenType.NP.getValue();

		} else if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.type.sputum"))
				&& values.get(env.getProperty(pre + "lab.specimen.type.sputum")) != null) {
			specimenType = SpecimenType.SPUTUM.getValue();

		} else if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.type.other"))
				&& values.get(env.getProperty(pre + "lab.specimen.type.other")) != null) {
			specimenType = values.get(env.getProperty(pre + "lab.specimen.type.other")).getValue();

		}

		return specimenType;
	}

	private String getSymptoms(Map<String, DataValue> values, String pre) {

		StringBuilder builder = new StringBuilder();

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.apnoea"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.apnoea")) != null) {
				builder.append(Symptoms.APNOEA);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.cough"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.cough")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.COUGH);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.diarrhoea"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.diarrhoea")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.DIARRHOEA);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.fever"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.fever")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.FEVER);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.none"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.none")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.NONE);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.paroxysmal"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.paroxysmal")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.PAROXYSMAL_WHOOP);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.breath.shortness"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.breath.shortness")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.SHORT_BREATH);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.sore.throat"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.sore.throat")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.SORE_THROAT);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.stiff.neck"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.stiff.neck")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.STIFF_NECK);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.unknown"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.unknown")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.UNKNOWN);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.vomiting"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.vomiting")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(Symptoms.VOMITTING);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.symptoms.other"))) {
			if (values.get(env.getProperty(pre + "lab.symptoms.other")) != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(values.get(env.getProperty(pre + "lab.symptoms.other")).getValue());

			}
		}

		return builder.toString();
	}

	/**
	 * Get the underlying risk factors
	 * 
	 * @param values
	 * @return
	 */
	private String getRiskFactors(Map<String, DataValue> values, String pre) {

		StringBuilder builder = new StringBuilder();

		DataValue value = null;
		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.asthma"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.asthma"));
			if (value != null) {
				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.chronic.lung"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.chronic.lung"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.diabetes"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.diabetes"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.hiv"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.hiv"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.none"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.none"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.stiff.neck"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.stiff.neck"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.tb"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.tb"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.unknown"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.unknown"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.pregnancy"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.pregnancy"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.underlying.other"))) {
			value = values.get(env.getProperty(pre + "lab.underlying.other"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		return builder.toString();
	}

	/**
	 * Find the type of test to be conducted
	 * 
	 * @param values
	 * @return
	 */
	private String getTestType(Map<String, DataValue> values, String pre) {

		StringBuilder builder = new StringBuilder();
		DataValue value = null;
		if (!StringUtils.isBlank(env.getProperty(pre + "lab.test.avian"))) {
			value = values.get(env.getProperty(pre + "lab.test.avian"));
			if (value != null) {
				builder.append(TestType.AVIAN);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.test.influenza.RSV"))) {
			value = values.get(env.getProperty(pre + "lab.test.influenza.RSV"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(TestType.FLU_RSV);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.test.mers"))) {
			value = values.get(env.getProperty(pre + "lab.test.mers"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(TestType.MERS_COV);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.test.neonatal"))) {
			value = values.get(env.getProperty(pre + "lab.test.neonatal"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(TestType.NEONATAL);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.test.cov2"))) {
			value = values.get(env.getProperty(pre + "lab.test.cov2"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(TestType.SARS_COV2);

			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.test.other"))) {
			value = values.get(env.getProperty(pre + "lab.test.other"));
			if (value != null) {

				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(value.getValue());

			}
		}

		return builder.toString();
	}

	/**
	 * Get submitter
	 * 
	 * @param values
	 * @return
	 */
	private String getSubmitter(Map<String, DataValue> values, String pre) {

		String submitter = "";

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.submitter.surname"))
				&& values.get(env.getProperty(pre + "lab.submitter.surname")) != null) {
			submitter = values.get(env.getProperty(pre + "lab.submitter.surname")).getValue();
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.submitter.firstname"))
				&& values.get(env.getProperty(pre + "lab.submitter.firstname")) != null) {

			if (!StringUtils.isBlank(submitter)) {
				submitter = submitter + " " + values.get(env.getProperty(pre + "lab.submitter.firstname")).getValue();
			} else {

				submitter = values.get(env.getProperty(pre + "lab.submitter.firstname")).getValue();
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
		 * If the results have already been produced, no need to pull the data or the
		 * barcode does not exist, or if the specimen has
		 */
		DataValue labResults = values
				.get(env.getProperty("lab.results").trim());
		boolean resultsCheckOk = false;

		if (!skipResulted) {
			// logger.debug("Resulted specimen not skipped");
			resultsCheckOk = true;
		} else {
			if (labResults == null || StringUtils.isBlank(labResults.getValue())
					|| labResults.getValue().equals("PENDING") || labResults.getValue().equals("ORDERED")) {
				resultsCheckOk = true;
			} else {
				resultsCheckOk = false;
			}
		}

		String pre = event.getProgram() + "." + event.getProgramStage() + ".";

		if (!resultsCheckOk || values.get(env.getProperty("lab.specimen.barcode").trim()) == null) {
			//logger.debug("Results check failed");
			//logger.debug(event.toString());
			return null;
		}
		
		SpecimenVO specimen = new SpecimenVO();
		specimen.setEvent(event.getEvent());
		specimen.setCreated(event.getCreated());
		specimen.setLastUpdated(event.getLastUpdated());
		specimen.setProgramId(event.getProgram());
		specimen.setProgramStageId(event.getProgramStage());

		DataValue val = null;
		
		if(event.getProgram().equals(covidProgram)) {
			if (!StringUtils.isBlank(env.getProperty(pre + "lab.submitter.facility"))) {
				val = values.get(env.getProperty(pre + "lab.submitter.facility").trim());
				if (val != null) {
					specimen.setDispatchLocation(val.getValue());
				}
			}
		} else {
			specimen.setDispatchLocation(event.getOrgUnitName());
		}

		if(event.getProgram().equals(covidProgram)) {
			if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.facility"))) {
				val = values.get(env.getProperty(pre + "lab.specimen.facility").trim());
				if (val != null) {
					specimen.setPatientFacility(val.getValue());
				}
			}
		} else {
			specimen.setPatientFacility(event.getOrgUnitName());
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.specimen.facility"))) {
			val = values.get(env.getProperty(pre + "lab.specimen.facility").trim());
			if (val != null) {

				specimen.setDispatcherCity(val.getValue());
			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.submitter.email"))) {
			val = values.get(env.getProperty(pre + "lab.submitter.email").trim());
			if (val != null) {

				specimen.setDispatcherEmail(val.getValue());
			}
		}

		if (!StringUtils.isBlank(env.getProperty(pre + "lab.submitter.contact"))) {
			val = values.get(env.getProperty(pre + "lab.submitter.contact").trim());
			if (val != null) {

				specimen.setDispatcherContact(val.getValue());
			}
		}

		// Get the barcode
		if (!StringUtils.isBlank(env.getProperty("lab.specimen.barcode"))) {
			val = values.get(env.getProperty("lab.specimen.barcode").trim());
			if (val != null) {
				String barcode = val.getValue();
				specimen.setSpecimenBarcode(barcode.toUpperCase()); // Ensure standard barcodes
			}
		}

		// Get the covid number
		if (!StringUtils.isBlank(env.getProperty(pre + "lab.covid.number"))) {
			val = values.get(env.getProperty(pre + "lab.covid.number").trim());
			if (val != null) {
				String covidNUmber = val.getValue();

				specimen.setCovidNumber(covidNUmber);

				if (StringUtils.isBlank(specimen.getSpecimenBarcode())) {
					specimen.setSpecimenBarcode(covidNUmber.replaceAll("[^a-zA-Z0-9]", ""));
				}
			}
		}

		specimen.setDispatcher(getSubmitter(values, pre));

		try {
			if (!StringUtils.isBlank(env.getProperty("lab.specimen.date.collection"))) {
				val = values.get(env.getProperty("lab.specimen.date.collection").trim());
				if (val != null) {

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
					String date = val == null ? "" : val.getValue();
					
					date.replace('T', ' ');

					String time = ""; 
					
					if(date.length() == 10) {
						if (!StringUtils.isBlank(env.getProperty("lab.specimen.collection.time"))) {
							if(values.get(env.getProperty("lab.specimen.collection.time").trim()) != null) {
								time = values.get(env.getProperty("lab.specimen.collection.time").trim()).getValue();
							}
						}
						
						if(StringUtils.isBlank(time)) {
							time = "00:01";
						}
					}
										
					if (date.trim().length() > 0) {
						date = date + " " + time;
						Date dt = format.parse(date.trim());
						Calendar cal = Calendar.getInstance();
						cal.setTime(dt);
						specimen.setCollectionDateTime(cal.getTime());
					}
				}
			}
			
			if (!StringUtils.isBlank(env.getProperty("lab.specimen.date.received"))) {
				val = values.get(env.getProperty("lab.specimen.date.received").trim());
				if (val != null) {

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
					String date = val == null ? "" : val.getValue();
					date = date.replace('T', ' ');

					String time = ""; 

					if(date.length() == 10) {
						if (!StringUtils.isBlank(env.getProperty("lab.specimen.time.received"))) {
							if(values.get(env.getProperty("lab.specimen.time.received").trim()) != null) {
								time = values.get(env.getProperty("lab.specimen.time.received").trim()).getValue();
							}
						} 
						
						if(StringUtils.isBlank(time)) {
							time = "00:01";
						}
					}
										
					if (date.trim().length() > 0) {
						date = date + " " + time;
						Date dt = format.parse(date.trim());
						Calendar cal = Calendar.getInstance();
						cal.setTime(dt);
						specimen.setReceivingDateTime(cal.getTime());
					}
				}
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if(StringUtils.isBlank(specimen.getProgramId()) || specimen.getProgramId().equals(covidProgram)) {
			specimen.setSpecimenType(getSpecimenType(values, pre));
		} else {
			if (!StringUtils.isBlank(env.getProperty("yVAVueRALgp.rQeSbwNwWRJ.lab.specimen.type"))) {
				val = values.get(env.getProperty("yVAVueRALgp.rQeSbwNwWRJ.lab.specimen.type").trim());
				if(val != null) {
					//logger.info(event.toString());
					specimen.setSpecimenType(val.getValue());
				}
			}
		}
		
		if(!StringUtils.isBlank(env.getProperty("yVAVueRALgp.rQeSbwNwWRJ.patient.receive.sms"))) {
			val = values.get(env.getProperty("yVAVueRALgp.rQeSbwNwWRJ.patient.receive.sms").trim());
			if(val != null) {
				specimen.setReceiveSMS(Boolean.valueOf(val.getValue()));
			} else {
				specimen.setReceiveSMS(false);
			}
		} else {
			specimen.setReceiveSMS(false);
		}
		
		if(!StringUtils.isBlank(env.getProperty("yVAVueRALgp.rQeSbwNwWRJ.lab.specimen.submitter"))) {
			val = values.get(env.getProperty("yVAVueRALgp.rQeSbwNwWRJ.lab.specimen.submitter").trim());
			if(val != null) {
				specimen.setDispatcher(val.getValue());
			}
		}

		specimen.setTestType(this.getTestType(values, pre));
		// specimen.setRiskFacors(this.getRiskFactors(values));

		return specimen;
	}

	/**
	 * Load data elements from DHIS2
	 * 
	 * @param elementId
	 * @return
	 */
	public DataElement getDataElement(String elementId) {

		return restTemplate().getForObject(dhis2Url + "/dataElements/" + elementId + ".json", DataElement.class);
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
		for (int i = 0; i < tmp.size(); i = i + 100) {
			StringBuilder builder = new StringBuilder();
			for (int j = 0; (j < 100 && (i + j) < tmp.size()); j++) {
				if (builder.length() > 0) {
					builder.append(";");
				}

				builder.append(tmp.get(i + j));
			}
			List<TrackedEntityInstance> teiList = getTrackedEntityInstanceList(builder.toString());

			for (TrackedEntityInstance tei : teiList) {
				map.put(tei.getTrackedEntityInstance(), trackedEntityInstanceToPatientVO(tei));
			}
		}

		return map;
	}

	/**
	 * Load specimen from DHIS2
	 * 
	 * Be careful not to query the dhis too many times. This happened when we were
	 * getting the patient information for each specimen. To avoid it, we make a
	 * call to get multiple tracked entity instances at the same time.
	 * 
	 * @param parameters
	 * @return
	 */
	public List<SpecimenVO> getSpecimen(String programId, String stageId) {
		final int pageSize = 2000;
		Map<String, String> parameters = new HashMap<>();
		parameters.put("program", programId);
		parameters.put("programStage", stageId);
		parameters.put("status", "COMPLETED");
		// params.put("filter", resultsField + ":EQ:PENDING");
		SpecimenVO last = specimenService.findLatestSpecimen(programId);
		String date = "2020-05-20";

		if (last != null) {

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH)
					.withZone(ZoneId.systemDefault());

			Calendar cal = Calendar.getInstance();
			cal.setTime(last.getLastUpdated());

			Instant updated = last.getLastUpdated().toInstant();
			updated = updated.minus(4, ChronoUnit.HOURS); /// Just backtrack in case we missed some. Also a hack to
															/// counter
			date = formatter.format(updated).replace(' ', 'T');
		}

		parameters.put("lastUpdatedStartDate", date);
		parameters.put("order", "lastUpdated:asc");
		parameters.put("pageSize", "" + pageSize);

		BigInteger numPulled = new BigInteger("0");
		int page = 1;
		parameters.put("page", "" + page);
		List<SpecimenVO> specimen = new ArrayList<>();
		List<Event> events = this.getEvents(parameters);
		List<SpecimenVO> tmp = getSpecimen(events, true);

		while (events.size() != 0) {
			specimen.addAll(tmp);
			
			logger.info("Total is " + specimen.size());

			numPulled.add(new BigInteger(tmp.size() + ""));
			page++;
			parameters.put("page", "" + page);
			events = this.getEvents(parameters);
			tmp = getSpecimen(events, true);
		}

		return specimen;
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

		for (SpecimenVO sp : tmp) {
			if (specimenService.findSpecimenByBarcode(sp.getSpecimenBarcode()) == null) {
				if(StringUtils.isBlank(sp.getProgramId()) || sp.getProgramId().equals(covidProgram)) {
					if (!StringUtils.isBlank(sp.getDispatchLocation())) {
						orgIds.add(sp.getDispatchLocation());
					}
	
					if (!StringUtils.isBlank(sp.getPatientFacility())) {
						orgIds.add(sp.getPatientFacility());
					}
				}
			}
		}

		for (OrganisationUnit unit : getOrganisationUnits(orgIds)) {
			orgUnits.put(unit.getId(), unit);
		}

		for (SpecimenVO sp : tmp) {
			String barcode = sp.getSpecimenBarcode();
			String tei = teis.get(barcode);
			PatientVO patientVO = pmap.get(tei);

			if (patientVO != null && patientVO.getId() == null) {
				if (StringUtils.isBlank(patientVO.getIdentityNo())) {
					patientVO.setIdentityNo(barcode);
				}

				/// We cannot do anything if both the patient and specimen have no
				/// identifying numbers
				if (StringUtils.isBlank(patientVO.getIdentityNo())) {
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
				if (st == null || StringUtils.isBlank(st.getEvent())) {

					if (st != null && st.getId() != null) {
						sp.setId(st.getId());

					}

					// Only get the facilities if this is a new specimen
					OrganisationUnit unit = null;
					if (!StringUtils.isBlank(sp.getDispatchLocation())) {
						unit = orgUnits.get(sp.getDispatchLocation());
						sp.setDispatchLocation(unit != null ? unit.getName() : null);
					}

					if (!StringUtils.isBlank(sp.getPatientFacility())) {

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

					if (data == null) {
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
	 * 
	 * @param id
	 * @return
	 */
	public List<OrganisationUnit> getOrganisationUnits(Set<String> ids) {
		if (ids.size() == 0) {
			return new ArrayList<>();
		}
		StringBuilder builder = new StringBuilder();
		for (String id : ids) {
			if (builder.length() > 0) {
				builder.append(",");
			}

			builder.append(id);
		}
		OrganisationUnitList orgList = restTemplate().getForObject(
				dhis2Url + "/organisationUnits.json?filter=id:in:[" + builder.toString() + "]", OrganisationUnitList.class);
		return (List<OrganisationUnit>) orgList.getOrganisationUnits();
	}

	/**
	 * Get one specimen from the staging area
	 * 
	 * @param barcode
	 * @return
	 */
	public SpecimenVO getOneSpecimen(String barcode) {

		return specimenService.findSpecimenByBarcode(barcode);
	}

	public String getSpecimenFieldsString(SpecimenVO specimen) {

		StringBuilder builder = new StringBuilder();
		for (DDPObjectField field : getSpecimenFieldList(specimen)) {
			if (builder.length() > 0) {
				builder.append(",");
			}
			builder.append(field.toString());
		}
		String str = "[" + builder.toString() + "]";

		return str;
	}

	/**
	 * Convert the specimen into a JSON string
	 * 
	 * @param specimen
	 * @return
	 */
	public List<DDPObjectField> getSpecimenFieldList(SpecimenVO specimen) {

		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();

		if (specimen == null) {
			return fields;
		}

		/// Patient demographics
		if (specimen.getPatient() != null) {
			if (specimen.getPatient().getDateOfBirth() != null) {

				Instant dob = specimen.getPatient().getDateOfBirth().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
						.withZone(ZoneId.systemDefault());
				fields.add(new DDPObjectField("date_birth", formatter.format(dob), null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getFirstName())) {
				fields.add(new DDPObjectField("patient_first_name", specimen.getPatient().getFirstName(), null));
			}

			if (specimen.getPatient().getSex() != null) {
				if (specimen.getPatient().getSex().equals("MALE")) {
					fields.add(new DDPObjectField("sex", "1", null));
				} else if (specimen.getPatient().getSex().equals("FEMALE")) {
					fields.add(new DDPObjectField("sex", "2", null));
				} else {
					fields.add(new DDPObjectField("sex", "3", null));
				}
			}

			if (!StringUtils.isBlank(specimen.getPatient().getSurname())) {
				fields.add(new DDPObjectField("patient_surname", specimen.getPatient().getSurname(), null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getContactNumber())) {
				fields.add(new DDPObjectField("patient_contact", specimen.getPatient().getContactNumber(), null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getNationality())) {
				fields.add(new DDPObjectField("patient_nationality", specimen.getPatient().getNationality(), null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getIdentityNo())) {
				fields.add(new DDPObjectField("national_id", specimen.getPatient().getIdentityNo(), null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getCity())) {
				fields.add(new DDPObjectField("patient_city", specimen.getPatient().getCity(), null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getCountryDeparture())) {
				fields.add(new DDPObjectField("patient_departure_country", specimen.getPatient().getCountryDeparture(),
						null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getNextOfKin())) {
				fields.add(new DDPObjectField("patient_kin", specimen.getPatient().getNextOfKin(), null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getKinContact())) {
				fields.add(new DDPObjectField("patient_kin_contact", specimen.getPatient().getKinContact(), null));
			}

			if (!StringUtils.isBlank(specimen.getPatient().getTransportRegistration())) {
				fields.add(new DDPObjectField("patient_transport_registration",
						specimen.getPatient().getTransportRegistration(), null));
			}

			if (specimen.getPatient().getDepartureDate() != null) {

				Calendar cal = Calendar.getInstance();
				cal.setTime(specimen.getPatient().getDepartureDate());

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
						.withZone(ZoneId.systemDefault());
				Instant depDate = specimen.getPatient().getDepartureDate().toInstant();
				String date = formatter.format(depDate);

				fields.add(new DDPObjectField("patient_departure_date", date, null));
			}

		}
		/// Specimen details
		if (!StringUtils.isBlank(specimen.getSpecimenBarcode())) {
			fields.add(new DDPObjectField("specimen_barcode", specimen.getSpecimenBarcode(), null));
		}

		if (!StringUtils.isBlank(specimen.getCovidNumber())) {
			fields.add(new DDPObjectField("ipms_lab_covid_number", specimen.getCovidNumber(), null));
		}

		if (!StringUtils.isBlank(specimen.getBatchNumber())) {
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

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
					.withZone(ZoneId.systemDefault());
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
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH)
					.withZone(ZoneId.systemDefault());
			String datetime = formatter.format(receivingDate);
			fields.add(new DDPObjectField("receiving_datetime", datetime, null));
		}

		if (specimen.getCollectionDateTime() != null) {

			Instant collectionDate = specimen.getCollectionDateTime().toInstant();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH)
					.withZone(ZoneId.systemDefault());
			String datetime = formatter.format(collectionDate);
			fields.add(new DDPObjectField("date_specimen_collected", datetime, null));
		}

		if (!StringUtils.isBlank(specimen.getReceivingConditionCode())) {
			fields.add(new DDPObjectField("receiving_condition_code", specimen.getReceivingConditionCode(), null));
		}

		if (!StringUtils.isBlank(specimen.getResults())) {

			String results = "";

			if (specimen.getResults().equals("1")) {
				results = "POSITIVE";
			} else if (specimen.getResults().equals("2")) {
				results = "NEGATIVE";
			} else if (specimen.getResults().equals("3")) {
				results = "INCONCLUSIVE";
			} else if (specimen.getResults().equals("4")) {
				//if(StringUtils.isBlank(specimen.getProgramId()) || specimen.getProgramId().equals(covidProgram)) {
				results = "PENDING";
				//} else {
					
				//}
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
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH)
					.withZone(ZoneId.systemDefault());
			String datetime = formatter.format(verifiedInstant);
			fields.add(new DDPObjectField("results_entered_date", datetime, null));
		}

		if (!StringUtils.isBlank(specimen.getResultsVerifiedBy())) {
			fields.add(new DDPObjectField("results_verifies_by", specimen.getResultsVerifiedBy(), null));
		}

		if (specimen.getResultsVerifiedDate() != null) {

			Instant collectionDate = specimen.getResultsVerifiedDate().toInstant();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH)
					.withZone(ZoneId.systemDefault());
			String datetime = formatter.format(collectionDate);

			fields.add(new DDPObjectField("results_verified_date", datetime, null));
		}

		if (!StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
			fields.add(new DDPObjectField("authorizer_personnel", specimen.getResultsAuthorisedBy(), null));
		}

		if (specimen.getResultsAuthorisedDate() != null) {

			Instant authDate = specimen.getResultsAuthorisedDate().toInstant();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH)
					.withZone(ZoneId.systemDefault());
			String datetime = formatter.format(authDate);
			fields.add(new DDPObjectField("authorizer_datetime", datetime, null));
		}

		if (!StringUtils.isBlank(specimen.getResultsAuthorisedBy()) && specimen.getResultsAuthorisedDate() != null) {
			fields.add(new DDPObjectField("result_authorised", "1", null));
		} else {
			fields.add(new DDPObjectField("result_authorised", "0", null));
		}

		if (!StringUtils.isBlank(specimen.getNotes())) {
			fields.add(new DDPObjectField("specimen_notes", specimen.getNotes(), null));
		}

		// Reception TPOR form
		List<DDPObjectField> newf = getTporFormFields(specimen.getSpecimenBarcode());
		fields.addAll(getTporFormFields(specimen.getSpecimenBarcode()));

		// Extraction form
		newf = getExtractionFormFields(specimen.getSpecimenBarcode());
		fields.addAll(newf);

		// Resulting form
		newf = getResultingFormFields(specimen.getSpecimenBarcode());
		fields.addAll(newf);

		return fields;
	}

	public List<DDPObjectField> getResultingFormFields(String barcode) {

		Map<String, RedcapDataVO> searchMap = null;
		List<DDPObjectField> fields = new ArrayList<DDPObjectField>();
		DDPObjectField field;
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		// resulting project
		criteria.setProjectId(labResultingPID);
		criteria.setValue(barcode);

		List<RedcapDataVO> redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);

		if (redcapDataVOs != null && redcapDataVOs.size() > 0) {

			RedcapDataVO res = null;
			if (redcapDataVOs.size() == 1) {
				res = redcapDataVOs.get(0);
			} else {
				// TODO: what to do if the specimen was processed multiple times
				res = redcapDataVOs.get(0);
			}

			int pindex = res.getFieldName().lastIndexOf("_");
			String pos = res.getFieldName().substring(pindex + 1);

			fields.add(new DDPObjectField("test_det_batch_id", res.getRecord(), null));
			fields.add(new DDPObjectField("test_det_barcode", res.getValue(), null));
			fields.add(new DDPObjectField("det_batch_pos", pos, null));

			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labResultingPID);
			criteria.setRecord(res.getRecord());
			criteria.setEventId(res.getEventId());
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			searchMap = getRedcapDataMap(redcapDataVOs);

			if (searchMap.containsKey("test_assay_personnel")) {
				res = searchMap.get("test_assay_personnel");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_assay_personnel")) {
				res = searchMap.get("test_assay_personnel");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_assay_result_" + pos)) {
				res = searchMap.get("test_assay_result_" + pos);
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				field.setField("test_assay_result");
				fields.add(field);
			}

			if (searchMap.containsKey("test_assay_batch_id")) {
				res = searchMap.get("test_assay_batch_id");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_assay_result_why")) {
				res = searchMap.get("test_assay_result_why");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("resulting_complete")) {
				res = searchMap.get("resulting_complete");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_det_personnel")) {
				res = searchMap.get("test_det_personnel");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_det_datetime")) {
				res = searchMap.get("test_det_datetime");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_det_batchsize")) {
				res = searchMap.get("test_det_batchsize");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("detection_lab")) {
				res = searchMap.get("detection_lab");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_det_instrument")) {
				res = searchMap.get("test_det_instrument");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_verify_personnel")) {
				res = searchMap.get("test_verify_personnel");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_verify_datetime")) {
				res = searchMap.get("test_verify_datetime");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_verify_batchsize")) {
				res = searchMap.get("test_verify_batchsize");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("covid_rna_results" + pos)) {
				res = searchMap.get("covid_rna_results" + pos);
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				field.setField("covid_rna_results");
				fields.add(field);
			}

			if (searchMap.containsKey("test_verify_result_" + pos)) {
				res = searchMap.get("test_verify_result_" + pos);
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
				field.setField("test_verify_result");
				fields.add(field);
			}

			if (searchMap.containsKey("verification_complete")) {
				res = searchMap.get("verification_complete");
				field = new DDPObjectField(res.getFieldName(), res.getValue(), null);
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
		// field = getCriteriaField(criteria);
		if (redcapDataVOs != null && redcapDataVOs.size() > 0) {

			RedcapDataVO ext = null;
			if (redcapDataVOs.size() == 1) {
				ext = redcapDataVOs.get(0);
			} else {
				// TODO: what to do if the specimen was processed multiple times
				ext = redcapDataVOs.get(0);
			}

			int pindex = ext.getFieldName().lastIndexOf("_");
			String pos = ext.getFieldName().substring(pindex + 1);

			fields.add(new DDPObjectField("ext_batch_pos", pos, null));
			fields.add(new DDPObjectField("test_ext_barcode", ext.getValue(), null));
			fields.add(new DDPObjectField("test_ext_batch_id", ext.getRecord(), null));

			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labExtractionPID);
			criteria.setRecord(ext.getRecord());
			criteria.setEventId(ext.getEventId());
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			Map<String, RedcapDataVO> searchMap = getRedcapDataMap(redcapDataVOs);

			if (searchMap.containsKey("test_ext_datetime")) {
				ext = searchMap.get("test_ext_datetime");
				field = new DDPObjectField(ext.getFieldName(), ext.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_ext_personnel")) {
				ext = searchMap.get("test_ext_personnel");
				field = new DDPObjectField(ext.getFieldName(), ext.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_ext_batchsize")) {
				ext = searchMap.get("test_ext_batchsize");
				field = new DDPObjectField(ext.getFieldName(), ext.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("extraction_lab")) {
				ext = searchMap.get("extraction_lab");
				field = new DDPObjectField(ext.getFieldName(), ext.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_ext_instrument")) {
				ext = searchMap.get("test_ext_instrument");
				field = new DDPObjectField(ext.getFieldName(), ext.getValue(), null);
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
		// field = getCriteriaField(criteria);
		if (redcapDataVOs != null && redcapDataVOs.size() > 0) {

			RedcapDataVO rec = null;
			if (redcapDataVOs.size() == 1) {
				rec = redcapDataVOs.get(0);
			} else {
				// TODO: what to do if the specimen was processed multiple times
				rec = redcapDataVOs.get(0);
			}

			// RedcapDataVO rd = redcapDataVOs.get(0);
			int pindex = rec.getFieldName().lastIndexOf("_");
			String pos = rec.getFieldName().substring(pindex + 1);
			fields.add(new DDPObjectField("tpor_batch_pos", pos, null));
			fields.add(new DDPObjectField("test_tpor_batch_id", rec.getRecord(), null));
			fields.add(new DDPObjectField("test_tpor_barcode", rec.getValue(), null));

			fields.add(new DDPObjectField("lab_rec_id", rec.getRecord(), null));

			criteria = new RedcapDataSearchCriteria();
			criteria.setProjectId(labReceptionPID);
			criteria.setRecord(rec.getRecord());
			criteria.setEventId(rec.getEventId());
			redcapDataVOs = (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
			Map<String, RedcapDataVO> searchMap = getRedcapDataMap(redcapDataVOs);

			// field = getCriteriaField(criteria);
			if (searchMap.containsKey("test_tpor_datetime")) {
				rec = searchMap.get("test_tpor_datetime");
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_tpor_personnel")) {
				rec = searchMap.get("test_tpor_personnel");
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("test_tpor_batchsize")) {
				rec = searchMap.get("test_tpor_batchsize");
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("tpor_lab")) {
				rec = searchMap.get("tpor_lab");
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("received_datetime")) {
				rec = searchMap.get("received_datetime");
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("receiving_personnel")) {
				rec = searchMap.get("receiving_personnel");
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("receiving_lab")) {
				rec = searchMap.get("receiving_lab");
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("lab_rec_batchsize")) {
				rec = searchMap.get("lab_rec_batchsize");
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				fields.add(field);
			}

			if (searchMap.containsKey("specimen_cond_" + pos)) {
				rec = searchMap.get("specimen_cond_" + pos);
				field = new DDPObjectField(rec.getFieldName(), rec.getValue(), null);
				field.setField("receiving_condition_code");
				fields.add(field);
			}
		}
		return fields;

	}

	private Map<String, RedcapDataVO> getRedcapDataMap(List<RedcapDataVO> data) {

		HashMap<String, RedcapDataVO> map = new HashMap<String, RedcapDataVO>();

		for (RedcapDataVO d : data) {
			map.put(d.getFieldName(), d);
		}

		return map;

	}

	/**
	 * Generate a payload JSON string for updating an event with the results of the
	 * specimen
	 * 
	 * @param event
	 * @param specimen
	 * @return
	 */
	private String getEventPayloadString(Event event) {

		StringBuilder builder = new StringBuilder();

		builder.append("{\n");
		builder.append("\"program\": \"" + event.getProgram() + "\",\n");
		builder.append("\"orgUnit\": \"" + event.getOrgUnit() + "\",\n");
		builder.append("\"programStage\": \"" + event.getProgramStage() + "\",\n");

		if (event.getCompletedDate() == null) {
			builder.append("\"completedDate\": \"null\",\n");
		} else {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			String datetime = format.format(event.getCompletedDate());

			builder.append("\"completedDate\": \"" + datetime + "\",\n");
		}

		builder.append("\"completedBy\": \"" + event.getCompleteBy() + "\",\n");
		if (event.getEventDate() == null) {
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
		for (DataValue value : event.getDataValues()) {

			if (evbuilder.length() > 0) {
				evbuilder.append(", ");
			}

			evbuilder.append("\t{" + JSONObject.quote("dataElement") + ": " + JSONObject.quote(value.getDataElement())
					+ ", " + JSONObject.quote("value") + ": " + JSONObject.quote(value.getValue()) + "}\n");
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

		if (CollectionUtils.isEmpty(specimen)) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		Map<String, SpecimenVO> spMap = new HashMap<String, SpecimenVO>();

		String barcodeField = null;
		String programStage = null, program = null;
		String pre = "";

		// To pull all events at once we have to put the events together
		for (SpecimenVO sp : specimen) {
			if (builder.length() > 0) {
				builder.append(";");
			}
			builder.append(sp.getSpecimenBarcode());
			spMap.put(sp.getSpecimenBarcode(), sp);

			if (!StringUtils.isEmpty(sp.getProgramId())) {
				program = sp.getProgramId();
				programStage = sp.getProgramStageId();
				pre = sp.getProgramId() + "." + sp.getProgramStageId() + ".";
			}
		}

		if (StringUtils.isBlank(pre)) {
			pre = "HR4C8VTwGuo.nIaEdUY97YD."; /// Default is the COVID surveillance
		}

		barcodeField = env.getProperty("lab.specimen.barcode");

		String barcodes = builder.toString();

		builder = new StringBuilder();
		builder.append(dhis2Url);
		builder.append("/events.json?programStage=" + programStage);
		builder.append("&program=" + program);
		builder.append("&filter=" + barcodeField + ":IN:" + barcodes);

		logger.info("Final url is " + builder.toString());

		EventList eventList = restTemplate().getForObject(builder.toString(), EventList.class);

		builder = new StringBuilder();
		builder.append("{\n");
		builder.append("\"events\" : [\n");

		for (Event event : eventList.getEvents()) {
			SpecimenVO s = eventToSpecimen(event, false);
			if (s == null) {
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
					if(StringUtils.isBlank(sp.getProgramId()) || sp.getProgramId().equals(covidProgram)) {
						val.setValue("PENDING");
					} else {
						val.setValue("ORDERED");
					}
				}
				
				Iterator<DataValue> it = event.getDataValues().iterator();
				boolean found = false;
				while(it.hasNext()) {
					DataValue v = it.next();
					if(v.getDataElement().equals(val.getDataElement())) {
						v.setValue(val.getValue());
						found = true;
						break;
					}
				}
				
				if(!found) {
					event.getDataValues().add(val);
				}
			}

			if (!StringUtils.isBlank(sp.getResultsEnteredBy())
					&& !StringUtils.isBlank(env.getProperty(pre + "lab.results.entered.by"))) {
				val = new DataValue();
				val.setDataElement(env.getProperty(pre + "lab.results.entered.by"));
				val.setValue(sp.getResultsEnteredBy());
				
				Iterator<DataValue> it = event.getDataValues().iterator();
				boolean found = false;
				while(it.hasNext()) {
					DataValue v = it.next();
					if(v.getDataElement().equals(val.getDataElement())) {
						v.setValue(val.getValue());
						found = true;
						break;
					}
				}
				
				if(!found) {
					event.getDataValues().add(val);
				}
			}

			if (sp.getResultsEnteredDate() != null
					&& !StringUtils.isBlank(env.getProperty("lab.results.date.entered"))) {

				val = new DataValue();
				val.setDataElement(env.getProperty("lab.results.date.entered"));

				Instant dob = sp.getResultsEnteredDate().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
						.withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);

				val.setValue(date);
				
				Iterator<DataValue> it = event.getDataValues().iterator();
				boolean found = false;
				while(it.hasNext()) {
					DataValue v = it.next();
					if(v.getDataElement().equals(val.getDataElement())) {
						v.setValue(val.getValue());
						found = true;
						break;
					}
				}
				
				if(!found) {
					event.getDataValues().add(val);
				}
			}

			if (!StringUtils.isBlank(sp.getResultsVerifiedBy())
					&& !StringUtils.isBlank(env.getProperty(pre + "lab.results.verified.by"))) {
				val = new DataValue();
				val.setDataElement(env.getProperty(pre + "lab.results.verified.by"));
				val.setValue(sp.getResultsVerifiedBy());
				
				Iterator<DataValue> it = event.getDataValues().iterator();
				boolean found = false;
				while(it.hasNext()) {
					DataValue v = it.next();
					if(v.getDataElement().equals(val.getDataElement())) {
						v.setValue(val.getValue());
						found = true;
						break;
					}
				}
				
				if(!found) {
					event.getDataValues().add(val);
				}
			}

			if (sp.getResultsVerifiedDate() != null
					&& !StringUtils.isBlank(env.getProperty(pre + "lab.result.date.verified"))) {
				val = new DataValue();
				val.setDataElement(env.getProperty(pre + "lab.result.date.verified"));

				Instant dob = sp.getResultsVerifiedDate().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
						.withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);

				val.setValue(date);
				
				Iterator<DataValue> it = event.getDataValues().iterator();
				boolean found = false;
				while(it.hasNext()) {
					DataValue v = it.next();
					if(v.getDataElement().equals(val.getDataElement())) {
						v.setValue(val.getValue());
						found = true;
						break;
					}
				}
				
				if(!found) {
					event.getDataValues().add(val);
				}
			}

			if (!StringUtils.isBlank(sp.getResultsAuthorisedBy())
					&& !StringUtils.isBlank(env.getProperty(pre + "lab.results.authorised.by"))) {
				val = new DataValue();
				val.setDataElement(env.getProperty(pre + "lab.results.authorised.by"));
				val.setValue(sp.getResultsAuthorisedBy());
				
				Iterator<DataValue> it = event.getDataValues().iterator();
				boolean found = false;
				while(it.hasNext()) {
					DataValue v = it.next();
					if(v.getDataElement().equals(val.getDataElement())) {
						v.setValue(val.getValue());
						found = true;
						break;
					}
				}
				
				if(!found) {
					event.getDataValues().add(val);
				}
			}

			if (sp.getResultsAuthorisedDate() != null
					&& !StringUtils.isBlank(env.getProperty(pre + "lab.results.date.authorised"))) {
				val = new DataValue();
				val.setDataElement(env.getProperty(pre + "lab.results.date.authorised"));

				Instant dob = sp.getResultsAuthorisedDate().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
						.withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);

				val.setValue(date);
				
				Iterator<DataValue> it = event.getDataValues().iterator();
				boolean found = false;
				while(it.hasNext()) {
					DataValue v = it.next();
					if(v.getDataElement().equals(val.getDataElement())) {
						v.setValue(val.getValue());
						found = true;
						break;
					}
				}
				
				if(!found) {
					event.getDataValues().add(val);
				}
			}

			if (sp.getReceivingDateTime() != null
					&& !StringUtils.isBlank(env.getProperty(pre + "lab.specimen.date.received"))) {
				val = new DataValue();
				val.setDataElement(env.getProperty(pre + "lab.specimen.date.received"));

				Instant dob = sp.getReceivingDateTime().toInstant();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
						.withZone(ZoneId.systemDefault());
				String date = formatter.format(dob);

				val.setValue(date);
				
				Iterator<DataValue> it = event.getDataValues().iterator();
				boolean found = false;
				while(it.hasNext()) {
					DataValue v = it.next();
					if(v.getDataElement().equals(val.getDataElement())) {
						v.setValue(val.getValue());
						found = true;
						break;
					}
				}
				
				if(!found) {
					event.getDataValues().add(val);
				}
			}

			val = values.get(env.getProperty("lab.results"));
			//logger.info(event.toString());

			// Only push if the results from dhis is not the same as redcap
			// AND the dhis results are not authorised and the authorised date is blank
			boolean sameResults = val != null && val.getValue().equalsIgnoreCase(sp.getResults());
			boolean synchResults = !sameResults;

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

				String url = this.dhis2Url + "/events/" + event.getEvent() + ".json";

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
