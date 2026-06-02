/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.controller;

import com.paymentchain.service.KeycloakRestService;
import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.paymentchain.exception.BussinesRuleException;
import com.paymentchain.service.JwtService;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController

public class IndexController {
    private Logger logger = LoggerFactory.getLogger(IndexController.class);
    
    @Autowired
    private KeycloakRestService restService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(@RequestHeader("Authorization") String authHeader) throws BussinesRuleException {
        try {
            DecodedJWT jwt = JWT.decode(authHeader.replace("Bearer", "").trim());

            // Comprobar que el Token es váido
            Jwk jwk = jwtService.getJwk();
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            algorithm.verify(jwt);

            // Comprobar que el Token contiene Roles
            List<String> roles = ((List) jwt.getClaim("realm_access").asMap().get("roles"));

            // Comprobar que el Token aún no expira
            Date expiryDate = jwt.getExpiresAt();
            if (expiryDate.before(new Date())) {
                throw new Exception("token is expired");
            }
            // all validation passed
            HashMap HashMap = new HashMap();
            for (String str : roles) {
                HashMap.put(str, str.length());
            }
            return ResponseEntity.ok(HashMap);
        } catch (Exception e) {
            logger.error("exception : {} ", e.getMessage());
            throw new BussinesRuleException("01", e.getMessage(),HttpStatus.FORBIDDEN);             
        }
    }
    
    @GetMapping("/valid")
    public ResponseEntity<?> valid(@RequestHeader("Authorization") String authHeader) throws BussinesRuleException {
        try {
            restService.checkValidity(authHeader);
            return ResponseEntity.ok(new HashMap (){{
                put("is_valid", "true");
            }});
        } catch (Exception e) {
            logger.error("token is not valid, exception : {} ", e.getMessage());
           throw new BussinesRuleException("is_valid", "False",HttpStatus.FORBIDDEN);   
           
        }
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(String username, String password) {
        String login = restService.login(username, password);
       return ResponseEntity.ok(login);
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> logout(@RequestParam(value = "refresh_token", name = "refresh_token") String refreshToken) throws BussinesRuleException {
        try {
            restService.logout(refreshToken);
            return ResponseEntity.ok(new HashMap (){{
                put("logout", "true");
            }});
        } catch (Exception e) {
            logger.error("unable to logout, exception : {} ", e.getMessage());
            throw new BussinesRuleException("logout", "False",HttpStatus.FORBIDDEN);   
        }
    }  
    @PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refresh(@RequestParam(value = "refresh_token", name = "refresh_token") String refreshToken) throws BussinesRuleException {
        try {            
            return ResponseEntity.ok(restService.refresh(refreshToken));
        } catch (Exception e) {
            logger.error("unable to refresh, exception : {} ", e.getMessage());
            throw new BussinesRuleException("refresh", "False",HttpStatus.FORBIDDEN);   
        }
    }  
}
