package com.project.usermanagement.Controller;

import com.project.usermanagement.DTO.ApiResponse;
import com.project.usermanagement.Service.NumberSystemInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class NumberConversionController {

    @Autowired
    NumberSystemInterface numberSystemInterface;
    
    @GetMapping("/number/in-word")
    public ResponseEntity<ApiResponse<String>> getNumberInWord(@RequestParam BigDecimal num){
       log.debug("Request received to convert number {} to words", num);
       try {
           String result = numberSystemInterface.convertToIndianCurrencyInWord(num);
           ApiResponse<String> response = ApiResponse.success("Number converted successfully", result);
           log.info("Number {} converted to words successfully", num);
           return ResponseEntity.ok(response);
       } catch (Exception e) {
           log.error("Error converting number {} to words", num, e);
           ApiResponse<String> response = ApiResponse.error("Failed to convert number to words", "CONVERSION_ERROR");
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
       }
    }

    @GetMapping("/number/in-metric")
    public ResponseEntity<ApiResponse<String>> getNumInMetricSystem(@RequestParam BigDecimal num){
       log.debug("Request received to convert number {} to metric", num);
       try {
           String result = numberSystemInterface.convertNumberToCurrency(num);
           ApiResponse<String> response = ApiResponse.success("Number converted successfully", result);
           log.info("Number {} converted to metric successfully", num);
           return ResponseEntity.ok(response);
       } catch (Exception e) {
           log.error("Error converting number {} to metric", num, e);
           ApiResponse<String> response = ApiResponse.error("Failed to convert number to metric", "CONVERSION_ERROR");
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
       }
    }
}
