package bw.ub.ehealth.controller;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;

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

import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;
import bw.ub.ehealth.dhislink.specimen.service.SpecimenService;
import bw.ub.ehealth.dhislink.specimen.vo.SpecimenVO;
import bw.ub.ehealth.dhislink.vo.CurrentUser;
import bw.ub.ehealth.dhislink.vo.DDPPostObject;
import bw.ub.ehealth.dhislink.vo.Event;
import bw.ub.ehealth.dhislink.vo.Program;
import bw.ub.ehealth.dhislink.vo.TrackedEntityInstance;

@RestController
@RequestMapping("/ddpcontroller")
@CrossOrigin()
public class DDPController {

    private Logger logger = LoggerFactory.getLogger(DDPController.class);

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
    private RedcapDataService redcapDataService;

    @Autowired
    private SpecimenService specimenService;
    
    @Autowired
    private DhisLink dhisLink;
    
    @Autowired
    private RedcapLink redcapLink;

    @GetMapping(value = "/getdhisuser")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public CurrentUser getDhisUser() {
        return dhisLink.getCurrentUser();
    }

    @GetMapping(value = "/events", produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Event> getEvents() {

    	Map<String, String> params = new HashMap<>();
        params.put("program", program);
        params.put("programStage", programStage);
        SpecimenVO last = specimenService.findLatestSpecimen(program);
        String date = "2020-05-20";
        
        if(last != null) {
        	
        	Calendar cal = Calendar.getInstance();
        	cal.setTime(last.getCreated());
        	
        } 
        params.put("startDate", date);
        params.put("order", "eventDate:asc");
        params.put("pageSize", "50");

        int page = 1;
        params.put("page", "" + page);
        List<Event> allEvents = new ArrayList<>();
        List<Event> tmp = dhisLink.getEvents(params);

        while(tmp != null && tmp.size() > 0) {
            allEvents.addAll(tmp);
            page++;
            tmp = dhisLink.getEvents(params);
        }

        return allEvents;
    }

    /**
     * 
     * 
     * @param postObject
     * @return
     */
    /*@PostMapping(value = "/ddp", produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String pullSpecimen(@RequestBody DDPPostObject postObject) {
    	SpecimenVO specimen = dhisLink.getOneSpecimen(postObject.getId());
        return dhisLink.getSpecimenFieldsString(specimen);
    }*/
    
    /**
     * Retrieve one specimen based on the barcode parameter
     * 
     * @param barcode
     * @return
     */
    @GetMapping(value = "/onespecimen/{barcode}")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public String getOneSpecimen(@PathVariable(name = "barcode") String barcode) {
    	
    	SpecimenVO specimen = dhisLink.getOneSpecimen(barcode);    	
    	return dhisLink.getSpecimenFieldsString(specimen);
    }

    /**
     * Pull new specimen information from dhis2 
     * 
     * @return
     */
    @GetMapping(value = "/newspecimen", produces = "application/json")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public List<SpecimenVO> getSpecimen() {			
		return dhisLink.getSpecimen(program, programStage);
    }

    @GetMapping(value = "/program", produces = "application/json")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public Program getProgram() {

        return dhisLink.getProgram(program);
    }

    @GetMapping(value = "/trackedentityinstance")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public TrackedEntityInstance getTrackedEntityInstance() {
        TrackedEntityInstance instance = dhisLink.getTrackedEntityInstance("Arv8sb0gLDR", "hGCed18kx7t");
        return instance;
    }

    @GetMapping(value = "/getredcapdata", produces = "application/json")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public List<RedcapDataVO> getRedcapData(@RequestBody @NotNull String barcode) {
    	
    	RedcapDataSearchCriteria criteria = new RedcapDataSearchCriteria();
    	criteria.setFieldName("specimen_barcode");
    	criteria.setValue(barcode);
    	
    	return (List<RedcapDataVO>) redcapDataService.searchByCriteria(criteria);
    	
    }
    
    /**
     * 
     * 
     * @return
     */
    @GetMapping(value = "/dhissynch", produces = "application/json") 
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public String sendDhisResults() {
    	
    	StringBuilder builder = new StringBuilder();
    	// Make sure the staging area is updated
    	Collection<SpecimenVO> updateable = specimenService.findUnsynchedSpecimen();
    	redcapLink.updateStaging(updateable);
    	Collection<SpecimenVO> tmp = null;
    	
    	for(SpecimenVO specimen : updateable) {
    		if(tmp == null) {
    			tmp = new ArrayList<>();
    		}
    		
    		tmp.add(specimen);
    		
    		if(tmp.size() == 100) {
    			builder.append(dhisLink.getDhisPayload(tmp));
    			tmp.clear();
    		}
    	}
    	
    	return builder.toString();
    }
    
    /**
     * 
     * 
     * @return
     */
    @GetMapping(value = "/updateredcapdata") 
    @ResponseStatus(value = HttpStatus.OK)
    public void updateRedcapData() {
    	Collection<SpecimenVO> vo = specimenService.findUnsynchedSpecimen();
    	redcapLink.updateStaging(vo);    	
    }

    @GetMapping(value = "/viewstaging") 
    @ResponseStatus(value = HttpStatus.OK)
    public void viewUpdates() {
    	redcapLink.updateStaging(specimenService.findUnsynchedSpecimen());
    }
}
