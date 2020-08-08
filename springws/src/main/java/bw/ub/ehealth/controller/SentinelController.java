package bw.ub.ehealth.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.specimen.service.SpecimenService;
import bw.ub.ehealth.dhislink.specimen.vo.SpecimenVO;
import bw.ub.ehealth.dhislink.vo.Event;
import bw.ub.ehealth.dhislink.vo.Program;
import bw.ub.ehealth.dhislink.vo.ProgramStage;

@RestController
@RequestMapping("/sentinel")
@CrossOrigin()
public class SentinelController {

    private Logger logger = LoggerFactory.getLogger(DDPController.class);

    @Value("${dhis2.api.url}")
    private String dhis2Url;

    @Value("${sentinel.id}")
    private String sentinelId;

    @Value("${sentinel.program.stage.examination}")
    private String sentinelExaminationId;
    
    @Autowired
    private DhisLink dhisLink;
    
    @Autowired
    private RedcapLink redcapLink;
    
    @Autowired
    private RedcapDataService redcapDataService;

    @Autowired
    private SpecimenService specimenService;
    
    private static Program sentinel = null;

    private static ProgramStage programStage = null;
    
    @GetMapping(produces = "application/json")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public Program getSentinelProgram() {
    	
    	if(sentinel == null) {
    		return dhisLink.getProgram(sentinelId);
    	}
    	
    	return sentinel;
    }
    
    @GetMapping(value = "/stage", produces = "application/json")
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public ProgramStage getSentinelExaminationStage() {
    	
    	if(programStage == null) {
    		return dhisLink.getProgramStage(sentinelExaminationId);
    	}
    	
    	return programStage;
    }
    
    @GetMapping(value = "/events", produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Event> getEvents() {

    	Map<String, String> params = new HashMap<>();
        params.put("program", sentinelId);
        params.put("programStage", sentinelExaminationId);
        SpecimenVO last = specimenService.findLatestSpecimen(sentinelId);
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
            params.put("page", "" + page);
            tmp = dhisLink.getEvents(params);
            //break;
        }

        return allEvents;
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
		return dhisLink.getSpecimen(sentinelId, sentinelExaminationId);
    }
}
