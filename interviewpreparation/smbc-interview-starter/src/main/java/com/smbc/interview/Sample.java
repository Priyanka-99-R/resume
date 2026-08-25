package com.smbc.interview;


public class Sample {

    public boolean isReady() {
        return true;
    }

    public boolean isEven(int n) {
        return n % 2 == 0;
    }

    public String reverse(String input) {
        if (input == null) {
            return null;
        }
        return new StringBuilder(input).reverse().toString();
    }
}
