package bw.ub.ehealth.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataSearchCriteria;
import bw.ub.ehealth.dhislink.redacap.data.vo.RedcapDataVO;

@RestController
@RequestMapping("/ddpcontroller/data")
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
    
	@Autowired
	private RedcapDataService redcapDataService;
	
	@Autowired
	private RedcapLink redcapLink;
	
	/**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.saveRedcapData
     * @param redcapDataVO TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.saveRedcapData(redcapDataVO)
     * @return RedcapDataVO
     */    
    @PostMapping("/saveone")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public RedcapDataVO saveRedcapData(RedcapDataVO redcapDataVO) {
    	
    	return redcapDataService.saveRedcapData(redcapDataVO);
    }
    
    @PostMapping("/saveall")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<RedcapDataVO> saveRedcapData(Collection<RedcapDataVO> data, Integer projectId) {
    	    	
    	redcapLink.doPostRedcapData(data, project);
    	
    	return dataList;
    	
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.searchByCriteria
     * @param searchCriteria TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.searchByCriteria(searchCriteria)
     * @return Collection<RedcapDataVO>
     */
    public Collection<RedcapDataVO> searchByCriteria(RedcapDataSearchCriteria searchCriteria) {
    	
    	return redcapDataService.searchByCriteria(searchCriteria);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.findMaxEvent
     * @param projectId TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.data.service.RedcapDataService.findMaxEvent(projectId)
     * @return Long
     */
    public Long findMaxEvent(Long projectId) {
    	
    	return redcapDataService.findMaxEvent(projectId);
    }
}
