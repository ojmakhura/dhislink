package bw.ub.ehealth.security;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import bw.ub.ehealth.dhislink.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JwtTokenProvider {

	private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
	
	@Value("${app.jwtSecret}")
	private String jwtSecret;
	
	@Value("${app.jwtExpirationInMs}")
	private int jwtExpirationInMs;
	
	public String generateToken(Authentication authentication) {
		
		UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
		
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
		
		return Jwts.builder()
				.setSubject(principal.getUsername())
				.setIssuedAt(new Date())
				.setExpiration(expiryDate)
				.signWith(SignatureAlgorithm.HS512, jwtSecret)
				.compact();
	}
	
	public String getUsernameFromJWT(String token) {
		
		Claims claims = Jwts.parser()
				.setSigningKey(jwtSecret)
				.parseClaimsJws(token)
				.getBody();
		
		
		return claims.getSubject();
	}
	
	public boolean validateToken(String authToken) {
		
		try {
			logger.info(Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken).toString());
			return true;
		} catch (SignatureException ex) {
			logger.error("Invalid JWT signature");
		} catch (MalformedJwtException ex) {
			logger.error("Invalid JWT token");
		} catch (ExpiredJwtException ex) {
			logger.error("Expired JWT token");
		} catch (UnsupportedJwtException ex) {
			logger.error("Unsupported JWT token");
		} catch (IllegalArgumentException ex) {
			logger.error("JWT string is empty");
		}
		
		return false;
	}
}
