package bw.ub.ehealth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.specimen.service.SpecimenService;

@RestController
@RequestMapping("/analytics")
@CrossOrigin()
public class Analytics {
	
	@Autowired
	private SpecimenService specimenService;
	
	public long getTotalReceived() {
		
		return 0;
	}

}
