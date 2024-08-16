package com.project.h2database.Utility;

import com.project.h2database.Service.NumberSystemInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@Slf4j
@Component
public class IndianNumberSystem implements NumberSystemInterface {
    @Override
    public String convertToIndianCurrencyInWord(BigDecimal num) {
        try {

            long number = num.longValue();
            long no = num.longValue();
            int decimal = (int) (num.remainder(BigDecimal.ONE).doubleValue() * 100);
            int digits_length = (int) Math.log10(no) + 1;
            int i = 0;
            int j = 0;
            int k = 0;
            int counter = 0;
            ArrayList<String> str = new ArrayList<>();
            HashMap<Integer, String> words = new HashMap<>();
            words.put(0, "");
            words.put(1, "One");
            words.put(2, "Two");
            words.put(3, "Three");
            words.put(4, "Four");
            words.put(5, "Five");
            words.put(6, "Six");
            words.put(7, "Seven");
            words.put(8, "Eight");
            words.put(9, "Nine");
            words.put(10, "Ten");
            words.put(11, "Eleven");
            words.put(12, "Twelve");
            words.put(13, "Thirteen");
            words.put(14, "Fourteen");
            words.put(15, "Fifteen");
            words.put(16, "Sixteen");
            words.put(17, "Seventeen");
            words.put(18, "Eighteen");
            words.put(19, "Nineteen");
            words.put(20, "Twenty");
            words.put(30, "Thirty");
            words.put(40, "Forty");
            words.put(50, "Fifty");
            words.put(60, "Sixty");
            words.put(70, "Seventy");
            words.put(80, "Eighty");
            words.put(90, "Ninety");
            String digits[] = {"", "Hundred", "Thousand", "Lakh", "Crore"};
            while (i < digits_length) {
                int divider = (k == 2) ? 10 : 100;
                number = no % divider;
                no = no / divider;
                i += divider == 10 ? 1 : 2;
                if (i < 8) {
                    counter = str.size();
                    String plural = (counter > 0 && number > 9) ? "s" : "";
                    String tmp = (number < 21) ? words.get(Integer.valueOf((int) number)) + (number != 0 ? " " + digits[counter] + plural : "")
                            : words.get(Integer.valueOf((int) Math.floor(number / 10) * 10)) + " " + words.get(Integer.valueOf((int) (number % 10))) + " " + digits[counter] + plural;
                    str.add(tmp);
                    counter = 0;
                    k = i;
                } else if (i > 8) {
                    divider = (k == 2) ? 10 : 100;
                    j += divider == 10 ? 1 : 2;
                    String plural = (counter > 0 && number > 9) ? "s" : "";
                    if (j == 2) {
                        String tmp = (number < 21) ? words.get(Integer.valueOf((int) number)) + " " + digits[4] + plural
                                : words.get(Integer.valueOf((int) Math.floor(number / 10) * 10)) + " " + words.get(Integer.valueOf((int) (number % 10))) + " " + digits[4] + plural;
                        str.add(tmp);
                    } else {
                        String tmp = (number < 21) ? words.get(Integer.valueOf((int) number)) + (number != 0 ? " " + digits[counter] + plural : "")
                                : words.get(Integer.valueOf((int) Math.floor(number / 10) * 10)) + " " + words.get(Integer.valueOf((int) (number % 10))) + " " + digits[counter] + plural;
                        str.add(tmp);
                    }
                    k = j;
                    counter++;
                } else {
                    //str.add("");
                }
            }
            Collections.reverse(str);
            String Rupees = String.join(" ", str).trim();
            String paise = "";
            if (decimal > 0) {
                if (decimal < 21) {
                    paise = words.get(decimal);
                } else {
                    int tens = (int) Math.floor(decimal / 10) * 10;
                    int ones = decimal % 10;
                    paise = words.get(tens);
                    if (ones > 0) {
                        paise += " " + words.get(ones);
                    }
                }
                paise += " Paise";
            }
            if (decimal > 0) {
                if (Rupees.isEmpty()) {
                    return paise + " Only";
                } else {
                    return Rupees + " Rupees" + " And " + paise + " Only";
                }
            } else {
                return Rupees + " Rupees" + " Only";
            }
        } catch (Exception ex) {
            log.info("Exception in Converting to number System", ex);
            return null;
        }
    }

    @Override
    public String convertNumberToCurrency(BigDecimal input) {
        try {
            StringBuffer finalTurnOver = new StringBuffer();
            BigDecimal fractionalPart = input.remainder(BigDecimal.ONE);
            BigDecimal decimalValue = fractionalPart.setScale(2, RoundingMode.DOWN);
            String numString = decimalValue.stripTrailingZeros().toPlainString();
            numString = numString.startsWith("0") ? numString.substring(1) : numString;
            BigDecimal integerValue = input.setScale(0, RoundingMode.DOWN);
            StringBuilder sbInput = new StringBuilder(integerValue.toString());
            Integer len = sbInput.length();
            for (int i = 0; i < len; ) {
                if (sbInput.charAt(i) == ',') {
                    sbInput.deleteCharAt(i);
                    len--;
                    i--;
                } else if (sbInput.charAt(i) == ' ') {
                    sbInput.deleteCharAt(i);
                    len--;
                    i--;
                } else {
                    i++;
                }
            }
            StringBuilder sbInputReverse = sbInput.reverse();
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < len; i++) {
                if (i == 2) {
                    if(sbInput.length() ==3) {
                        output.append(sbInputReverse.charAt(i));
                    }else {
                        output.append(sbInputReverse.charAt(i));
                        output.append(",");
                    }
                } else if (i > 2 && i % 2 == 0 && i + 1 < len) {
                    output.append(sbInputReverse.charAt(i));
                    output.append(",");
                } else {
                    output.append(sbInputReverse.charAt(i));
                }
            }
            StringBuilder reverseOutput = output.reverse();
            StringBuffer turnover = finalTurnOver.append(reverseOutput).append(numString);
            return turnover.toString();
        } catch (Exception ex) {
            log.info("Exception in Converting to number System", ex);
            return null;
        }
    }
}
