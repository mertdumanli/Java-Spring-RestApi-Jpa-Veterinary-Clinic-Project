package com.works.dto.rest.customer;

import com.works.entities.Customer;
import com.works.entities.CustomerGroup;
import com.works.entities.constant.address.Address;
import com.works.entities.constant.address.City;
import com.works.entities.constant.address.District;
import com.works.entities.constant.pets.*;
import com.works.properties.CustomerInterlayer;
import com.works.properties.CustomerPetInterlayer;
import com.works.properties.PetListInterlayer;
import com.works.repositories._elastic.CustomerElasticSearchRepository;
import com.works.repositories._jpa.*;
import com.works.utils.REnum;
import com.works.utils.Util;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.*;

@Service
public class CustomerRestDto {
    final CityRepository cityRepository;
    final DistrictRepository districtRepository;
    final ColorPetRepository colorPetRepository;
    final TypePetRepository typePetRepository;
    final BreedPetRepository breedPetRepository;
    final CustomerGroupRepository customerGroupRepository;
    final CustomerRepository customerRepository;
    final PetRepository petRepository;
    final AddressRepository addressRepository;
    final JoinPetCustomerRepository joinPetCustomerRepository;
    final JoinTypeBreedPetRepository joinTypeBreedPetRepository;
    final CustomerElasticSearchRepository customerElasticSearchRepository;

    public CustomerRestDto(CityRepository cityRepository, DistrictRepository districtRepository, ColorPetRepository colorPetRepository, TypePetRepository typePetRepository, BreedPetRepository breedPetRepository, CustomerGroupRepository customerGroupRepository, CustomerRepository customerRepository, PetRepository petRepository, AddressRepository addressRepository, JoinPetCustomerRepository joinPetCustomerRepository, JoinTypeBreedPetRepository joinTypeBreedPetRepository, CustomerElasticSearchRepository customerElasticSearchRepository) {
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.colorPetRepository = colorPetRepository;
        this.typePetRepository = typePetRepository;
        this.breedPetRepository = breedPetRepository;
        this.customerGroupRepository = customerGroupRepository;
        this.customerRepository = customerRepository;
        this.petRepository = petRepository;
        this.addressRepository = addressRepository;
        this.joinPetCustomerRepository = joinPetCustomerRepository;
        this.joinTypeBreedPetRepository = joinTypeBreedPetRepository;
        this.customerElasticSearchRepository = customerElasticSearchRepository;
    }

    public Map<REnum, Object> addCustomer(CustomerPetInterlayer obj, BindingResult bindingResults) {
        Map<REnum, Object> hm = new LinkedHashMap<>();
        List<JoinPetCustomer> petCustomerList = new ArrayList<>();
        Customer customer = new Customer();
        if (!bindingResults.hasErrors()) {
            //CUSTOMER NESNES??N?? OLU??TURMA
            CustomerInterlayer customerDto = obj.getCustomerObj();

            customer.setCu_name(customerDto.getCu_name());
            customer.setCu_surname(customerDto.getCu_surname());
            if (Util.isTel(customerDto.getCu_tel1())) {
                customer.setCu_tel1(customerDto.getCu_tel1());
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Telefon1 format hatas??.");
                return hm;
            }

            if (!customerDto.getCu_tel2().equals("")) {
                if (Util.isTel(customerDto.getCu_tel2())) {
                    customer.setCu_tel2(customerDto.getCu_tel2());
                } else {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "Telefon2 format hatas??.");
                    return hm;
                }
            }

            if (Util.isEmail(customerDto.getCu_mail())) {
                customer.setCu_mail(customerDto.getCu_mail());
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Email format hatas??.");
                return hm;
            }

            customer.setCu_taxname(customerDto.getCu_taxname());
            customer.setCu_note(customerDto.getCu_note());
            customer.setCu_tcnumber(customerDto.getCu_tcnumber());
            customer.setCu_rateOfDiscount(customerDto.getCu_rateOfDiscount());

            if (customerDto.getCu_smsNotice().equals("1")) {
                customer.setCu_smsNotice("1");
            } else if (customerDto.getCu_smsNotice().equals("0")) {
                customer.setCu_smsNotice("0");
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Sms format hatas??");
                return hm;
            }

            if (customerDto.getCu_mailNotice().equals("1")) {
                customer.setCu_mailNotice("1");
            } else if (customerDto.getCu_mailNotice().equals("0")) {
                customer.setCu_mailNotice("0");
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Mail format hatas??");
                return hm;
            }

            //Address Class Add
            Address address = new Address();

            //CITY
            Integer city_id = 0;
            if (!customerDto.getCu_cities().equals("Se??im Yap??n??z")) {
                try {
                    city_id = Integer.parseInt(customerDto.getCu_cities());
                } catch (NumberFormatException e) {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "??l Numaras?? Cast Hatas??");
                    return hm;
                }
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "??l numaras?? rakamlardan olu??mal??d??r.");
                return hm;
            }
            Optional<City> optCity = cityRepository.findById(city_id);
            if (optCity.isPresent()) {
                address.setCity(optCity.get());
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Veri taban??nda" + customerDto.getCu_cities() + " numaral?? il bulunamad??.");
                return hm;
            }

            //DISTRICT
            Integer district_id = 0;
            if (!customerDto.getCu_districts().equals("Se??im Yap??n??z")) {
                try {
                    district_id = Integer.parseInt(customerDto.getCu_districts());
                } catch (NumberFormatException e) {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "??l??e Numaras?? Cast Hatas??");
                    return hm;
                }
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "??l??e numaras?? rakamlardan olu??mal??d??r.");
                return hm;
            }
            Optional<District> optDistrict = districtRepository.findById(district_id);
            if (optDistrict.isPresent()) {
                if (optCity.get().getCid() != optDistrict.get().getCid()) {
                    //Girilen ??l??e numaras?? ??le ait mi?
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "Girilen ??l??e, girilen ??le ait de??il.");
                    return hm;
                }
                address.setDistrict(optDistrict.get());
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Veri taban??nda" + customerDto.getCu_districts() + " numaral?? il??e bulunamad??.");
                return hm;
            }

            //Address <String>
            address.setCu_address(customerDto.getCu_address());

            //Add Address to DB after add customer
            Address address1 = addressRepository.save(address);
            customer.setAddress(address1);

            //CUSTOMERGROUP
            Integer group_id = 0;
            if (!customerDto.getCu_group().equals("Se??im Yap??n??z")) {
                try {
                    group_id = Integer.parseInt(customerDto.getCu_group());
                } catch (NumberFormatException e) {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "M????teri grbuu numaras?? Cast Hatas??");
                    return hm;
                }
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "M????teri grup numaras?? rakamlardan olu??mal??d??r.");
                return hm;
            }
            Optional<CustomerGroup> optGroup = customerGroupRepository.findById(group_id);
            if (optGroup.isPresent()) {
                customer.setCustomerGroup(optGroup.get());
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Veri taban??nda" + customerDto.getCu_group() + " numaral?? m????teri grubu bulunamad??.");
                return hm;
            }

            try {
                //Eklenen customer
                customer = customerRepository.save(customer);
            } catch (DataIntegrityViolationException e) {
                //email ve tc unique hatas??
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Veri taban??nda ayn?? mail - tc kimlik numaras?? mevcut.");
                return hm;
            }


            //PET L??STES?? OLU??TURMA
            for (PetListInterlayer item : obj.getPetList()) {
                Pet pet = new Pet();

                pet.setPet_name(item.getName());
                pet.setPet_chipNumber(item.getChipNumber());
                pet.setPet_earTag(item.getEarTag());

                pet.setPet_bornDate(item.getBornDate());


                if (item.getNeutering().equals("true")) {
                    pet.setPet_neutering(true);
                } else {
                    pet.setPet_neutering(false);
                }

                if (item.getGender().equals("true")) {
                    pet.setPet_gender(true);
                } else {
                    pet.setPet_gender(false);
                }

                //Color
                Integer color_id = 0;
                if (!item.getColor().equals("0")) {
                    try {
                        color_id = Integer.parseInt(item.getColor());
                    } catch (NumberFormatException e) {
                        hm.put(REnum.STATUS, false);
                        hm.put(REnum.MESSAGE, "Pet Color Cast Hatas??!");
                        return hm;
                    }
                } else {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "Renk Se??ilmedi.");
                    return hm;
                }

                Optional<ColorPet> optColor_pet = colorPetRepository.findById(color_id);
                if (optColor_pet.isPresent()) {
                    pet.setColorPet(optColor_pet.get());
                } else {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "Veri taban??nda" + item.getColor() + " numaral?? renk bulunamad??.");
                    return hm;
                }

                //Irk T??r Nesnesi olu??turma
                JoinTypeBreedPet joinTypeBreedPet = new JoinTypeBreedPet();

                //Type
                Integer type_id = 0;
                if (!item.getType().equals("0")) {
                    try {
                        type_id = Integer.parseInt(item.getType());
                    } catch (NumberFormatException e) {
                        hm.put(REnum.STATUS, false);
                        hm.put(REnum.MESSAGE, "Pet Type Cast Hatas??");
                        return hm;
                    }
                } else {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "Tip Se??ilmedi");
                    return hm;
                }
                System.out.println("type_id : " + type_id);
                Optional<TypePet> optType = typePetRepository.findById(type_id);
                if (optType.isPresent()) {
                    joinTypeBreedPet.setTypePet(optType.get());
                } else {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "Veri taban??nda" + item.getType() + " numaral?? t??r bulunamad??.");
                    return hm;
                }

                //Breed
                Integer breed_id = 0;
                if (!item.getBreed().equals("0")) {
                    try {
                        breed_id = Integer.parseInt(item.getBreed());
                    } catch (NumberFormatException e) {
                        hm.put(REnum.STATUS, false);
                        hm.put(REnum.MESSAGE, "Pet Breed Cast Hatas??");
                        return hm;
                    }
                } else {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "Irk Se??ilmedi");
                    return hm;
                }
                System.out.println("breed_id : " + breed_id);
                Optional<BreedPet> optBreed = breedPetRepository.findById(breed_id);
                if (optBreed.isPresent()) {
                    if (optBreed.get().getType_pet_id() != optType.get().getTy_id()) {
                        //Girilen Irk numaras?? T??re ait mi?
                        hm.put(REnum.STATUS, false);
                        hm.put(REnum.MESSAGE, "Girilen Irk, girilen t??re ait de??il.");
                        return hm;
                    }
                    joinTypeBreedPet.setBreedPet(optBreed.get());
                } else {
                    hm.put(REnum.STATUS, false);
                    hm.put(REnum.MESSAGE, "Veri taban??nda" + item.getBreed() + " numaral?? ??rk bulunamad??.");
                    return hm;
                }
                joinTypeBreedPet = joinTypeBreedPetRepository.save(joinTypeBreedPet);
                pet.setJoinTypeBreedPet(joinTypeBreedPet);

                pet = petRepository.save(pet);
                JoinPetCustomer joinPetCustomer = new JoinPetCustomer();
                joinPetCustomer.setCustomer(customer);
                joinPetCustomer.setPet(pet);
                joinPetCustomerRepository.save(joinPetCustomer);
                petCustomerList.add(joinPetCustomer);
            }
            //----------------------------------------------------------------------------------------------------------
            //CUSTOMER BA??ARIYLA EKLENM???? VARSA DA PETLER?? EKLENM????
            //ELASTIC SEARCH VER?? TABANINA EKLENME ????LEM?? BURADA GER??EKLE????YOR
            com.works.models.elasticsearch.Customer customerElastic = new com.works.models.elasticsearch.Customer();
            customerElastic.setId(customer.getCu_id().toString());
            customerElastic.setName(customer.getCu_name());
            customerElastic.setSurname(customer.getCu_surname());
            customerElastic.setTel1(customer.getCu_tel1());
            customerElastic.setMail(customer.getCu_mail());
            customerElastic.setTcnumber(customer.getCu_tcnumber());
            customerElastic.setGroup(customer.getCustomerGroup().getCu_gr_name());
            customerElastic.setCity(customer.getAddress().getCity().getCname());
            customerElastic.setDistrict(customer.getAddress().getDistrict().getDname());
            customerElasticSearchRepository.save(customerElastic);
            //----------------------------------------------------------------------------------------------------------

        } else {
            hm.put(REnum.STATUS, false);
            hm.put(REnum.MESSAGE, "Girilen de??erlerde hata(lar) mevcut. (Validasyon)");
            hm.put(REnum.ERROR, Util.errors(bindingResults));
            return hm;
        }
        hm.put(REnum.STATUS, true);
        hm.put(REnum.MESSAGE, "????lem Ba??ar??l??!");
        hm.put(REnum.COUNT, "Pet Say??s??: " + petCustomerList.size() + " Eklenen M????teri Say??s??: 1");
        if (petCustomerList.size() > 0) {
            hm.put(REnum.RESULT, petCustomerList);
        } else {
            hm.put(REnum.RESULT, customer);
        }
        return hm;
    }

}
