package com.works.dto.rest;

import com.works.entities.Product;
import com.works.entities.Purchase;
import com.works.entities.Supplier;
import com.works.entities.projections.PurchaseInfo;
import com.works.properties.PurchaseInterlayer;
import com.works.repositories._jpa.ProductRepository;
import com.works.repositories._jpa.PurchaseRepository;
import com.works.repositories._jpa.SupplierRepository;
import com.works.utils.REnum;
import com.works.utils.Util;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.*;

@Service
public class PurchaseRestDto {

    final ProductRepository productRepository;
    final SupplierRepository supplierRepository;
    final PurchaseRepository purchaseRepository;

    public PurchaseRestDto(ProductRepository productRepository, SupplierRepository supplierRepository, PurchaseRepository purchaseRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
    }


    public Map<REnum, Object> insertPurchase(PurchaseInterlayer purchaseInterlayer, BindingResult bindingResult) {
        Map<REnum, Object> hm = new LinkedHashMap<>();
        if (!bindingResult.hasErrors()) {
            Purchase purchase = new Purchase();
            purchase.setPurchase_detail(purchaseInterlayer.getPNote());
            purchase.setPurchase_code(UUID.randomUUID().toString());
            purchase.setPurchase_number(purchaseInterlayer.getPurchase_number());
            purchase.setPurchase_type(purchaseInterlayer.getPurchase_type());
            Optional<Product> optProduct = productRepository.findById(purchaseInterlayer.getProduct_id());
            purchase.setProduct(optProduct.get());
            if (optProduct.isPresent()) {
                optProduct.get().setProduct_stokMiktari(optProduct.get().getProduct_stokMiktari() + purchaseInterlayer.getPurchase_number());
                productRepository.saveAndFlush(optProduct.get());
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Veri taban??nda " + purchaseInterlayer.getProduct_id() + " numaral?? ??r??n bulunamad??.");
                return hm;
            }
            Optional<Supplier> optSupplier = supplierRepository.findById(purchaseInterlayer.getSupplier_id());
            if (optSupplier.isPresent()) {
                purchase.setSupplier(optSupplier.get());
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Veri taban??nda " + purchaseInterlayer.getSupplier_id() + " numaral?? tedarik??i bulunamad??.");
                return hm;
            }
            purchase.setPurchase_total(optProduct.get().getProduct_alis() * purchaseInterlayer.getPurchase_number());

            purchaseRepository.save(purchase);

            hm.put(REnum.STATUS, true);
            hm.put(REnum.MESSAGE, "????lem Ba??ar??l??!");
            hm.put(REnum.RESULT, purchase);
            return hm;
        }

        hm.put(REnum.STATUS, false);
        hm.put(REnum.MESSAGE, "Girilen de??erlerde hata(lar) mevcut. (Validasyon)");
        hm.put(REnum.ERROR, Util.errors(bindingResult));
        return hm;
    }

    public Map<REnum, Object> getSupplierList() {
        Map<REnum, Object> hm = new LinkedHashMap<>();
        try {
            hm.put(REnum.STATUS, true);
            hm.put(REnum.MESSAGE, "Liste ba??ar??yla geri d??nd??.");
            hm.put(REnum.RESULT, supplierRepository.findAll());

        } catch (Exception e) {
            hm.put(REnum.STATUS, false);
            hm.put(REnum.ERROR, "Listeleme s??ras??nda bir hata olu??tu");
        }
        return hm;
    }

    public Map<REnum, Object> getProductList() {
        Map<REnum, Object> hm = new LinkedHashMap<>();
        try {
            hm.put(REnum.STATUS, true);
            hm.put(REnum.MESSAGE, "Liste ba??ar??yla geri d??nd??.");
            hm.put(REnum.RESULT, productRepository.findAll());

        } catch (Exception e) {
            hm.put(REnum.STATUS, false);
            hm.put(REnum.ERROR, "Listeleme s??ras??nda bir hata olu??tu");
        }
        return hm;
    }

    public Map<REnum, Object> getRows() {
        Map<REnum, Object> hm = new LinkedHashMap<>();
        try {
            hm.put(REnum.STATUS, true);
            hm.put(REnum.MESSAGE, "getRows i??lemi ba??ar??yla ger??ekle??ti");
            hm.put(REnum.RESULT, purchaseRepository.getRows());

        } catch (Exception e) {
            hm.put(REnum.STATUS, false);
            hm.put(REnum.ERROR, "getRows i??lemi s??ras??nda bir hata olu??tu");
        }
        return hm;
    }

    public Map<REnum, Object> getRowsSearching(String search) {
        Map<REnum, Object> hm = new LinkedHashMap<>();
        try {
            List<PurchaseInfo> purchaseList = purchaseRepository.getRowsSearching(search.trim());
            if (purchaseList.size() > 0) {
                hm.put(REnum.STATUS, true);
                hm.put(REnum.MESSAGE, "Arama i??lemi ba??ar??yla ger??ekle??ti");
                hm.put(REnum.COUNT, "Veri taban??nda " + purchaseList.size() + " adet sonu?? bulundu.");
                hm.put(REnum.RESULT, purchaseRepository.getRowsSearching(search));
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "Veri bulunamad??.");
            }
        } catch (Exception e) {
            hm.put(REnum.STATUS, false);
            hm.put(REnum.ERROR, "Arama s??ras??nda bir hata olu??tu");
        }
        return hm;
    }

    public Map<REnum, Object> delete(String stPurchaseId) {
        Map<REnum, Object> hm = new LinkedHashMap<>();
        try {
            Integer purchaseId = Integer.parseInt(stPurchaseId);
            Optional<Purchase> optionalPurchase = purchaseRepository.findById(purchaseId);
            if (optionalPurchase.isPresent()) {
                optionalPurchase.get().getProduct().setProduct_stokMiktari(optionalPurchase.get().getProduct().getProduct_stokMiktari() - optionalPurchase.get().getPurchase_number());
                productRepository.saveAndFlush(optionalPurchase.get().getProduct());
                purchaseRepository.deleteById(purchaseId);
                hm.put(REnum.STATUS, true);
                hm.put(REnum.MESSAGE, "Ba??ar??yla silindi");
                hm.put(REnum.RESULT, optionalPurchase);
                return hm;
            } else {
                hm.put(REnum.STATUS, false);
                hm.put(REnum.MESSAGE, "B??yle bir fatura bulunamad??, silme i??lemi ger??ekle??emedi.");
            }
        } catch (Exception e) {
            hm.put(REnum.MESSAGE, "stPurchaseId Integer de??er olmal??d??r.");
        }
        return hm;
    }
}
