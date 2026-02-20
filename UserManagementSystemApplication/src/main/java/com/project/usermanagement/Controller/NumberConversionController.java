package com.project.usermanagement.Controller;

import com.project.usermanagement.Service.NumberSystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/number/")
public class NumberConversionController {

    @Autowired
    NumberSystemInterface numberSystemInterface;
    @GetMapping("/in-word")
    public String getNumberInWord(@RequestParam BigDecimal num){
       return  numberSystemInterface.convertToIndianCurrencyInWord(num);
    }

    @GetMapping("/in-metric")
    public String getNumInMetricSystem(@RequestParam BigDecimal num){
        return  numberSystemInterface.convertNumberToCurrency(num);
    }
}
