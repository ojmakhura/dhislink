package bw.ub.ehealth.controller;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.specimen.service.SpecimenService;
import bw.ub.ehealth.dhislink.specimen.vo.SpecimenVO;

@RestController
@RequestMapping("/ddpcontroller/specimen")
@CrossOrigin()
public class SpecimenController {
	
	private static final Logger logger = LoggerFactory.getLogger(SpecimenController.class);
	
	@Autowired
	private SpecimenService specimenService;
	
	/**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findSpecimenByBarcode
     * @param barcode TODO: Model Documentation for
bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findSpecimenByBarcode(barcode)
     * @return SpecimenVO
     */
	@GetMapping("/barcode/{barcode}")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
    public SpecimenVO findSpecimenByBarcode(@PathVariable String barcode) {
    	return specimenService.findSpecimenByBarcode(barcode);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.deleteSpecimen
     * @param id TODO: Model Documentation for
bw.ub.ehealth.dhislink.specimen.service.SpecimenService.deleteSpecimen(id)
     */
    @DeleteMapping("/delete/{id}")
	@ResponseStatus(code = HttpStatus.OK)
    public void deleteSpecimen(@PathVariable Long id) {
    	specimenService.deleteSpecimen(id);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.saveSpecimen
     * @param specimenVO TODO: Model Documentation for
bw.ub.ehealth.dhislink.specimen.service.SpecimenService.saveSpecimen(specimenVO)
     * @return SpecimenVO
     */
    @PostMapping()
    @ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
    public SpecimenVO saveSpecimen(@NotNull @RequestBody SpecimenVO specimenVO) {
    	
    	return specimenService.saveSpecimen(specimenVO);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findByEvent
     * @param event TODO: Model Documentation for
bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findByEvent(event)
     * @return SpecimenVO
     */
    @GetMapping("/event/{event}")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
    public SpecimenVO findByEvent(@PathVariable String event) {
    	return specimenService.findByEvent(event);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findLatestSpecimen
     * @return SpecimenVO
     */
    @GetMapping("/latest{programId}")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
    public SpecimenVO findLatestSpecimen(@PathVariable String programId) {
    	return specimenService.findLatestSpecimen(programId);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.specimen.service.SpecimenService.findUnsynchedSpecimen
     * @return Collection<SpecimenVO>
     */
    @GetMapping("/unsynched")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
    public Collection<SpecimenVO> findUnsynchedSpecimen() {
    	return specimenService.findUnsynchedSpecimen();
    }

}
