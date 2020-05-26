package bw.ub.ehealth.dhislink.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserDetailsImpl implements UserDetails {
	
	@JsonIgnore
	private String username;
	
	@JsonIgnore
    private String password;
	
	public UserDetailsImpl(String username, String password) {
		
		this.username = username;
		this.password = password;
	}

	public UserDetailsImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		SimpleGrantedAuthority gl = new SimpleGrantedAuthority("ROLE_USER");
		authorities.add(gl);
		
		return authorities;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}


    /**
     * @return String representation of object
     * @see Object#toString()
     */
    @Override
    public String toString()
    {

        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("\t\"username\" : \"" + this.getUsername() + "\"");
        builder.append("\t,\"password\" : \"" + this.getPassword() + "\"");
        builder.append("}");
        return builder.toString();

    }
}
