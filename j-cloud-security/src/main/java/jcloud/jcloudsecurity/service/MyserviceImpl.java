package jcloud.jcloudsecurity.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class MyserviceImpl implements MyService{
    @Override
    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
        Object obj = authentication.getPrincipal();

        if (obj instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) obj;
            return userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return false;
    }
}
