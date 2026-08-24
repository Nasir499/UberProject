package com.example.uberentityservice.models;


import jakarta.persistence.Entity;
import lombok.*;

import java.util.Random;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OTP extends BaseModel{

    private String code;

    private String sentNumber;

    public static OTP make(String phoneNumber){
        Random random = new Random();
        Integer code = random.nextInt(900000) + 100000;

        return OTP.builder().code(Integer.toString(code)).sentNumber(phoneNumber).build();
    }
}
