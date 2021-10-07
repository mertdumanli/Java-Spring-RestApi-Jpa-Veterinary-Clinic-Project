package com.works.properties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "Sadece Pet Ekleme Ara Katman Modeli", description = "Pet Ekleme İçin Kullanılır.")
public class AddPetInterlayer extends PetListInterlayer {
    @ApiModelProperty(value = "Müşteri sıra numarası", required = true, notes = "Müşteri sıra numarası girilmezse işlemler iptal edilir.", example = "1")
    @NotNull(message = "cu_id NULL OLAMAZ")
    @Min(value = 0, message = "En az 0 olabilir.")
    private Integer cu_id;
}
