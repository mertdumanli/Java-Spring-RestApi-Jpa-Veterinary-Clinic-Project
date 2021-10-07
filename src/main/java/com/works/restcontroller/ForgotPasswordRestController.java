package com.works.restcontroller;

import com.works.dto.rest.ForgotPasswordRestDto;
import com.works.utils.REnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rest/forgotpassword")
@Api(value = "Forgot Password Rest Controller")
public class ForgotPasswordRestController {

    final ForgotPasswordRestDto forgotPasswordRestDto;

    public ForgotPasswordRestController(ForgotPasswordRestDto forgotPasswordRestDto) {
        this.forgotPasswordRestDto = forgotPasswordRestDto;
    }

    @ApiOperation(value = "Şifremi Unuttum Bana Taken Oluştur Servisi")
    @GetMapping("")
    public Map<Object, Object> forgotpassword(@RequestParam("us_mail") String us_mail) {
        return forgotPasswordRestDto.forgotpassword(us_mail);
    }

    @ApiOperation(value = "Yeni Şifre Belirleme Servisi")
    @PostMapping("/change")
    public Map<REnum, Object> change(@RequestParam("mail") String mail, @RequestParam("ref") String ref, @RequestParam("newpass") String newpass) {
        return forgotPasswordRestDto.change(mail, ref, newpass);
    }
}
