package com.project.usermanagement.Service;

import java.math.BigDecimal;

public interface NumberSystemInterface {
    String convertToIndianCurrencyInWord(BigDecimal num);
    String convertNumberToCurrency(BigDecimal num);
}
