package com.cafe.management.service;

import com.cafe.management.entity.Coupon;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Coupon createCoupon(Coupon coupon) {
        coupon.setCode(coupon.getCode().toUpperCase());
        return couponRepository.save(coupon);
    }

    public Coupon updateCoupon(Long id, Coupon details) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        coupon.setCode(details.getCode().toUpperCase());
        coupon.setDescription(details.getDescription());
        coupon.setDiscountType(details.getDiscountType());
        coupon.setDiscountValue(details.getDiscountValue());
        coupon.setMinOrderAmount(details.getMinOrderAmount());
        coupon.setMaxDiscount(details.getMaxDiscount());
        coupon.setValidUntil(details.getValidUntil());
        coupon.setActive(details.getActive());
        return couponRepository.save(coupon);
    }

    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }
}
