package bw.ub.ehealth.controller;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.redacap.location.service.LocationService;
import bw.ub.ehealth.dhislink.redacap.location.vo.LocationVO;

@RestController
@RequestMapping("/ddpcontroller/location")
public class LocationController {
	
	private static final Logger logger = LoggerFactory.getLogger(LocationController.class);
	
	@Autowired
	private LocationService locationService;

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.location.service.LocationService.createLocation
     * @param locationVO TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.location.service.LocationService.createLocation(locationVO)
     * @return LocationVO
     */
	@PostMapping("/new")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public LocationVO createLocation(@NotNull @RequestBody LocationVO locationVO) {
		logger.info("Saving " + locationVO.toString());
		if(locationVO.getId() != null) {
			locationService.updateLocation(locationVO);
			return locationVO;
		}
		
		return locationService.createLocation(locationVO);
	}

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.location.service.LocationService.updateLocation
     * @param locationVO TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.location.service.LocationService.updateLocation(locationVO)
     */
	@PutMapping("/update")
	@ResponseStatus(code = HttpStatus.OK)
    public void updateLocation( @RequestBody LocationVO locationVO) {
		
    	locationService.updateLocation(locationVO);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.location.service.LocationService.deleteLocation
     * @param id TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.location.service.LocationService.deleteLocation(id)
     */
    @DeleteMapping("/delete/{id}")
    public void deleteLocation(@PathVariable Long id) {
    	locationService.deleteLocation(id);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.location.service.LocationService.findById
     * @param id TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.location.service.LocationService.findById(id)
     * @return LocationVO
     */
    @GetMapping("/{id}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public LocationVO findById(@PathVariable Long id) {
    	return locationService.findById(id);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.location.service.LocationService.searchByName
     * @param name TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.location.service.LocationService.searchByName(name)
     * @return Collection<LocationVO>
     */
    @GetMapping("/search/{name}")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<LocationVO> searchByName(@PathVariable String name) {
    	
    	return locationService.searchByName(name);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.location.service.LocationService.findAll
     * @return Collection<LocationVO>
     */
    @GetMapping("/all")
    @ResponseBody
    @ResponseStatus(code = HttpStatus.OK)
    public Collection<LocationVO> findAll() {
    	return locationService.findAll();
    }

}
