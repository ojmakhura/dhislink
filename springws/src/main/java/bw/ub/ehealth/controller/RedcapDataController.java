package bw.ub.ehealth.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.patient.vo.PatientVO;
import bw.ub.ehealth.dhislink.redacap.data.RedcapData;
import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.redacap.data.vo.BatchVO;
import bw.ub.ehealth.dhislink.batch.BatchAuthorityStage;
import bw.ub.ehealth.dhislink.instrument.vo.InstrumentVO;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;
import bw.ub.ehealth.dhislink.location.service.LocationService;
import bw.ub.ehealth.dhislink.location.vo.LocationVO;
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

	@Value("${sentinel.id}")
	private String sentinelId;

	@Value("${sentinel.program.stage.examination}")
	private String sentinelExaminationId;

	@Value("${dhis2.api.program}")
	private String covidProgram;

	@Value("${dhis2.api.program.stage}")
	private String covidProgramStage;

	@Value("${lab.specimen.barcode}")
	private String barcodeField;

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

		for (SpecimenVO sp : specimen) {
			map.put(sp.getSpecimenBarcode(), sp);
		}

		return map;
	}

	private List<SpecimenVO> queryDhisSpecimenBarcodes(List<String> barcodes, String program, String stage,
			String field) {

		if (Collections.isEmpty(barcodes)) {
			return new ArrayList<>();
		}

		String queryBase = dhis2Url + "/events.json?" + "programStage=" + stage + "&program=" + program + "&filter="
				+ field + ":IN:";
		StringBuilder builder = new StringBuilder();
		for (String barcode : barcodes) {
			if (builder.length() > 0) {
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

		if (Collections.isEmpty(specimenToPull)) {
			return new ArrayList<>();
		}

		List<SpecimenVO> noInfo = new ArrayList<>();
		List<String> covidBarcodes = new ArrayList<>();
		List<String> sentinelBarcodes = new ArrayList<>();
		Map<String, SpecimenVO> sps = new HashMap<>();

		for (SpecimenVO sp : specimenToPull) {
			if (StringUtils.isBlank(sp.getEvent())) {
				noInfo.add(sp);
				sps.put(sp.getSpecimenBarcode(), sp);

				if (StringUtils.isBlank(sp.getProgramId()) || sp.getProgramId().equals(covidProgram)) {
					covidBarcodes.add(sp.getSpecimenBarcode());
				} else {
					sentinelBarcodes.add(sp.getSpecimenBarcode());
				}
			}
		}

		List<SpecimenVO> pulled = queryDhisSpecimenBarcodes(covidBarcodes, covidProgram, covidProgramStage,
				barcodeField);
		pulled.addAll(queryDhisSpecimenBarcodes(sentinelBarcodes, sentinelId, sentinelExaminationId, barcodeField));

		redcapLink.updateStaging(pulled);

		for (SpecimenVO sp : pulled) {
			SpecimenVO s = sps.get(sp.getSpecimenBarcode());
			int idx = decodePosition(s.getPosition()) - 1;
			sp.setPosition(s.getPosition());

			sp.setBatchNumber(s.getBatchNumber());
			sp.setCovidRnaResults(s.getCovidRnaResults());
			sp.setResults(s.getResults());
			sp.setReceivingConditionCode(s.getReceivingConditionCode());
			sp.setReceivingDateTime(s.getReceivingDateTime());
			sp.setReceivingLab(s.getReceivingLab());
			sp.setReceivingPersonnel(s.getReceivingPersonnel());
			sp.setResultsAuthorisedBy(s.getResultsAuthorisedBy());
			sp.setResultsAuthorisedDate(s.getResultsAuthorisedDate());
			sp.setResultsEnteredBy(s.getResultsEnteredBy());
			sp.setResultsEnteredDate(s.getResultsEnteredDate());
			sp.setResultsVerifiedBy(s.getResultsVerifiedBy());
			sp.setResultsVerifiedDate(s.getResultsVerifiedDate());
			sp.setTestAssayResults(s.getTestAssayResults());

			specimenToPull.set(idx, sp);
		}

		return specimenToPull;
	}

	@PostMapping("/savebatch")
	@ResponseStatus(code = HttpStatus.OK)
	@ResponseBody
	public List<SpecimenVO> saveBatch(@RequestBody BatchVO batch) {
		List<RedcapDataVO> redcapData = new ArrayList<RedcapDataVO>();
		List<SpecimenVO> verifiedSpecimen = new ArrayList<SpecimenVO>();

		if (StringUtils.isBlank(batch.getAssayBatchId())) {
			batch.setAssayBatchId(batch.getBatchId());
		}

		if (StringUtils.isBlank(batch.getDetectionBatchId())) {
			batch.setDetectionBatchId(batch.getBatchId());
		}

		if (StringUtils.isBlank(batch.getVerifyBatchId())) {
			batch.setVerifyBatchId(batch.getBatchId());
		}

		if (batch.getPage() == BatchAuthorityStage.DETECTION) {

			redcapData.add(
					getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_id", batch.getBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_batch_id",
					batch.getDetectionBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_batch_id",
					batch.getAssayBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batch_id",
					batch.getVerifyBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_personnel",
					batch.getDetectionPersonnel()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_datetime",
					batch.getDetectionDateTime()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "detection_lab",
					batch.getLab().getCode()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_instrument",
					batch.getInstrument().getCode()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_batchsize",
					batch.getDetectionSize().toString()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "testing_detection_complete",
					batch.getDetectionStatus()));

		} else if (batch.getPage() == BatchAuthorityStage.RESULTING) {
			redcapData.add(
					getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_id", batch.getBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_batch_id",
					batch.getDetectionBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_batch_id",
					batch.getAssayBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_personnel",
					batch.getResultingPersonnel()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_datetime",
					batch.getResultingDateTime()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_assay_batchsize",
					batch.getDetectionSize().toString()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "resulting_complete",
					batch.getDetectionStatus()));

		} else if (batch.getPage() == BatchAuthorityStage.AUTHORISATION) {
			batch.setVerifyBatchId(batch.getBatchId());
			redcapData.add(
					getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_det_id", batch.getBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batch_id",
					batch.getDetectionBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batch_id",
					batch.getVerifyBatchId()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_personnel",
					batch.getVerificationPersonnel()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_datetime",
					batch.getVerificationDateTime()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_batchsize",
					batch.getDetectionSize().toString()));
			redcapData.add(getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "verification_complete",
					batch.getVerificationStatus()));

		}

		/// Detect barcodes that do not have data in the stagins
		List<String> covidMissing = new ArrayList<>();
		List<String> sentinelMissing = new ArrayList<>();

		for (SpecimenVO specimen : batch.getBatchItems()) {
			if (specimen.getId() == null) {
				// Record this somewhere
				if (StringUtils.isBlank(specimen.getProgramId()) || specimen.getProgramId().equals(covidProgram)) {
					covidMissing.add(specimen.getSpecimenBarcode());
				} else {
					sentinelMissing.add(specimen.getSpecimenBarcode());
				}
				// Save the specimen in the staging area

				if (specimen.getPatient() != null && StringUtils.isBlank(specimen.getPatient().getIdentityNo())) {
					specimen.setPatient(null);
				}
				specimenService.saveSpecimen(specimen);
				if (specimen.getPatient() == null) {
					specimen.setPatient(new PatientVO());
				}
			}
		}

		// Find the specimen from DHIS2
		List<SpecimenVO> found = new ArrayList<>();
		if (batch.getPublishResults()) {
			found = queryDhisSpecimenBarcodes(covidMissing, covidProgram, covidProgramStage, barcodeField);
			found.addAll(queryDhisSpecimenBarcodes(sentinelMissing, covidProgram, covidProgramStage, barcodeField));
		}
		Map<String, SpecimenVO> foundMap = getSpecimenMap(found);

		for (SpecimenVO specimen : batch.getBatchItems()) {

			if (foundMap.containsKey(specimen.getSpecimenBarcode())) {
				String position = specimen.getPosition();
				specimen = foundMap.get(specimen.getSpecimenBarcode());
				specimen.setPosition(position);
			}

			int pos = decodePosition(specimen.getPosition());
			if (batch.getPage() == BatchAuthorityStage.DETECTION) {

				RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(),
						"test_det_barcode_" + pos, specimen.getSpecimenBarcode());
				redcapData.add(data);

			} else if (batch.getPage() == BatchAuthorityStage.RESULTING) {

				RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(),
						"test_assay_result_" + pos, specimen.getTestAssayResults());
				redcapData.add(data);

				specimen.setResultsEnteredBy(batch.getResultingPersonnel());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				try {
					specimen.setResultsEnteredDate(format.parse(batch.getResultingDateTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}

			} else if (batch.getPage() == BatchAuthorityStage.AUTHORISATION) {

				if (!StringUtils.isBlank(specimen.getTestVerifyResults())
						&& specimen.getTestVerifyResults().equals("5")) {
					specimen.setCovidRnaResults(specimen.getTestAssayResults());
					RedcapDataVO data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(),
							"covid_rna_results" + pos, specimen.getCovidRnaResults());
					redcapData.add(data);

					data = getRedcapDataObjet(batch.getBatchId(), batch.getProjectId(), "test_verify_result_" + pos,
							specimen.getTestVerifyResults());
					redcapData.add(data);

					specimen.setResults(specimen.getCovidRnaResults());
					if (specimen.getId() != null) {
						verifiedSpecimen.add(specimen);
					}
				}

				if (!StringUtils.isBlank(batch.getVerificationPersonnel())) {
					specimen.setResultsVerifiedBy(batch.getVerificationPersonnel());

					try {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
						specimen.setResultsVerifiedDate(format.parse(batch.getVerificationDateTime()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}

				if (!StringUtils.isBlank(batch.getAuthorisingPersonnel())) {

					specimen.setResultsAuthorisedBy(batch.getAuthorisingPersonnel());

					try {
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
						specimen.setResultsAuthorisedDate(format.parse(batch.getAuthorisingDateTime()));
					} catch (ParseException e) {

						e.printStackTrace();
					}
				}
			}

		}
		// logger.info(redcapData.toString());
		// Save the data for this particular project
		// redcapLink.postSpecimen(batch.getBatchItems(), batch.getProjectId());
		redcapLink.postRedcapData(redcapData, batch.getProjectId());

		// Update the staging area. This also updated the lab report
		redcapLink.updateStaging(batch.getBatchItems());

		if (batch.getPublishResults() && isLive) {
			// logger.info(String.format("%d specimen data sent to DHIS2 and they are %s",
			// verifiedSpecimen.size(), verifiedSpecimen.toString()));
			dhisLink.getDhisPayload(verifiedSpecimen);
		}

		List<SpecimenVO> tmp = new ArrayList<>();

		for (SpecimenVO specimen : batch.getBatchItems()) {
			if (foundMap.containsKey(specimen.getSpecimenBarcode())) {
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
	public Collection<SpecimenVO> fetchBatchSpecimen(@RequestBody BatchVO batch) {

		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
		criteria.setRecord(batch.getBatchId());
		criteria.setProjectId(batch.getProjectId());

		Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);

		Collection<SpecimenVO> specimens = new ArrayList<SpecimenVO>();

		if (tmp != null && tmp.size() > 0) {

			Map<String, RedcapDataVO> map = getDataMap((List<RedcapDataVO>) tmp);
			specimens = getDetectionSpecimen(batch, map);
		}

		return specimens;
	}

	private Collection<SpecimenVO> doFetchBatchSpecimen(RedcapDataSearchCriteria criteria) {
		Collection<SpecimenVO> specimens = new ArrayList<SpecimenVO>();
		Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);

		for (RedcapDataVO rd : tmp) {
			SpecimenVO specimen = specimenService.findSpecimenByBarcode(rd.getValue());

			if (specimen == null) {
				specimen = new SpecimenVO();
				specimen.setSpecimenBarcode(rd.getValue());
				specimen.setPatient(new PatientVO());
			}

			if (specimen.getPatient() == null || specimen.getPatient().getId() == null) {
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
	public Collection<RedcapDataVO> saveRedcapData(@RequestBody Collection<RedcapDataVO> data,
			@RequestBody Long projectId) {

		redcapLink.postRedcapData((List<RedcapDataVO>) data, projectId);

		return data;
	}

	/**
	 * TODO: Model Documentation for
	 * bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.searchByCriteria
	 * 
	 * @param searchCriteria TODO: Model Documentation for
	 *                       bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.searchByCriteria(searchCriteria)
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
		for (RedcapDataVO d : data) {
			List<RedcapDataVO> tmp = map.get(d.getRecord());

			if (tmp == null) {
				tmp = new ArrayList<RedcapDataVO>();
				map.put(d.getRecord(), tmp);
			}

			tmp.add(d);
		}

		return map;
	}

	private Map<String, RedcapDataVO> getDataMap(List<RedcapDataVO> data) {
		HashMap<String, RedcapDataVO> map = new HashMap<String, RedcapDataVO>();

		for (RedcapDataVO d : data) {
			map.put(d.getFieldName(), d);
		}

		return map;
	}

	private String getInstrumentName(String code) {

		String name = "";
		if (code.equals("122")) {
			name = "NHL ROCHE Z480 (122)";
		} else if (code.equals("221")) {

			name = "BHHRL ABI 7500 S/N 750S8180106 (221)";
		} else if (code.equals("222")) {

			name = "BHHRL m2000rt S/N 275020775 (222)";
		} else if (code.equals("321")) {
			name = "UB ABI 7500 (321)";

		} else if (code.equals("421")) {

			name = "BNVL ABI 7500 FAST (421)";
		} else if (code.equals("521")) {

			name = "BVI ABI 7500 (521)";
		} else if (code.equals("999")) {

			name = "Other detection machine(specify) (999))";
		}

		return name;
	}

	private Collection<SpecimenVO> getDetectionSpecimen(BatchVO batch, Map<String, RedcapDataVO> map) {

		String prefix = "test_det_barcode_";
		String assayResPrefix = "test_assay_result_";
		String rnaResPrefix = "covid_rna_results";
		String verifyPrefix = "test_verify_result_";

		Collection<SpecimenVO> sps = new ArrayList<SpecimenVO>();

		if (batch.getDetectionSize() == null || batch.getDetectionSize() == 0) {
			return sps;
		}

		for (int i = 1; i <= batch.getDetectionSize(); i++) {
			RedcapDataVO vo = map.get(prefix + i);

			if (vo != null) {
				SpecimenVO specimen = specimenService.findSpecimenByBarcode(vo.getValue());

				if (specimen == null) {
					specimen = new SpecimenVO();
					specimen.setSpecimenBarcode(vo.getValue());
				}

				specimen.setPosition(encodePosition(i));

				vo = map.get(assayResPrefix + i);
				if (vo != null) {
					specimen.setTestAssayResults(vo.getValue());
				}

				vo = map.get(rnaResPrefix + i);
				if (vo != null) {
					specimen.setCovidRnaResults(vo.getValue());
				}

				vo = map.get(verifyPrefix + i);
				if (vo != null) {
					specimen.setTestVerifyResults(vo.getValue());
				}

				if (specimen.getPatient() == null) {
					PatientVO patient = new PatientVO();
					patient.setFirstName("");
					patient.setSurname("");
					patient.setIdentityNo("");

					specimen.setPatient(patient);
				}

				if (StringUtils.isBlank(batch.getAuthorisingPersonnel())
						&& !StringUtils.isBlank(specimen.getResultsAuthorisedBy())) {
					batch.setAuthorisingPersonnel(specimen.getResultsAuthorisedBy());

					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
					batch.setAuthorisingDateTime(format.format(specimen.getResultsAuthorisedDate()));
				}

				sps.add(specimen);
			}
		}

		// logger.info(batch.toString());
		return sps;
	}

	private BatchVO getBatchFromRedcapData(List<RedcapDataVO> redcapData, boolean includeSpecimen,
			BatchAuthorityStage stage) {
		Map<String, RedcapDataVO> map = getDataMap(redcapData);
		BatchVO batch = new BatchVO();

		RedcapDataVO vo = map.get("detection_lab");
		if (vo != null) {
			LocationVO loc = locationService.searchByCode(vo.getValue());
			batch.setLab(loc);
		}

		vo = map.get("test_det_instrument");
		if (vo != null) {

			InstrumentVO instrument = new InstrumentVO(vo.getValue(), getInstrumentName(vo.getValue()));
			batch.setInstrument(instrument);
		}

		vo = map.get("test_assay_batchsize");

		if (vo != null) {
			batch.setAssayBatchId(vo.getRecord());
			batch.setBatchId(batch.getAssayBatchId());
			batch.setDetectionBatchId(batch.getAssayBatchId());

			Long size = Long.parseLong(vo.getValue());
			batch.setInstrumentBatchSize(size);
			batch.setDetectionSize(size);
		}

		vo = map.get("test_det_id");

		if (vo != null) {
			batch.setDetectionBatchId(vo.getValue());
			batch.setBatchId(batch.getDetectionBatchId());
			batch.setAssayBatchId(batch.getDetectionBatchId());
		}

		vo = map.get("test_det_batchsize");

		if (vo != null) {
			Long size = Long.parseLong(vo.getValue());
			batch.setDetectionSize(size);
			batch.setInstrumentBatchSize(size);
		}

		vo = map.get("test_verify_batchsize");

		if (vo != null) {
			Long size = Long.parseLong(map.get("test_verify_batchsize").getValue());
			batch.setDetectionSize(size);
			batch.setInstrumentBatchSize(size);

			batch.setVerifyBatchId(vo.getRecord());
			batch.setBatchId(batch.getVerifyBatchId());
			batch.setAssayBatchId(batch.getVerifyBatchId());
		}

		vo = map.get("resulting_complete");
		if (vo != null) {
			batch.setResultingStatus(vo.getValue());
		}

		vo = map.get("verification_complete");
		if (vo != null) {
			batch.setVerificationStatus(vo.getValue());
		}

		vo = map.get("testing_detection_complete");
		if (vo != null) {
			batch.setDetectionStatus(vo.getValue());
		}

		// if (stage == BatchAuthorityStage.RESULTING) {

		vo = map.get("test_assay_datetime");
		if (vo != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				Date dt = format.parse(vo.getValue());
				format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				batch.setResultingDateTime(format.format(dt));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		vo = map.get("test_assay_personnel");
		if (vo != null) {
			batch.setResultingPersonnel(vo.getValue());
		}
		// }

		// if (stage == BatchAuthorityStage.DETECTION) {

		vo = map.get("test_det_datetime");
		if (vo != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				Date dt = format.parse(vo.getValue());
				format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				batch.setDetectionDateTime(format.format(dt));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		vo = map.get("test_det_personnel");
		if (vo != null) {
			batch.setDetectionPersonnel(vo.getValue());
		}
		// }

		// if (stage == BatchAuthorityStage.AUTHORISATION) {

		vo = map.get("test_verify_datetime");
		if (vo != null) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			try {
				Date dt = format.parse(vo.getValue());
				format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
				batch.setVerificationDateTime(format.format(dt));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		vo = map.get("test_verify_personnel");
		if (vo != null) {
			batch.setVerificationPersonnel(vo.getValue());
		}
		// }

		if (includeSpecimen) {
			batch.setBatchItems(getDetectionSpecimen(batch, map));
		}

		if (batch.getDetectionSize() == null) {
			batch.setDetectionSize(0l);
		}

		if (batch.getInstrumentBatchSize() == null) {
			batch.setInstrumentBatchSize(0l);
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

		if (ch == 'A') {

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

		if (r == 0) {
			r = 12;
			q--;
		}

		String position = "";

		if (q == 0) {
			position = "A" + r;
		} else if (q == 1) {
			position = "B" + r;
		} else if (q == 2) {
			position = "C" + r;
		} else if (q == 3) {
			position = "D" + r;
		} else if (q == 4) {
			position = "E" + r;
		} else if (q == 5) {
			position = "F" + r;
		} else if (q == 6) {
			position = "G" + r;
		} else if (q == 7) {
			position = "H" + r;
		}

		return position;
	}

	@PostMapping("/searches/batch")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public List<BatchVO> _searchBatches(@RequestBody BatchSearchCriteria searchCriteria) {

		return null;
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

		if (searchCriteria.getIncludeSpecimen() == null) {
			searchCriteria.setIncludeSpecimen(true);
		}

		List<BatchVO> batches = new ArrayList<BatchVO>();
		RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();

		if (searchCriteria.getPage() == BatchAuthorityStage.DETECTION
				|| searchCriteria.getPage() == BatchAuthorityStage.AUTHORISATION
				|| searchCriteria.getPage() == BatchAuthorityStage.RESULTING) {
			criteria.setProjectId(labResultingPID);
		} else if (searchCriteria.getPage() == BatchAuthorityStage.RECEPTION
				|| searchCriteria.getPage() == BatchAuthorityStage.RECEPTION_CONDITION
				|| searchCriteria.getPage() == BatchAuthorityStage.TESTING_RECEPTION) {
			criteria.setProjectId(labReceptionPID);
		} else if (searchCriteria.getPage() == BatchAuthorityStage.EXTRACTION) {
			criteria.setProjectId(labExtractionPID);
		}

		if (!StringUtils.isBlank(searchCriteria.getBatchId())) {
			criteria.setRecord(searchCriteria.getBatchId());
			Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);

			if (tmp != null && tmp.size() > 0) {

				Map<String, List<RedcapDataVO>> map = getRedcapDataBatchMap(tmp);
				batches.add(getBatchFromRedcapData(map.get(searchCriteria.getBatchId()),
						searchCriteria.getIncludeSpecimen(), searchCriteria.getPage()));
			}

		} else if (!StringUtils.isBlank(searchCriteria.getSpecimenBarcode())) {

			criteria.setValue(searchCriteria.getSpecimenBarcode());
			Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);

			Set<String> records = new HashSet<String>();
			for (RedcapDataVO rd : tmp) {
				records.add(rd.getRecord());
			}

			for (String record : records) {
				criteria.setValue(null);
				criteria.setRecord(record);
				Collection<RedcapDataVO> t2 = redcapDataService.searchByCriteria(criteria);
				Map<String, List<RedcapDataVO>> map = getRedcapDataBatchMap(t2);
				batches.add(getBatchFromRedcapData(map.get(record), false, searchCriteria.getPage()));

			}

		} else if (!StringUtils.isBlank(searchCriteria.getLab())) {

			criteria.setValue(searchCriteria.getLab());

			if (searchCriteria.getPage() == BatchAuthorityStage.DETECTION
					|| searchCriteria.getPage() == BatchAuthorityStage.AUTHORISATION) {
				criteria.setFieldName("detection_lab");
			} else if (searchCriteria.getPage() == BatchAuthorityStage.RECEPTION
					|| searchCriteria.getPage() == BatchAuthorityStage.RECEPTION_CONDITION) {
				criteria.setFieldName("reception_lab");
			} else if (searchCriteria.getPage() == BatchAuthorityStage.TESTING_RECEPTION) {
				criteria.setFieldName("tpor_lab");
			} else if (searchCriteria.getPage() == BatchAuthorityStage.EXTRACTION) {
				criteria.setFieldName("extraction_lab");
			}

			Collection<RedcapDataVO> tmp = redcapDataService.searchByCriteria(criteria);

			Set<String> records = new HashSet<String>();
			for (RedcapDataVO rd : tmp) {
				records.add(rd.getRecord());
			}

			for (String record : records) {
				criteria.setFieldName(null);
				criteria.setRecord(record);
				criteria.setValue(null);

				Collection<RedcapDataVO> t2 = redcapDataService.searchByCriteria(criteria);

				Map<String, List<RedcapDataVO>> map = getRedcapDataBatchMap(t2);

				batches.add(getBatchFromRedcapData(map.get(record), false, searchCriteria.getPage()));
			}
		}

		return batches;
	}

}
