package bw.ub.ehealth.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService;
import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthLocationVO;
import bw.ub.ehealth.dhislink.redacap.auth.vo.RedcapAuthVO;

@RestController
@RequestMapping("/ddpcontroller/location/auth")
public class AuthLocationController {
	@Autowired
	private AuthLocationService authLocationService;
	
	/**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.createAuthLocation
     * @param authLocationVO TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.createAuthLocation(authLocationVO)
     * @return RedcapAuthLocationVO
     */
    public RedcapAuthLocationVO createAuthLocation(RedcapAuthLocationVO authLocationVO) {
    	
    	if(authLocationVO.getId() != null) {
    		this.updateAuthLocation(authLocationVO);
    		return authLocationVO;
    	}
    	
    	return authLocationService.createAuthLocation(authLocationVO); 
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.updateAuthLocation
     * @param authLocationVO TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.updateAuthLocation(authLocationVO)
     */
    public void updateAuthLocation(RedcapAuthLocationVO authLocationVO) {
    	authLocationService.updateAuthLocation(authLocationVO);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.deleteAuthLocation
     * @param id TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.deleteAuthLocation(id)
     */
    public void deleteAuthLocation(Long id) {
    	authLocationService.deleteAuthLocation(id);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.findById
     * @param id TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.findById(id)
     * @return RedcapAuthLocationVO
     */
    public RedcapAuthLocationVO findById(Long id) {
    	return authLocationService.findById(id);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.searchByRedcapAuth
     * @param auth TODO: Model Documentation for
bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.searchByRedcapAuth(auth)
     * @return RedcapAuthLocationVO
     */
    public RedcapAuthLocationVO searchByRedcapAuth(RedcapAuthVO auth) {
    	return authLocationService.searchByRedcapAuth(auth);
    }

    /**
     * TODO: Model Documentation for
     * bw.ub.ehealth.dhislink.redacap.auth.service.AuthLocationService.findAll
     * @return Collection<RedcapAuthLocationVO>
     */
    public Collection<RedcapAuthLocationVO> findAll() {
    	return authLocationService.findAll();
    }
}
